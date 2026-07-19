document.getElementById('loginForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;

    try {
        const response = await fetch('http://localhost:80/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ 
                email: email, 
                senha: senha
            })
        });

        if (response.ok) {
            const data = await response.json();
            console.log('Login efetuado com sucesso:', data);
            
            localStorage.setItem('token', data.token);
            window.location.href = 'catalogo.html';
            
        } else {
            alert('Falha no login. Verifique suas credenciais.');
        }
    } catch (error) {
        console.error('Erro de conexão:', error);
        alert('Erro ao tentar conectar com o servidor.');
    }
});