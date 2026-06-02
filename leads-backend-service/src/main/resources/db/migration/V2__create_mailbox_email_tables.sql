create table mailbox_email (
    id uuid primary key,
    provider varchar(64) not null,
    mailbox varchar(320) not null,
    folder varchar(128) not null,
    provider_uid varchar(128),
    message_id varchar(512),
    dedupe_key varchar(1024) not null unique,
    direction varchar(32) not null,
    from_email varchar(320) not null,
    from_name varchar(256),
    to_emails jsonb not null default '[]'::jsonb,
    cc_emails jsonb not null default '[]'::jsonb,
    subject text,
    body_text text,
    snippet text,
    sent_at timestamptz,
    received_at timestamptz,
    synced_at timestamptz not null default now(),
    processing_status varchar(64) not null default 'PENDING',
    processed_at timestamptz,
    processing_reason text,
    email_message_id uuid references email_message(id),
    raw_payload jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_mailbox_email_pending on mailbox_email(mailbox, processing_status, direction, coalesce(received_at, sent_at) desc);
create index idx_mailbox_email_message_id on mailbox_email(message_id);
create index idx_mailbox_email_uid on mailbox_email(mailbox, folder, provider_uid);
create index idx_mailbox_email_email_message on mailbox_email(email_message_id);
create index idx_mailbox_email_to_gin on mailbox_email using gin (to_emails);

create table follow_up_task_mailbox_email (
    follow_up_task_id uuid primary key references follow_up_task(id) on delete cascade,
    mailbox_email_id uuid not null references mailbox_email(id) on delete cascade,
    lead_id uuid not null references lead_record(id) on delete cascade,
    task_type varchar(64) not null,
    created_at timestamptz not null default now(),
    constraint uk_task_mailbox_email unique (lead_id, mailbox_email_id, task_type)
);

create table sales_activity_mailbox_email (
    sales_activity_id uuid primary key references sales_activity(id) on delete cascade,
    mailbox_email_id uuid not null references mailbox_email(id) on delete cascade,
    lead_id uuid not null references lead_record(id) on delete cascade,
    activity_type varchar(64) not null,
    created_at timestamptz not null default now(),
    constraint uk_activity_mailbox_email unique (lead_id, mailbox_email_id, activity_type)
);
