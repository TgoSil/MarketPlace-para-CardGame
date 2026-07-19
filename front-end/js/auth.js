// Este script roda em todas as páginas
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

document.addEventListener('DOMContentLoaded', () => {
    const cargo = localStorage.getItem('userCargo');
    const navAdmin = document.getElementById('navAdminUsuarios');
    
    // Se a página possuir o link e o usuário for ADMIN, exibe a opção
    if (navAdmin && cargo === 'ADMIN') {
        navAdmin.style.display = 'inline-block';
    }
});

verificarCargoGlobal();