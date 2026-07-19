const token = localStorage.getItem('token');
const userCargo = localStorage.getItem('userCargo');
let usuarioSelecionadoId = null;

document.addEventListener('DOMContentLoaded', async () => {
    if (!token || userCargo !== 'ADMIN') {
        alert("Acesso restrito. Apenas administradores têm permissão para visualizar esta página.");
        window.location.href = 'perfil.html';
        return;
    }

    await carregarUsuarios();

    document.getElementById('btnSalvarDinheiro').addEventListener('click', adicionarDinheiro);
    document.getElementById('btnSalvarCartaUser').addEventListener('click', adicionarCartaAoUsuario);
});

async function carregarUsuarios() {
    try {
        const response = await fetch('http://localhost:80/profile/admin/usuarios', {
            method: 'GET',
            headers: { 
                'Authorization': `Bearer ${token}`,
                'User-cargo': userCargo
            }
        });

        if (response.ok) {
            const usuarios = await response.json();
            renderizarUsuarios(usuarios);
        } else {
            document.getElementById('usersListBody').innerHTML = '<div class="market-row"><span colspan="5" style="width:100%; text-align:center;">Erro ao carregar lista de usuários.</span></div>';
        }
    } catch (error) { console.error("Erro de conexão:", error); }
}

function renderizarUsuarios(usuarios) {
    const tbody = document.getElementById('usersListBody');
    tbody.innerHTML = '';

    usuarios.forEach(user => {
        const row = document.createElement('div');
        row.className = 'market-row';
        row.style.gridTemplateColumns = '2fr 1fr 1fr 1fr 1fr';
        
        row.innerHTML = `
            <span style="font-size: 11px; word-break: break-all; text-align: left;">${user.id}</span>
            <span>${user.username}</span>
            <span>${user.dinheiro !== undefined ? user.dinheiro.toFixed(2) : '0.00'}</span>
            <span style="text-align: center;"><button class="btn-action" style="padding: 5px 15px; font-size: 12px; margin-top: 0px; width: 75%" onclick="abrirModalDinheiro('${user.id}', '${user.username}')">+$</button></span>
            <span style="text-align: center;"><button class="btn-action" style="padding: 5px 10px; font-size: 12px; margin-top: 0px; width: 75%" onclick="abrirModalCarta('${user.id}', '${user.username}')">+ Carta</button></span>
        `;
        tbody.appendChild(row);
    });
}

function abrirModalDinheiro(id, username) {
    usuarioSelecionadoId = id;
    document.getElementById('dinheiroUserName').innerText = `Usuário: ${username}`;
    document.getElementById('modalDinheiro').style.display = 'block';
}

function abrirModalCarta(id, username) {
    usuarioSelecionadoId = id;
    document.getElementById('cartaUserName').innerText = `Usuário: ${username}`;
    document.getElementById('modalCarta').style.display = 'block';
}

function fecharModais() {
    document.getElementById('modalDinheiro').style.display = 'none';
    document.getElementById('modalCarta').style.display = 'none';
    document.getElementById('valorDinheiro').value = '';
    document.getElementById('idCartaAdd').value = '';
    usuarioSelecionadoId = null;
}

// ==========================================
// INTEGRAÇÃO COM BACKEND
// ==========================================

async function adicionarDinheiro() {
    const valor = parseFloat(document.getElementById('valorDinheiro').value);
    if (isNaN(valor) || valor <= 0) return alert("Insira um valor válido.");

    const payload = {
        dinheiro: valor 
    };

    try {
        const response = await fetch(`http://localhost:80/profile/usuario/${usuarioSelecionadoId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
                'User-cargo': userCargo
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert("Dinheiro adicionado com sucesso!");
            fecharModais();
            await carregarUsuarios();
        } else {
            alert("Erro ao adicionar dinheiro. Verifique a requisição.");
        }
    } catch (error) { console.error(error); }
}

async function adicionarCartaAoUsuario() {
    const idCarta = document.getElementById('idCartaAdd').value;
    if (!idCarta) return alert("Insira o ID da carta.");

    const payload = {
        cartaId: idCarta 
    };

    try {
        const response = await fetch(`http://localhost:80/inventory/usuario/${usuarioSelecionadoId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
                'User-cargo': userCargo
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert("Carta concedida com sucesso!");
            fecharModais();
        } else {
            alert("Erro ao adicionar carta ao inventário.");
        }
    } catch (error) { console.error(error); }
}