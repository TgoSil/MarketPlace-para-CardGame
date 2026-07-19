document.getElementById('registerForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const usuario = document.getElementById('usuario').value;
    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;

    try {
        const response = await fetch('http://localhost:80/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            // Enviando com a chave 'senha' conforme o DTO do seu back-end
            body: JSON.stringify({ 
                username: usuario,
                email: email, 
                senha: senha 
            })
        });

        if (response.ok) {
            const data = await response.json();
            console.log('Registro efetuado com sucesso:', data);
            
            // Salva o token retornado
            localStorage.setItem('token', data.token);
            
            // Redireciona diretamente para o catálogo
            window.location.href = 'catalogo.html'; 
        } else {
            const errorData = await response.text();
            alert('Falha ao registrar: ' + (errorData || 'Verifique os dados informados.'));
        }
    } catch (error) {
        console.error('Erro de conexão:', error);
        alert('Erro ao tentar conectar com o servidor.');
    }
});