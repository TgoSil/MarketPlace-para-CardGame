const token = localStorage.getItem('token');
const userId = "123e4567-e89b-12d3-a456-426614174000"; 

document.addEventListener('DOMContentLoaded', async () => {
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    await carregarCarteira();
    await carregarInventario();
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
    } catch (error) {
        console.error('Erro ao buscar carteira:', error);
    }
}

async function carregarInventario() {
    try {
        let catalogo = [];
        const resCat = await fetch(`http://localhost:80/catalog/carta`, { 
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (resCat.ok) {
            catalogo = await resCat.json();
        }

        let inventario = [];
        const resInv = await fetch(`http://localhost:80/inventory/usuario`, { 
            headers: { 'User-Id': userId, 'Authorization': `Bearer ${token}` }
        });
        if (resInv.ok) {
            inventario = await resInv.json();
        }

        renderizarGrid(catalogo, inventario);
    } catch (error) {
        console.error('Erro ao buscar dados do inventário e catálogo:', error);
    }
}

function renderizarGrid(catalogo, inventario) {
    const grid = document.getElementById('inventoryGrid');
    grid.innerHTML = '';

    catalogo.forEach(carta => {
        // Lógica CORRIGIDA e BLINDADA para evitar falsos positivos (undefined === undefined)
        const posse = inventario.find(i => {
            // 1. Tenta cruzar pelo ID (garantindo que ambos os lados não são nulos/undefined)
            const idInv = i.idCarta || i.id_carta || i.cartaId;
            const idCat = carta.id || carta.carta_id || carta.cartaId || carta.idCarta;
            
            if (idInv && idCat && idInv === idCat) {
                return true;
            }
            
            // 2. Se o ID falhar, tenta cruzar pelo nome exato
            const nomeInv = i.nomeCarta || i.nome;
            const nomeCat = carta.nome;
            
            if (nomeInv && nomeCat && nomeInv === nomeCat) {
                return true;
            }
            
            return false;
        });

        const possuiCarta = !!posse;

        let urlDaImagem = carta.imagemUrl || 'sprites/placeholder.png';
        
        if (urlDaImagem.includes('drive.google.com')) {
            try {
                const params = new URLSearchParams(urlDaImagem.substring(urlDaImagem.indexOf('?')));
                const fileId = params.get('id');
                if (fileId) {
                    urlDaImagem = `https://lh3.googleusercontent.com/d/${fileId}`;
                }
            } catch (e) {
                console.error("Erro ao converter URL do Drive:", e);
            }
        }

        const poderDaCarta = carta.vida !== undefined ? carta.vida : carta.poder;

        const cardElement = document.createElement('div');
        // Agora, se o JS não encontrar correspondência exata, a classe será 'unowned' (opacidade reduzida)
        cardElement.className = `card ${possuiCarta ? 'owned' : 'unowned'}`; 
        cardElement.style.padding = '0';
        cardElement.style.border = 'none';
        cardElement.style.backgroundColor = 'transparent';
        
        const cardHtml = `
            <img src="${urlDaImagem}" alt="${carta.nome}" style="width: 100%; height: 100%; object-fit: contain; border-radius: 10px; display: block;" onerror="this.src='sprites/placeholder.png'">
        `;
        
        cardElement.innerHTML = cardHtml;
        
        cardElement.addEventListener('click', () => {
            if(possuiCarta) {
                // Passamos a quantidade real caso o usuário possua, ou 1 como fallback visual
                const qtdPossuida = posse.quantidade !== undefined ? posse.quantidade : 1;
                abrirModal(carta, qtdPossuida, urlDaImagem, poderDaCarta);
            }
        });
        
        grid.appendChild(cardElement);
    });
}

// Lógica do Modal
const modal = document.getElementById('cardModal');
const btnClose = document.getElementById('closeModal');
const btnVender = document.getElementById('btnVender');

let cartaSelecionada = null;
let quantidadePossuida = 0;

function abrirModal(carta, quantidade, urlDaImagem, poderDaCarta) {
    cartaSelecionada = carta;
    quantidadePossuida = quantidade;
    
    const previewContainer = document.getElementById('modalCardPreview');
    previewContainer.innerHTML = `<img src="${urlDaImagem}" alt="${carta.nome}" style="width: 100%; max-width: 250px; border-radius: 15px; margin-bottom: 15px; box-shadow: 0 4px 8px rgba(0,0,0,0.3);">`;
    previewContainer.appendChild(btnVender);

    document.getElementById('modalCardName').innerText = carta.nome;
    document.getElementById('modalCardDesc').innerText = carta.descricao;
    document.getElementById('modalCardPower').innerText = poderDaCarta;
    document.getElementById('modalCardType').innerText = carta.tipo;
    document.getElementById('modalCardRarity').innerText = carta.raridade;
    
    const qtdElement = document.getElementById('modalCardQty');
    if (qtdElement) {
        qtdElement.innerText = quantidade;
    }

    modal.style.display = 'flex';
}

btnClose.addEventListener('click', () => {
    modal.style.display = 'none';
});

btnVender.addEventListener('click', () => {
    const id = cartaSelecionada.id || cartaSelecionada.carta_id || cartaSelecionada.idCarta;
    window.location.href = `mercado.html?action=vender&cartaId=${id}`;
});