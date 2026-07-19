let catalogoGlobal = [];

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    if (!token) { window.location.href = 'index.html'; return; }

    await carregarPerfil(token);
    await carregarCatalogo(token);
    await verificarStatusRecompensa(token);
    await carregarMinhasOfertas(token);

    document.getElementById('btnResgatar').addEventListener('click', () => resgatarRecompensa(token));
});

// Mesma lógica usada no mercado.js, para exibir o nome da carta em vez do UUID cru
async function carregarCatalogo(token) {
    try {
        const response = await fetch('http://localhost:80/catalog/carta', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) catalogoGlobal = await response.json();
    } catch (e) { console.error('Erro ao buscar catálogo:', e); }
}

async function carregarPerfil(token) {
    try {
        const response = await fetch('http://localhost:80/profile/usuario', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const data = await response.json();
            document.querySelector('.profile-title').innerText = data.username.toUpperCase();
            document.getElementById('criadoEm').innerText = data.criadoEm.split('T')[0].split('-').reverse().join('/');
            document.getElementById('walletBalance').innerText = data.dinheiro.toFixed(2);
        }
    } catch (e) { console.error('Erro perfil:', e); }
}

async function verificarStatusRecompensa(token) {
    try {
        const response = await fetch('http://localhost:80/rewards/status', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const status = await response.json();
            document.getElementById('rewardCiclo').innerText = status.ciclo;
            document.getElementById('rewardDia').innerText = status.diaCiclo;
            
            const btn = document.getElementById('btnResgatar');
            if (status.disponivel) {
                btn.style.display = 'block';
                document.getElementById('rewardInfo').innerText = "Resgate sua recompensa diária!";
            } else {
                btn.style.display = 'none';
                document.getElementById('rewardInfo').innerText = "Já resgatado hoje. Volte amanhã!";
            }
        }
    } catch (e) { console.error('Erro recompensa:', e); }
}

async function resgatarRecompensa(token) {
    const response = await fetch('http://localhost:80/rewards/resgate', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (response.ok) {
        alert("Resgate realizado!");
        verificarStatusRecompensa(token);
        carregarPerfil(token);
    } else {
        alert("Erro no resgate.");
    }
}

async function carregarMinhasOfertas(token) {
    try {
        const response = await fetch('http://localhost:80/order/orders', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const data = await response.json();
            const container = document.getElementById('offersList');
            container.innerHTML = '';
            
            const lista = [
                ...(data.auctions || []).map(a => ({...a, tipo: 'VENDA', id: a.idAuction})),
                ...(data.bids || []).map(b => ({...b, tipo: 'COMPRA', id: b.id}))
            ]
                // A seção é "SUAS OFERTAS ATIVAS": ofertas já EXPIRADA/CANCELADA/CONCLUIDA
                // não devem aparecer aqui misturadas com as que ainda estão em aberto.
                .filter(o => o.status === 'ATIVO');

            if (lista.length === 0) container.innerHTML = '<p>Nenhuma oferta ativa.</p>';
            else lista.forEach(o => container.appendChild(criarDivOferta(o, token)));
        }
    } catch (e) { console.error('Erro ofertas:', e); }
}

function criarDivOferta(oferta, token) {
    const cartaRelacionada = catalogoGlobal.find(c => (c.id || c.carta_id || c.idCarta) === oferta.idCarta);
    const nomeCarta = cartaRelacionada ? cartaRelacionada.nome : 'Carta Desconhecida';
    const valor = oferta.tipo === 'VENDA' ? oferta.precoMinimo : oferta.limitePagamento;

    // Reaproveita a classe .offer-item (já definida em styles.css) em vez de estilo inline,
    // seguindo o mesmo padrão visual das demais páginas.
    const div = document.createElement('div');
    div.className = 'offer-item';
    div.innerHTML = `
        <span><strong>[${oferta.tipo}]</strong> ${nomeCarta} — ${valor ? valor.toFixed(2) : '--'}</span>
        <span>${oferta.status}</span>
        <button class="btn-text-only" style="cursor:pointer;">CANCELAR</button>
    `;
    div.querySelector('button').addEventListener('click', () => cancelarOrdem(oferta.id, token));
    return div;
}

async function cancelarOrdem(id, token) {
    const response = await fetch(`http://localhost:80/order/orders/${id}/cancel`, {
        method: 'PATCH',
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (response.ok) {
        alert("Cancelado!");
        carregarMinhasOfertas(token);
    } else {
        alert("Erro ao cancelar.");
    }
}