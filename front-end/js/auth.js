// Este script roda em todas as páginas
document.addEventListener('DOMContentLoaded', () => {
    const cargo = localStorage.getItem('userCargo');
    const navAdmin = document.getElementById('navAdminUsuarios');
    const btnLogout = document.getElementById('btnLogout');
    
    // Se a página possuir o link e o usuário for ADMIN, exibe a opção
    if (navAdmin && cargo === 'ADMIN') {
        navAdmin.style.display = 'inline-block';
    }
    
    if (btnLogout) {
        btnLogout.addEventListener('click', () => {
            const confirmar = confirm("Tem certeza que deseja sair?");
            
            if (confirmar) {
                // 1. Apaga o Token JWT
                localStorage.removeItem('token');
                
                // 2. Apaga o Cargo
                localStorage.removeItem('userCargo');
                
                // (Opcional) Limpa todo o localStorage se não houver mais nada a preservar
                // localStorage.clear();
                
                // 3. Redireciona para a tela de Login
                window.location.href = 'index.html';
            }
        });
    }
});

async function verificarCargoGlobal() {
    if (!localStorage.getItem('userCargo') && localStorage.getItem('token')) {
        try {
            // Busca o perfil para pegar o cargo injetado pelo backend
            const response = await fetch(`http://localhost:80/profile/usuario`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
            });
            const data = await response.json();
            if (data.cargo) {
                localStorage.setItem('userCargo', data.cargo);
            }
        } catch (error) {
            console.error("Erro ao sincronizar cargo:", error);
        }
    }
}

verificarCargoGlobal();