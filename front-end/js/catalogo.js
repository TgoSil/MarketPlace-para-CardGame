const token = localStorage.getItem('token');
const userId = "123e4567-e89b-12d3-a456-426614174000"; 

document.addEventListener('DOMContentLoaded', async () => {
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    await carregarCarteira();
    await carregarCatalogo();
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

async function carregarCatalogo() {
    try {
        const response = await fetch(`http://localhost:80/catalog/carta`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        let cartas = [];
        if (response.ok) {
            cartas = await response.json();
        } else {
            console.error('Falha ao buscar o catálogo do servidor.');
        }
        
        renderizarGrid(cartas);
    } catch (error) {
        console.error('Erro ao buscar catálogo:', error);
    }
}

function renderizarGrid(cartas) {
    const grid = document.getElementById('catalogGrid');
    grid.innerHTML = '';

    cartas.forEach(carta => {
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
        // Usamos apenas a classe base, sem fundo dinâmico, pois a imagem já tem o fundo
        cardElement.className = 'card'; 
        // Remove padding e bordas para a imagem preencher tudo
        cardElement.style.padding = '0';
        cardElement.style.border = 'none';
        cardElement.style.backgroundColor = 'transparent';
        
        // Apenas a imagem ocupando todo o espaço
        const cardHtml = `
            <img src="${urlDaImagem}" alt="${carta.nome}" style="width: 100%; height: 100%; object-fit: contain; border-radius: 10px; display: block;" onerror="this.src='sprites/placeholder.png'">
        `;

        cardElement.innerHTML = cardHtml;
        cardElement.addEventListener('click', () => abrirModal(carta, urlDaImagem, poderDaCarta));
        grid.appendChild(cardElement);
    });
}

// Lógica do Modal
const modal = document.getElementById('cardModal');
const btnClose = document.getElementById('closeModal');
const btnComprar = document.getElementById('btnComprar');
let cartaSelecionada = null;

function abrirModal(carta, urlDaImagem, poderDaCarta) {
    cartaSelecionada = carta;
    
    const previewContainer = document.getElementById('modalCardPreview');
    // Limpa o conteúdo e exibe a imagem em tamanho maior
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
    alert(`Redirecionando para o Mercado para comprar: ${cartaSelecionada.nome}`);
});