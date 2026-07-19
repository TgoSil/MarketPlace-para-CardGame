const token = localStorage.getItem('token');
const userCargo = localStorage.getItem('userCargo'); // Assumindo que você salva isso no login
const userId = "123e4567-e89b-12d3-a456-426614174000"; 

document.addEventListener('DOMContentLoaded', async () => {
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    // Configuração de Admin
    if (userCargo === 'ADMIN') {
        document.getElementById('btnNovaCarta').style.display = 'inline-block';
    }

    // Listeners Admin
    document.getElementById('btnNovaCarta').addEventListener('click', () => {
        document.getElementById('modalAdmin').style.display = 'block';
    });

    document.getElementById('btnSalvarCarta').addEventListener('click', salvarNovaCarta);

    await carregarCarteira();
    await carregarCatalogo();
});

async function salvarNovaCarta() {
    const novaCarta = {
        nome: document.getElementById('adminNome').value,
        tipo: document.getElementById('adminTipo').value,
        vida: parseInt(document.getElementById('adminVida').value),
        raridade: document.getElementById('adminRaridade').value,
        descricao: document.getElementById('adminDescricao').value,
        imagemUrl: document.getElementById('adminUrl').value
    };

    try {
        const response = await fetch('http://localhost:80/catalog/carta', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
                'User-cargo': 'ADMIN' // Requisito do CatalogController[cite: 11]
            },
            body: JSON.stringify(novaCarta)
        });

        if (response.ok) {
            alert("Carta adicionada com sucesso!");
            document.getElementById('modalAdmin').style.display = 'none';
            await carregarCatalogo();
        } else {
            alert("Erro ao adicionar carta. Verifique as permissões.");
        }
    } catch (e) {
        console.error(e);
    }
}

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
        
        let cartas = [];
        if (response.ok) {
            cartas = await response.json();
        }
        renderizarGrid(cartas);
    } catch (error) { console.error('Erro ao buscar catálogo:', error); }
}

function renderizarGrid(cartas) {
    const grid = document.getElementById('catalogGrid');
    grid.innerHTML = '';

    cartas.forEach(carta => {
        const corBase = getCorElemento(carta.tipo);
        const icone = getIconeElemento(carta.tipo);
        const poderDaCarta = carta.vida !== undefined ? carta.vida : carta.poder;
        const descricao = carta.descricao || '';
        
        let urlDaImagem = carta.imagemUrl;
        
        // Tratamento para links do Google Drive
        if (urlDaImagem && urlDaImagem.includes('drive.google.com')) {
            try {
                const params = new URLSearchParams(urlDaImagem.substring(urlDaImagem.indexOf('?')));
                const fileId = params.get('id');
                if (fileId) { urlDaImagem = `https://lh3.googleusercontent.com/d/${fileId}`; }
            } catch (e) { console.error("Erro ao converter URL:", e); }
        }

        const cardElement = document.createElement('div');
        cardElement.className = 'card';
        cardElement.style.cursor = 'pointer';

        // Função interna que constrói a carta com o design antigo (cor, texto, ícone) e um "?"
        const aplicarDesignColorido = () => {
            // Remove as formatações da imagem invisível e aplica a cor de fundo
            cardElement.style.padding = ''; 
            cardElement.style.border = ''; 
            cardElement.style.backgroundColor = corBase;
            
            cardElement.innerHTML = `
                <div class="card-image-container" style="display: flex; justify-content: center; align-items: center; min-height: 120px; background-color: rgba(255,255,255,0.2); border-radius: 5px; margin-bottom: 10px;">
                    <span style="font-size: 60px; font-weight: bold; color: white; text-shadow: 2px 2px 0 #000;">?</span>
                </div>
                <div class="card-info">
                    <strong>${carta.nome}:</strong><br>
                    ${descricao}
                </div>
                <div class="card-stats">
                    <div class="element-icon">${icone}</div>
                    <div class="power-pill">Poder: ${poderDaCarta}</div>
                </div>
            `;
        };

        if (urlDaImagem) {
            // Se houver URL, preparamos a div para receber a imagem completa (fundo transparente)
            cardElement.style.padding = '0';
            cardElement.style.border = 'none';
            cardElement.style.backgroundColor = 'transparent';

            const img = document.createElement('img');
            img.src = urlDaImagem;
            img.alt = carta.nome;
            img.style.width = '100%';
            img.style.height = '100%';
            img.style.objectFit = 'contain';
            img.style.borderRadius = '10px';
            img.style.display = 'block';

            // O SEGREDO: Se a URL falhar ou for inválida, disparamos o design antigo!
            img.onerror = () => aplicarDesignColorido();

            cardElement.appendChild(img);
        } else {
            // Se já sabemos que não tem imagem no banco, aplicamos o design antigo direto
            aplicarDesignColorido();
        }

        // Abre o modal ao clicar (mesmo para cartas sem imagem)
        cardElement.addEventListener('click', () => abrirModal(carta, urlDaImagem, poderDaCarta));
        grid.appendChild(cardElement);
    });
}

// Funções utilitárias restauradas
function getCorElemento(tipo) {
    switch(tipo?.toLowerCase()) {
        case 'agua': return '#4b7bec';
        case 'fogo': return '#fc5c65';
        case 'grama': return '#2bcbba';
        case 'eletrico': return '#fed330';
        default: return '#a5b1c2';
    }
}

function getIconeElemento(tipo) {
    switch(tipo?.toLowerCase()) {
        case 'agua': return '💧';
        case 'fogo': return '🔥';
        case 'grama': return '🌱';
        case 'eletrico': return '⚡';
        default: return '❓';
    }
}

const modal = document.getElementById('cardModal');
const btnClose = document.getElementById('closeModal');
const btnComprar = document.getElementById('btnComprar');
let cartaSelecionada = null;

function abrirModal(carta, urlDaImagem, poderDaCarta) {
    cartaSelecionada = carta;
    const previewContainer = document.getElementById('modalCardPreview');
    previewContainer.innerHTML = `<img src="${urlDaImagem}" alt="${carta.nome}" style="width: 100%; max-width: 250px; border-radius: 15px; margin-bottom: 15px; box-shadow: 0 4px 8px rgba(0,0,0,0.3);">`;
    previewContainer.appendChild(btnComprar);

    document.getElementById('modalCardName').innerText = carta.nome;
    document.getElementById('modalCardDesc').innerText = carta.descricao;
    document.getElementById('modalCardPower').innerText = poderDaCarta;
    document.getElementById('modalCardType').innerText = carta.tipo;
    document.getElementById('modalCardRarity').innerText = carta.raridade;

    modal.style.display = 'flex';
}

btnClose.addEventListener('click', () => modal.style.display = 'none');

btnComprar.addEventListener('click', () => {
    const id = cartaSelecionada.id || cartaSelecionada.carta_id || cartaSelecionada.idCarta;
    window.location.href = `mercado.html?action=comprar&cartaId=${id}`;
});