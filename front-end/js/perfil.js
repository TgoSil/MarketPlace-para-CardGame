document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('token');
    // Em produção, recupere o ID do payload do JWT ou deixe o API Gateway inferir
    const userId = "123e4567-e89b-12d3-a456-426614174000"; 

    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    await carregarPerfil(userId, token);
    await verificarStatusRecompensa(userId, token);

    document.getElementById('btnResgatar').addEventListener('click', async () => {
        await resgatarRecompensa(userId, token);
    });
});

async function carregarPerfil(userId, token) {
    try {
        const response = await fetch(`http://localhost:80/profile/usuario`, {
            headers: { 
                'User-Id': userId, 
                'Authorization': `Bearer ${token}` 
            }
        });

        if (response.ok) {
            const data = await response.json();
            
            document.querySelector('.profile-title').innerText = data.username.toUpperCase();
            document.querySelector('.profile-input-mock').innerText = formatarData(data.criadoEm);
            document.querySelectorAll('.wallet-pill').forEach(el => {
                el.innerText = data.dinheiro.toFixed(2);
            });
        }
    } catch (error) {
        console.error('Erro ao buscar dados do perfil:', error);
    }
}

async function verificarStatusRecompensa(userId, token) {
    try {
        const response = await fetch('http://localhost:80/rewards/status', {
            method: 'GET',
            headers: { 
                'User-Id': userId, 
                'Authorization': `Bearer ${token}` 
            }
        });

        if (response.ok) {
            const status = await response.json(); // Mapeia para StatusRecompensaDto
            
            document.getElementById('rewardCiclo').innerText = status.ciclo;
            document.getElementById('rewardDia').innerText = status.diaCiclo;

            const btnResgatar = document.getElementById('btnResgatar');
            const rewardInfo = document.getElementById('rewardInfo');

            if (status.disponivel) {
                btnResgatar.style.display = 'block';
                
                // Formata a mensagem com base no tipo da próxima recompensa
                if (status.tipoProximaRecompensa === 'MOEDAS') {
                    rewardInfo.innerText = `Recompensa Disponível: ${status.moedasPrevistas} Moedas!`;
                } else if (status.tipoProximaRecompensa === 'PACOTE') {
                    rewardInfo.innerText = `Recompensa Disponível: Pacote ${status.tierPacotePrevisto}!`;
                }
            } else {
                btnResgatar.style.display = 'none';
                rewardInfo.innerText = `Você já resgatou sua recompensa de hoje. Volte amanhã! (Streak Atual: ${status.streakAtual})`;
            }
        }
    } catch (error) {
        console.error('Erro ao verificar status da recompensa:', error);
        document.getElementById('rewardInfo').innerText = "Erro ao carregar recompensa.";
    }
}

async function resgatarRecompensa(userId, token) {
    const btnResgatar = document.getElementById('btnResgatar');
    btnResgatar.disabled = true;
    btnResgatar.innerText = "Resgatando...";

    try {
        const response = await fetch('http://localhost:80/rewards/resgate', {
            method: 'POST',
            headers: { 
                'User-Id': userId, 
                'Authorization': `Bearer ${token}` 
            }
        });

        if (response.ok) {
            const resgate = await response.json(); // Mapeia para ResgateResponseDto
            
            let mensagemAlerta = `Resgate efetuado com sucesso!\n\n`;
            
            if (resgate.tipoReward === 'MOEDAS') {
                mensagemAlerta += `Você recebeu: ${resgate.moedasRecebidas} Moedas\n`;
            } else if (resgate.tipoReward === 'PACOTE') {
                mensagemAlerta += `Você abriu um Pacote ${resgate.tierPacote} e recebeu:\n`;
                resgate.cartas.forEach(carta => {
                    mensagemAlerta += `- ${carta.nome} (${carta.raridade})\n`;
                });
            }
            
            mensagemAlerta += `\nStreak atual: ${resgate.streak} dia(s)`;
            alert(mensagemAlerta);

            // Atualiza os dados da tela após o resgate
            await carregarPerfil(userId, token);
            await verificarStatusRecompensa(userId, token);
        } else {
            const erro = await response.text();
            alert(`Falha ao resgatar: ${erro}`);
        }
    } catch (error) {
        console.error('Erro ao resgatar recompensa:', error);
        alert('Erro de conexão ao tentar resgatar a recompensa.');
    } finally {
        btnResgatar.disabled = false;
        btnResgatar.innerText = "Resgatar";
    }
}

function formatarData(dataString) {
    if (!dataString) return '--/--/----';
    const partes = dataString.split('-');
    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}