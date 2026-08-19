begin;

alter table public.blockchain_records
    add column if not exists retry_count integer;

alter table public.blockchain_records
    add column if not exists last_retry_at timestamp without time zone;

update public.blockchain_records
set retry_count = 0
where retry_count is null;

do $$
begin
    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'blockchain_records'
          and column_name = 'original_event_hash'
    ) and exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'blockchain_records'
          and column_name = 'hash_rebased_at'
    ) then
        update public.blockchain_records
        set event_hash = original_event_hash
        where hash_rebased_at is not null
          and transaction_hash is null
          and original_event_hash is not null;
    end if;
end
$$;

alter table public.blockchain_records
    drop column if exists original_event_hash,
    drop column if exists hash_rebased_at;

alter table public.blockchain_records
    alter column retry_count set default 0,
    alter column retry_count set not null;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'blockchain_records_retry_count_check'
          and conrelid = 'public.blockchain_records'::regclass
    ) then
        alter table public.blockchain_records
            add constraint blockchain_records_retry_count_check
            check (retry_count >= 0);
    end if;
end
$$;

commit;
