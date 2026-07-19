const token = localStorage.getItem('token');
const userId = "123e4567-e89b-12d3-a456-426614174000"; 
let catalogoGlobal = [];
let auctionsGlobais = [];
let bidsGlobais = [];

document.addEventListener('DOMContentLoaded', async () => {
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    await carregarCarteira();
    await carregarCatalogo();
    await carregarListaMercado();

    // Event Listeners
    document.getElementById('btnFazerOferta').addEventListener('click', () => abrirNovaOferta());
    document.getElementById('btnVoltarMercado').addEventListener('click', voltarParaLista);
    document.getElementById('selectCarta').addEventListener('change', atualizarPreviewDaCarta);
    
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
    try {
        const response = await fetch(`http://localhost:80/orders`, {
            headers: { 'User-Id': userId, 'Authorization': `Bearer ${token}` }
        });
        
        if (response.ok) {
            const data = await response.json();
            // Armazena as listas separadamente[cite: 7, 8]
            auctionsGlobais = data.auctions || []; 
            bidsGlobais = data.bids || [];
            renderizarTabela();
        }
    } catch (error) { console.error('Erro ao carregar mercado:', error); }
}

function renderizarTabela() {
    const tbody = document.getElementById('marketListBody');
    tbody.innerHTML = '';

    const mostrarVendas = document.getElementById('checkVendas').checked;
    const mostrarCompras = document.getElementById('checkCompras').checked;

    let listaParaExibir = [];
    if (mostrarVendas) listaParaExibir.push(...auctionsGlobais.map(a => ({...a, tipo: 'VENDA'})));
    if (mostrarCompras) listaParaExibir.push(...bidsGlobais.map(b => ({...b, tipo: 'COMPRA'})));

    if (listaParaExibir.length === 0) {
        tbody.innerHTML = '<div class="market-row"><span colspan="4" style="width: 100%; text-align: center;">Nenhuma oferta selecionada.</span></div>';
        return;
    }

    listaParaExibir.forEach(oferta => {
        const cartaRelacionada = catalogoGlobal.find(c => (c.id || c.carta_id || c.idCarta) === oferta.idCarta);
        const nomeCarta = cartaRelacionada ? cartaRelacionada.nome : 'Carta Desconhecida';
        
        const valor = oferta.tipo === 'VENDA' ? oferta.precoMinimo : oferta.limitePagamento;
        const valorMax = oferta.tipo === 'VENDA' ? (oferta.precoTeto || '--') : '--';
        
        const html = `
            <div class="market-row">
                <span>${nomeCarta}</span>
                <span>${valor ? valor.toFixed(2) : '--'}</span>
                <span>${typeof valorMax === 'number' ? valorMax.toFixed(2) : valorMax}</span>
                <span>${formatarTempo(oferta.expiraEm)}</span>
            </div>
        `;
        tbody.insertAdjacentHTML('beforeend', html);
    });
}

function formatarTempo(expiraEm) {
    if (!expiraEm) return 'Sem validade';
    const agora = new Date();
    const expiracao = new Date(expiraEm);
    const diffMs = expiracao - agora;
    const diffHoras = Math.ceil(diffMs / (1000 * 60 * 60));

    if (diffHoras <= 0) return 'Expirado';
    if (diffHoras > 24) {
        return Math.ceil(diffHoras / 24) + ' dias';
    }
    return diffHoras + ' horas';
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
    renderizarTabela();
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
    enviarRequisicao('/auction', payload, 'Venda');
}

async function criarBid() {
    const payload = {
        idCarta: document.getElementById('selectCarta').value,
        limitePagamento: parseFloat(document.getElementById('valorMax').value),
        perfilCompra: "PADRAO",
        expiraEm: formatarDataFimDoDia(document.getElementById('ofertarAte').value)
    };
    enviarRequisicao('/bid', payload, 'Compra');
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
            alert(`Oferta de ${tipo} criada!`);
            voltarParaLista();
        } else {
            const err = await response.text();
            alert(`Erro: ${err}`);
        }
    } catch (e) { alert('Erro de conexão.'); }
}

function formatarDataFimDoDia(dataString) {
    if(!dataString) return null;
    const data = new Date(dataString);
    data.setHours(23, 59, 59, 999);
    return data.toISOString();
}