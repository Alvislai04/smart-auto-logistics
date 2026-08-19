begin;

create table if not exists public.stock_alert_rules (
    id bigint generated always as identity primary key,
    rule_key character varying not null,
    party_key character varying not null,
    vehicle_model_code character varying not null,
    logistics_stage character varying not null,
    minimum_quantity integer not null,
    active boolean not null default true,
    last_evaluated_at timestamp without time zone,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    constraint uk_stock_alert_rules_rule_key unique (rule_key),
    constraint uk_stock_alert_rules_scope unique (
        party_key,
        vehicle_model_code,
        logistics_stage
    ),
    constraint fk_stock_alert_rules_party
        foreign key (party_key)
        references public.shipment_party_profiles (party_key),
    constraint fk_stock_alert_rules_vehicle_model
        foreign key (vehicle_model_code)
        references public.vehicle_models (model_code),
    constraint ck_stock_alert_rules_stage check (
        logistics_stage in (
            'MANUFACTURING',
            'STOCK_YARD',
            'WORK_IN_PROGRESS',
            'READY_FOR_DISPATCH',
            'IN_TRANSIT',
            'ARRIVED_AT_DEALERSHIP',
            'DELIVERED'
        )
    ),
    constraint ck_stock_alert_rules_minimum_quantity check (minimum_quantity >= 1)
);

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'uk_stock_alert_rules_rule_key'
          and conrelid = 'public.stock_alert_rules'::regclass
    ) then
        alter table public.stock_alert_rules
            add constraint uk_stock_alert_rules_rule_key unique (rule_key);
    end if;

    if not exists (
        select 1
        from pg_constraint
        where conname = 'uk_stock_alert_rules_scope'
          and conrelid = 'public.stock_alert_rules'::regclass
    ) then
        alter table public.stock_alert_rules
            add constraint uk_stock_alert_rules_scope unique (
                party_key,
                vehicle_model_code,
                logistics_stage
            );
    end if;

    if not exists (
        select 1
        from pg_constraint
        where conname = 'fk_stock_alert_rules_party'
          and conrelid = 'public.stock_alert_rules'::regclass
    ) then
        alter table public.stock_alert_rules
            add constraint fk_stock_alert_rules_party
            foreign key (party_key)
            references public.shipment_party_profiles (party_key);
    end if;

    if not exists (
        select 1
        from pg_constraint
        where conname = 'fk_stock_alert_rules_vehicle_model'
          and conrelid = 'public.stock_alert_rules'::regclass
    ) then
        alter table public.stock_alert_rules
            add constraint fk_stock_alert_rules_vehicle_model
            foreign key (vehicle_model_code)
            references public.vehicle_models (model_code);
    end if;
end
$$;

alter table public.stock_alert_rules
    drop constraint if exists ck_stock_alert_rules_stage;

alter table public.stock_alert_rules
    add constraint ck_stock_alert_rules_stage check (
        logistics_stage in (
            'MANUFACTURING',
            'STOCK_YARD',
            'WORK_IN_PROGRESS',
            'READY_FOR_DISPATCH',
            'IN_TRANSIT',
            'ARRIVED_AT_DEALERSHIP',
            'DELIVERED'
        )
    );

alter table public.stock_alert_rules
    drop constraint if exists ck_stock_alert_rules_minimum_quantity;

alter table public.stock_alert_rules
    add constraint ck_stock_alert_rules_minimum_quantity
    check (minimum_quantity >= 1);

create index if not exists idx_stock_alert_rules_party_key
    on public.stock_alert_rules (party_key);

create index if not exists idx_stock_alert_rules_active
    on public.stock_alert_rules (active);

alter table public.alerts
    alter column shipment_id drop not null;

alter table public.alerts
    add column if not exists stock_alert_rule_id bigint;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'fk_alerts_stock_alert_rule'
          and conrelid = 'public.alerts'::regclass
    ) then
        alter table public.alerts
            add constraint fk_alerts_stock_alert_rule
            foreign key (stock_alert_rule_id)
            references public.stock_alert_rules (id);
    end if;
end
$$;

do $$
declare
    constraint_name text;
begin
    for constraint_name in
        select c.conname
        from pg_constraint c
        where c.conrelid = 'public.alerts'::regclass
          and c.contype = 'c'
          and pg_get_constraintdef(c.oid) ilike '%alert_type%'
    loop
        execute format(
            'alter table public.alerts drop constraint %I',
            constraint_name
        );
    end loop;
end
$$;

alter table public.alerts
    add constraint ck_alerts_alert_type check (
        alert_type in (
            'DELAY',
            'IDLE',
            'ETA_OVERDUE',
            'ROUTE_DEVIATION',
            'PROGRESS_SHORTFALL',
            'SENSOR_WARNING',
            'MISSING_UPDATE',
            'LOW_STOCK',
            'ORDER_UPDATE'
        )
    );

alter table public.alerts
    drop constraint if exists ck_alerts_reference;

alter table public.alerts
    add constraint ck_alerts_reference check (
        (shipment_id is not null and stock_alert_rule_id is null)
        or (shipment_id is null and stock_alert_rule_id is not null)
    );

create index if not exists idx_alerts_stock_alert_rule_id
    on public.alerts (stock_alert_rule_id);

commit;
