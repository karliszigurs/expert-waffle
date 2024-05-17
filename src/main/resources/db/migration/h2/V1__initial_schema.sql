create table clients
(
    -- Possible improvement - use UUID v7 by default depending on DB engine
    id uuid not null,
    primary key (id)
);

create table accounts
(
    id        uuid           not null,
    client_id uuid           not null,
    currency  varchar(3)     not null,
    -- We will store 'cents' at this stage. See README.md for possible improvements
    balance   numeric(38, 0) not null,
    primary key (id)
);

create table transactions
(
    id                     uuid                                   not null,
    -- stored as enum ordinal. Could be stored as explicit string
    status                 tinyint check (status between 0 and 1) not null,

    -- We'll store time as UTC
    timestamp              time without time zone                 not null,

    source_account_id      uuid                                   not null,
    source_amount          numeric(38, 0)                         not null,
    source_balance         numeric(38, 0)                         not null,

    destination_account_id uuid                                   not null,
    destination_amount     numeric(38, 0)                         not null,
    destination_balance    numeric(38, 0)                         not null,

    description            varchar(255),
    primary key (id)
);

alter table if exists accounts
    add constraint accounts_to_clients_fk foreign key (client_id) references clients;

alter table if exists transactions
    add constraint transactions_to_source_account_fk foreign key (source_account_id) references accounts;

alter table if exists transactions
    add constraint transactions_to_destination_account_fk foreign key (destination_account_id) references accounts;
