CREATE TABLE IF NOT EXISTS time_deposit
(
    id        SERIAL       primary key,
    balance   numeric(38, 2) not null,
    days      integer        not null,
    plan_type varchar(255)   not null
        constraint time_deposit_plan_type_check
            check ((plan_type)::text = ANY
                   ((ARRAY ['BASIC'::character varying, 'STUDENT'::character varying, 'PREMIUM'::character varying])::text[]))
);

CREATE TABLE IF NOT EXISTS withdrawal
(
    id              SERIAL       primary key,
    amount          numeric(19, 4) not null,
    date            date           not null,
    time_deposit_id integer        not null

    CONSTRAINT fk_withdrawal_time_deposit
            references time_deposit
);