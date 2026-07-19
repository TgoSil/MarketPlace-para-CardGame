const token = localStorage.getItem('token');
let catalogoGlobal = [];
let auctionsGlobais = [];
let bidsGlobais = [];

document.addEventListener('DOMContentLoaded', async () => {
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    // Carregamento inicial de dados
    await carregarCarteira();
    await carregarCatalogo();
    await carregarListaMercado();

    // ==========================================
    // MAPEAMENTO DE EVENTOS DOS BOTÕES DO HTML[cite: 6]
    // ==========================================
    
    // Controles da lista principal
    document.getElementById('btnFazerOferta')?.addEventListener('click', () => abrirNovaOferta());
    document.getElementById('searchMarket')?.addEventListener('input', renderizarTabela);
    
    // Controles do formulário de Nova Oferta
    document.getElementById('btnVoltarMercado')?.addEventListener('click', voltarParaLista);
    document.getElementById('selectCarta')?.addEventListener('change', atualizarPreviewDaCarta);
    
    // Botões de Envio
    document.getElementById('btnComprarOferta')?.addEventListener('click', criarBid);
    document.getElementById('btnVenderOferta')?.addEventListener('click', criarAuction);
});

// ==========================================
// FUNÇÕES DE BUSCA DE DADOS (GET)
// ==========================================

async function carregarCarteira() {
    try {
        // Removido o 'User-Id' da header, o Gateway injeta automaticamente
        const response = await fetch(`http://localhost:80/profile/usuario`, {
            headers: { 'Authorization': `Bearer ${token}` } 
        });
        if (response.ok) {
            const data = await response.json();
            const walletElement = document.getElementById('walletBalance');
            if (walletElement) walletElement.innerText = data.dinheiro.toFixed(2);
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
            if (select) {
                select.innerHTML = '';
                catalogoGlobal.forEach(carta => {
                    const option = document.createElement('option');
                    option.value = carta.id || carta.carta_id || carta.idCarta;
                    option.innerText = carta.nome;
                    select.appendChild(option);
                });
                atualizarPreviewDaCarta();
            }
        }
    } catch (error) { console.error('Erro ao buscar catálogo:', error); }
}

async function carregarListaMercado() {
    try {
        // Removido o 'User-Id' da header
        const response = await fetch(`http://localhost:80/orders`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (response.ok) {
            const data = await response.json();
            auctionsGlobais = data.auctions || []; 
            bidsGlobais = data.bids || [];
            renderizarTabela();
        }
    } catch (error) { console.error('Erro ao carregar mercado:', error); }
}

// ==========================================
// LÓGICA DE RENDERIZAÇÃO E INTERFACE
// ==========================================

function renderizarTabela() {
    const tbody = document.getElementById('marketListBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';

    // Lê os filtros do HTML[cite: 6]
    const mostrarVendas = document.getElementById('checkVendas')?.checked;
    const mostrarCompras = document.getElementById('checkCompras')?.checked;
    const termoBusca = document.getElementById('searchMarket')?.value.toLowerCase() || '';

    let listaParaExibir = [];
    if (mostrarVendas) listaParaExibir.push(...auctionsGlobais.map(a => ({...a, tipo: 'VENDA'})));
    if (mostrarCompras) listaParaExibir.push(...bidsGlobais.map(b => ({...b, tipo: 'COMPRA'})));

    // Aplica o filtro da barra de pesquisa
    if (termoBusca) {
        listaParaExibir = listaParaExibir.filter(oferta => {
            const cartaRelacionada = catalogoGlobal.find(c => (c.id || c.carta_id || c.idCarta) === oferta.idCarta);
            const nomeCarta = cartaRelacionada ? cartaRelacionada.nome.toLowerCase() : '';
            return nomeCarta.includes(termoBusca);
        });
    }

    if (listaParaExibir.length === 0) {
        tbody.innerHTML = '<div class="market-row"><span colspan="4" style="width: 100%; text-align: center;">Nenhuma oferta encontrada.</span></div>';
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
    if (diffHoras > 24) return Math.ceil(diffHoras / 24) + ' dias';
    
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
    // Limpa os campos ao voltar
    if(document.getElementById('valorMin')) document.getElementById('valorMin').value = '';
    if(document.getElementById('valorMax')) document.getElementById('valorMax').value = '';
    
    carregarListaMercado(); // Recarrega para trazer novidades
}

function atualizarPreviewDaCarta() {
    const cartaId = document.getElementById('selectCarta')?.value;
    const carta = catalogoGlobal.find(c => (c.id || c.carta_id || c.idCarta) === cartaId);
    
    if (carta) {
        let imgUrl = carta.imagemUrl || 'sprites/placeholder.png';
        if (imgUrl.includes('drive.google.com')) {
            const params = new URLSearchParams(imgUrl.substring(imgUrl.indexOf('?')));
            const fileId = params.get('id');
            if (fileId) { imgUrl = `https://lh3.googleusercontent.com/d/${fileId}`; }
        }
        
        const imgElement = document.getElementById('previewImg');
        if (imgElement) imgElement.src = imgUrl;
    }
}

// ==========================================
// LÓGICA DE CRIAÇÃO E ENVIO (POST)
// ==========================================

function calcularInstanteExpiracao(dias, horas) {
    const agora = new Date();
    const milissegundosDias = dias * 24 * 60 * 60 * 1000;
    const milissegundosHoras = horas * 60 * 60 * 1000;
    const dataExpiracao = new Date(agora.getTime() + milissegundosDias + milissegundosHoras);
    return dataExpiracao.toISOString();
}

async function criarAuction() {
    // Utilizando os IDs baseados no seu HTML[cite: 6]
    const idCarta = document.getElementById('selectCarta')?.value;
    const valorMinStr = document.getElementById('valorMin')?.value?.replace(',', '.');
    const valorMaxStr = document.getElementById('valorMax')?.value?.replace(',', '.');
    const dias = parseInt(document.getElementById('duracaoDias')?.value || "0");
    const horas = parseInt(document.getElementById('duracaoHoras')?.value || "0");

    if (!idCarta) return alert("Por favor, selecione uma carta.");
    if (!valorMinStr) return alert("O preço mínimo é obrigatório para vender.");
    if (dias === 0 && horas === 0) return alert("A duração da oferta deve ser maior que zero.");

    const payload = {
        idCarta: idCarta,
        precoMinimo: parseFloat(valorMinStr), 
        precoTeto: valorMaxStr ? parseFloat(valorMaxStr) : null,
        expiraEm: calcularInstanteExpiracao(dias, horas)
    };

    await enviarRequisicao('/auction', payload, 'Venda');
}

async function criarBid() {
    // Utilizando os IDs baseados no seu HTML[cite: 6]
    const idCarta = document.getElementById('selectCarta')?.value;
    const valorMaxStr = document.getElementById('valorMax')?.value?.replace(',', '.');
    const perfil = document.getElementById('perfilCompra')?.value || "PADRAO";
    const dias = parseInt(document.getElementById('duracaoDias')?.value || "0");
    const horas = parseInt(document.getElementById('duracaoHoras')?.value || "0");

    if (!idCarta) return alert("Por favor, selecione uma carta.");
    if (!valorMaxStr) return alert("O limite de pagamento é obrigatório para compra.");
    if (dias === 0 && horas === 0) return alert("A duração da oferta deve ser maior que zero.");

    const payload = {
        idCarta: idCarta,
        limitePagamento: parseFloat(valorMaxStr),
        perfilCompra: perfil,
        expiraEm: calcularInstanteExpiracao(dias, horas)
    };

    await enviarRequisicao('/bid', payload, 'Compra');
}

async function enviarRequisicao(endpoint, payload, tipo) {
    try {
        const response = await fetch(`http://localhost:80/order${endpoint}`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` 
            },
            body: JSON.stringify(payload)
        });
        
        if (response.ok) {
            alert(`Oferta de ${tipo} criada com sucesso!`);
            voltarParaLista();
        } else {
            const err = await response.text();
            alert(`Erro 422: O Backend recusou os dados. Detalhe: ${err}`);
        }
    } catch (e) { 
        alert('Erro de conexão com o servidor. Verifique se a porta 80 está respondendo.'); 
    }
}