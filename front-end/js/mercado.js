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

    document.getElementById('checkVendas').addEventListener('change', carregarDadosMercado);
    document.getElementById('checkCompras').addEventListener('change', carregarDadosMercado);
    
    await carregarDadosMercado();
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

async function carregarDadosMercado() {
    const mostrarVendas = document.getElementById('checkVendas').checked;
    const mostrarCompras = document.getElementById('checkCompras').checked;

    const fetches = [];

    // Se estiver marcado, adiciona na fila de busca
    if (mostrarVendas) {
        fetches.push(fetch('http://localhost:80/order/auctions', { headers: { 'Authorization': `Bearer ${token}` } }));
    }
    if (mostrarCompras) {
        fetches.push(fetch('http://localhost:80/order/bids', { headers: { 'Authorization': `Bearer ${token}` } }));
    }

    try {
        const responses = await Promise.all(fetches);
        // Cada resposta é tratada de forma independente: se uma falhar, a outra ainda é exibida
        const data = await Promise.all(responses.map(r => r.ok ? r.json() : []));

        auctionsGlobais = [];
        bidsGlobais = [];

        // Mapeia os dados de volta para as variáveis globais dependendo da ordem
        let index = 0;
        if (mostrarVendas) auctionsGlobais = data[index++];
        if (mostrarCompras) bidsGlobais = data[index++];

        renderizarTabela();
    } catch (error) {
        console.error('Erro ao carregar dados do mercado:', error);
    }
}

function renderizarTabela() {
    const tbody = document.getElementById('marketListBody');
    tbody.innerHTML = '';

    const mostrarVendas = document.getElementById('checkVendas').checked;
    const mostrarCompras = document.getElementById('checkCompras').checked;
    const termoBusca = (document.getElementById('searchMarket')?.value || '').trim().toLowerCase();

    // Só faz sentido oferecer no mercado o que ainda está ATIVO
    // (ofertas EXPIRADA/CANCELADA/CONCLUIDA continuam voltando da API, mas não são "ofertas disponíveis")
    const passaNoFiltro = (oferta) => {
        if (oferta.status !== 'ATIVO') return false;
        if (!termoBusca) return true;
        const cartaRelacionada = catalogoGlobal.find(c => (c.id || c.carta_id || c.idCarta) === oferta.idCarta);
        const nomeCarta = cartaRelacionada ? cartaRelacionada.nome : '';
        return nomeCarta.toLowerCase().includes(termoBusca);
    };

    let linhasRenderizadas = 0;

    // Renderiza Vendas
    if (mostrarVendas) {
        auctionsGlobais.filter(passaNoFiltro).forEach(oferta => {
            tbody.appendChild(criarLinhaTabela('VENDA', oferta));
            linhasRenderizadas++;
        });
    }

    // Renderiza Compras
    if (mostrarCompras) {
        bidsGlobais.filter(passaNoFiltro).forEach(oferta => {
            tbody.appendChild(criarLinhaTabela('COMPRA', oferta));
            linhasRenderizadas++;
        });
    }

    if (linhasRenderizadas === 0) {
        const vazio = document.createElement('div');
        vazio.className = 'market-row';
        vazio.innerHTML = `<span style="text-align: center; width: 100%;">Nenhuma oferta encontrada.</span>`;
        tbody.appendChild(vazio);
    }
}

// Função auxiliar para criar a linha mantendo a lógica de nome da carta
function criarLinhaTabela(tipo, oferta) {
    const cartaRelacionada = catalogoGlobal.find(c => (c.id || c.carta_id || c.idCarta) === oferta.idCarta);
    const nomeCarta = cartaRelacionada ? cartaRelacionada.nome : 'Carta Desconhecida';
    
    const valor = tipo === 'VENDA' ? oferta.precoMinimo : oferta.limitePagamento;
    const valorMax = tipo === 'VENDA' ? (oferta.precoTeto || '--') : '--';

    // O cabeçalho da tabela tem 4 colunas (Carta / Valor Mín. / Valor Máx. / Tempo Restante),
    // então o tipo (VENDA/COMPRA) é indicado dentro da própria célula da carta, e não
    // como uma 5ª coluna solta (isso desalinhava as colunas com o cabeçalho).
    const div = document.createElement('div');
    div.className = 'market-row';
    div.innerHTML = `
        <span><strong>[${tipo}]</strong> ${nomeCarta}</span>
        <span>${valor ? valor.toFixed(2) : '--'}</span>
        <span>${valorMax !== '--' ? parseFloat(valorMax).toFixed(2) : '--'}</span>
        <span>${formatarTempo(oferta.expiraEm)}</span>
    `;
    return div;
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
    const list = document.getElementById('marketListView');
    const nova = document.getElementById('novaOfertaView');
    
    if (list && nova) {
        list.style.display = 'none';
        nova.style.display = 'block';
        
        if (cartaId) {
            const select = document.getElementById('selectCarta');
            if (select) {
                select.value = cartaId;
                atualizarPreviewDaCarta();
            }
        }
    } else {
        console.error("IDs de visualização não encontrados no DOM.");
    }
}

function voltarParaLista() {
    document.getElementById('novaOfertaView').style.display = 'none';
    document.getElementById('marketListView').style.display = 'block';
    // Limpa os campos ao voltar
    if(document.getElementById('valorMin')) document.getElementById('valorMin').value = '';
    if(document.getElementById('valorMax')) document.getElementById('valorMax').value = '';
    
    carregarDadosMercado(); // Recarrega para trazer novidades
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