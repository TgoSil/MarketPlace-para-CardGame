document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    // Em produção, recupere o ID do payload do JWT ou deixe o API Gateway inferir
    const userId = "123e4567-e89b-12d3-a456-426614174000"; 

    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    try {
        const response = await fetch(`http://localhost:80/profile/usuario`, {
            headers: { 
                'User-Id': userId, 
                'Authorization': `Bearer ${token}` 
            }
        });

        if (response.ok) {
            const data = await response.json();
            
            // Atualiza os elementos na tela
            document.querySelector('.profile-title').innerText = data.username.toUpperCase();
            document.querySelector('.profile-input-mock').innerText = formatarData(data.criadoEm);
            document.querySelectorAll('.wallet-pill').forEach(el => {
                el.innerText = data.dinheiro.toFixed(2);
            });
        }
    } catch (error) {
        console.error('Erro ao buscar dados do perfil:', error);
    }
});

// Transforma "2026-07-18" em "18/07/2026"
function formatarData(dataString) {
    if (!dataString) return '--/--/----';
    const partes = dataString.split('-');
    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}