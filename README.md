# Marketplace para CardGame:
Fork de Projeto da disciplina Sistemas Distribuídos (USP) realizado em grupo.

![DIAGRAMA DA ARQUITETURA](https://drive.google.com/uc?export=view&id=1wlR89JATDYbF0k5XY4rbW4JiINyhsLoh)

# Minhas contribuições:
 - Arquitetura de solução (Arquitetura.png): Propondo a modularização dos serviços, os tipos e caminhos das conexões, escolhendo as tecnologias e construindo por completo o desenho da arquitetura.
 - Funcionalidade de login/cadastro com autenticação via tokens JWT;
 - Implementação de API Gateway via Java Spring Boot;
 - Implementação do motor de leilão, incluindo:
   - 3 microsserviços a partir de Java Spring Boot;
   - Conexões síncronas REST e gRPC;
   - Conexão assíncrona via consumo e produção de eventos Kafka;
   - Persistência de dados com banco relacional (PostgreSQL) e não relacional (MongoDB);
 - Construção de imagens Docker para a maioria dos microsserviços.
 - Orquestração geral do Docker Compose (com exceção de módulos de replicação de serviços e bancos de dados);


# Projeto e Arquitetura:
O projeto consiste em um MarketPlace de cartas virtuais para um CardGame construído sob arquitetura distribuída.
Pontos chave da arquitetura do projeto são:
 - Cliente X Servidor, via APIs REST;
 - Microsserviços stateless, construídos com Java Spring Boot;
 - Event-driven (PUB/SUB), com broker Kafka;
 - Uso de banco relacional (PostgreSQL) e não relacional (MongoDB);
 - Comunicação interna síncrona RPC, via gRPC, para validações pontuais;
 - Tolerância à falhas a partir de replicação de serviços, via NGINX, e de bancos de dados PostgreSQL;
 
As funcionalidades consistem em:
 - Login/Cadastro de usuário com camada de segurança via hashing de senhas e tokens JWT;
 - CRUD para gestão de perfis e contas, compra de cartas com dinheiro virtual, gestão de cartas por administradores;
 - Recompensas diárias em forma de cartas e dinheiro virtual;
 - Motor de leilão automatizado, construído com um algoritmo de cruzamento de intenções de compra e intenções de venda, garantindo que os usuários consigam comprar e vender cartas sob um modelo de leilão assíncrono;

### Grupo original do projeto: 
- Ana Julia Silva de Oliveira - 14557202
- Lucas Giovani Santos Ross - 15471693 
- Tiago Silveira Almeida - 15490509
- Vinícius Chirnev Panhoca - 15580531 
