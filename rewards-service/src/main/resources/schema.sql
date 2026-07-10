create table if not exists "user_streak" (
    id_user uuid primary key,
    dia_ciclo int not null default 1,
    ciclo int not null default 0,
    streak int not null default 0,
    data_ultimo_login date
);

create table if not exists "rewards" (
    dia_ciclo int primary key,
    tipo_reward varchar(20) not null,
    quantidade_moedas_base int,
    tier_pacote_base varchar(20)
);

create table if not exists "pacote_probabilidades" (
    tier_pacote varchar(20) not null,
    raridade varchar(20) not null,
    porcentagem decimal(5,2) not null,
    primary key (tier_pacote, raridade)
);

create table if not exists "login" (
    id_login uuid primary key,
    id_user uuid not null,
    dia_ciclo int not null,
    data_login date not null,
    horario_login time not null,
    streak int not null,
    ciclo int not null,
    moedas_recebidas int,
    tier_pacote_recebido varchar(20),
    constraint uq_usuario_dia unique (id_user, data_login)
);

create table if not exists "cartas_recebidas_resgate" (
    id uuid primary key,
    id_login uuid not null,
    id_carta uuid not null,
    raridade_sorteada varchar(20) not null
);

create table if not exists "cartas" (
    id_carta uuid primary key,
    nome varchar(100) not null,
    raridade varchar(5) not null
);