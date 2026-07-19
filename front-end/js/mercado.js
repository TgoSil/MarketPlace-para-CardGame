const token = localStorage.getItem('token');
const userId = "123e4567-e89b-12d3-a456-426614174000"; 
let catalogoGlobal = [];

document.addEventListener('DOMContentLoaded', async () => {
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    await carregarCarteira();
    await carregarCatalogo();
    
    // Verifica se veio redirecionado com parâmetros na URL
    const urlParams = new URLSearchParams(window.location.search);
    const action = urlParams.get('action');
    const cartaId = urlParams.get('cartaId');

    if (action && cartaId) {
        abrirNovaOferta(cartaId);
    } else {
        await carregarListaMercado();
    }

    // Event Listeners dos botões
    document.getElementById('btnFazerOferta').addEventListener('click', () => abrirNovaOferta());
    document.getElementById('btnVoltarMercado').addEventListener('click', voltarParaLista);
    document.getElementById('selectCarta').addEventListener('change', atualizarPreviewDaCarta);
    
    // Submissão: Criar Bid (Compra) e Criar Auction (Venda)
    document.getElementById('btnComprarOferta').addEventListener('click', criarBid);
    document.getElementById('btnVenderOferta').addEventListener('click', criarAuction);
});

async function carregarCarteira() {
    try {
        const response = await fetch(`http://localhost:80/profile/usuario`, {
            headers: { 'User-Id': userId, 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const data = await response.json();
            document.getElementById('walletBalance').innerText = data.dinheiro.toFixed(2);
        }
    } catch (error) { console.error('Erro ao buscar carteira:', error); }
}

async function carregarCatalogo() {
    try {
        const response = await fetch(`http://localhost:80/catalog/carta`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            catalogoGlobal = await response.json();
            const select = document.getElementById('selectCarta');
            select.innerHTML = '';
            catalogoGlobal.forEach(carta => {
                const option = document.createElement('option');
                option.value = carta.id || carta.carta_id || carta.idCarta;
                option.innerText = carta.nome;
                select.appendChild(option);
            });
            atualizarPreviewDaCarta();
        }
    } catch (error) { console.error('Erro ao buscar catálogo:', error); }
}

async function carregarListaMercado() {
    // Busca as ordens do usuário. (Se houver um endpoint público no futuro, altere a URL aqui)
    try {
        const response = await fetch(`http://localhost:80/orders`, {
            headers: { 'User-Id': userId, 'Authorization': `Bearer ${token}` }
        });
        
        const tbody = document.getElementById('marketListBody');
        tbody.innerHTML = '';

        if (response.ok) {
            const data = await response.json();
            
            // O endpoint /orders retorna um Map contendo 'auctions' e 'bids'
            const auctions = data.auctions || [];
            const bids = data.bids || [];
            const todasOfertas = [...auctions, ...bids];

            if (todasOfertas.length === 0) {
                tbody.innerHTML = '<div class="market-row"><span colspan="4">Nenhuma oferta encontrada.</span></div>';
                return;
            }

            todasOfertas.forEach(oferta => {
                // Tenta cruzar com o catálogo para pegar o nome da carta
                const cartaRelacionada = catalogoGlobal.find(c => (c.id || c.carta_id) === oferta.idCarta);
                const nomeCarta = cartaRelacionada ? cartaRelacionada.nome : 'Carta Desconhecida';
                
                const min = oferta.precoMinimo ? oferta.precoMinimo.toFixed(2) : '--';
                const max = oferta.precoTeto ? oferta.precoTeto.toFixed(2) : (oferta.limitePagamento ? oferta.limitePagamento.toFixed(2) : '--');
                
                // Formatação simples de data
                const expira = oferta.expiraEm ? new Date(oferta.expiraEm).toLocaleDateString('pt-BR') : 'Sem validade';

                const html = `
                    <div class="market-row">
                        <span>${nomeCarta}</span>
                        <span>${min}</span>
                        <span>${max}</span>
                        <span>${expira}</span>
                    </div>
                `;
                tbody.insertAdjacentHTML('beforeend', html);
            });
        } else {
            tbody.innerHTML = '<div class="market-row"><span colspan="4">Erro ao buscar ofertas.</span></div>';
        }
    } catch (error) { console.error('Erro:', error); }
}

function abrirNovaOferta(cartaId = null) {
    document.getElementById('marketListView').style.display = 'none';
    document.getElementById('novaOfertaView').style.display = 'block';

    if (cartaId) {
        document.getElementById('selectCarta').value = cartaId;
        atualizarPreviewDaCarta();
    }
}

function voltarParaLista() {
    document.getElementById('novaOfertaView').style.display = 'none';
    document.getElementById('marketListView').style.display = 'block';
    carregarListaMercado();
}

function atualizarPreviewDaCarta() {
    const cartaId = document.getElementById('selectCarta').value;
    const carta = catalogoGlobal.find(c => (c.id || c.carta_id || c.idCarta) === cartaId);
    
    if (carta) {
        let imgUrl = carta.imagemUrl || 'sprites/placeholder.png';
        if (imgUrl.includes('drive.google.com')) {
            const params = new URLSearchParams(imgUrl.substring(imgUrl.indexOf('?')));
            const fileId = params.get('id');
            if (fileId) { imgUrl = `https://lh3.googleusercontent.com/d/${fileId}`; }
        }
        document.getElementById('previewImg').src = imgUrl;
    }
}

async function criarAuction() {
    const payload = {
        idCarta: document.getElementById('selectCarta').value,
        precoMinimo: parseFloat(document.getElementById('valorMin').value),
        precoTeto: parseFloat(document.getElementById('valorMax').value),
        expiraEm: formatarDataFimDoDia(document.getElementById('ofertarAte').value)
    };

    enviarRequisicao('/auction', payload, 'Oferta de Venda (Auction)');
}

async function criarBid() {
    const payload = {
        idCarta: document.getElementById('selectCarta').value,
        limitePagamento: parseFloat(document.getElementById('valorMax').value), // Mapeado para limitePagamento do BidRequestDto
        perfilCompra: "PADRAO",
        expiraEm: formatarDataFimDoDia(document.getElementById('ofertarAte').value)
    };

    enviarRequisicao('/bid', payload, 'Oferta de Compra (Bid)');
}

async function enviarRequisicao(endpoint, payload, tipo) {
    try {
        const response = await fetch(`http://localhost:80${endpoint}`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'User-Id': userId, 
                'Authorization': `Bearer ${token}` 
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert(`${tipo} criada com sucesso!`);
            voltarParaLista();
        } else {
            const error = await response.text();
            alert(`Erro ao criar ${tipo}: ${error}`);
        }
    } catch (error) {
        alert('Erro de conexão com o servidor.');
    }
}

// Transforma a string de data (YYYY-MM-DD) num formato ISO que o Spring Data (Instant) entenda
function formatarDataFimDoDia(dataString) {
    if(!dataString) return null;
    const data = new Date(dataString);
    data.setHours(23, 59, 59, 999);
    return data.toISOString();
}