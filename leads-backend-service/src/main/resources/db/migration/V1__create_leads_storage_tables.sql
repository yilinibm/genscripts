create extension if not exists pgcrypto;

create table product_bundle (
    id uuid primary key,
    code varchar(128) not null unique,
    path_en varchar(512) not null,
    path_cn varchar(512) not null,
    business_unit varchar(64) not null,
    category_level1 varchar(128),
    category_level2 varchar(128),
    category_level3 varchar(128),
    synonyms jsonb not null default '[]'::jsonb,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_product_bundle_path_en on product_bundle(path_en);
create index idx_product_bundle_synonyms_gin on product_bundle using gin (synonyms);

create table lead_record (
    id uuid primary key,
    lead_no varchar(64) not null unique,
    customer_email varchar(320) not null,
    customer_email_normalized varchar(320) not null,
    customer_name varchar(256),
    company varchar(256),
    title varchar(256),
    phone varchar(64),
    product_bundle_id uuid not null references product_bundle(id),
    lead_unique_key varchar(1024) not null,
    owner_sales_email varchar(320) not null,
    status varchar(64) not null,
    intent_level varchar(32),
    current_stage varchar(64),
    timeline_trend varchar(64),
    latest_timeline_summary_id uuid,
    source varchar(64) not null default 'EMAIL',
    inquiry_summary text,
    extracted_requirements jsonb not null default '{}'::jsonb,
    first_email_id uuid,
    latest_email_at timestamptz,
    last_customer_email_at timestamptz,
    last_sales_activity_at timestamptz,
    created_by varchar(64) not null default 'LEADS_AGENT',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    version bigint not null default 0,
    constraint uk_lead_customer_product unique (customer_email_normalized, product_bundle_id)
);

create index idx_lead_owner_status on lead_record(owner_sales_email, status);
create index idx_lead_owner_stage_trend on lead_record(owner_sales_email, current_stage, timeline_trend);
create index idx_lead_customer_email on lead_record(customer_email_normalized);
create index idx_lead_product_bundle on lead_record(product_bundle_id);
create index idx_lead_latest_email_at on lead_record(latest_email_at desc);
create index idx_lead_requirements_gin on lead_record using gin (extracted_requirements);

create table email_message (
    id uuid primary key,
    provider_email_id varchar(256) not null,
    mailbox varchar(320) not null,
    thread_id varchar(256),
    direction varchar(32) not null,
    from_email varchar(320) not null,
    from_name varchar(256),
    to_emails jsonb not null default '[]'::jsonb,
    cc_emails jsonb not null default '[]'::jsonb,
    subject text,
    body_text text,
    body_html_ref text,
    snippet text,
    attachment_refs jsonb not null default '[]'::jsonb,
    sent_at timestamptz,
    received_at timestamptz,
    processed_at timestamptz,
    email_status varchar(64),
    raw_storage_ref text,
    created_at timestamptz not null default now(),
    constraint uk_email_provider unique (provider_email_id)
);

create index idx_email_thread on email_message(thread_id);
create index idx_email_direction_time on email_message(direction, coalesce(received_at, sent_at));
create index idx_email_from on email_message(from_email);
create index idx_email_to_gin on email_message using gin (to_emails);

create table lead_email (
    id uuid primary key,
    lead_id uuid not null references lead_record(id) on delete cascade,
    email_message_id uuid not null references email_message(id) on delete cascade,
    relation_type varchar(64) not null,
    match_confidence numeric(5,4),
    match_reason text,
    created_at timestamptz not null default now(),
    constraint uk_lead_email unique (lead_id, email_message_id)
);

create index idx_lead_email_lead on lead_email(lead_id);
create index idx_lead_email_message on lead_email(email_message_id);

create table follow_up_task (
    id uuid primary key,
    task_no varchar(64) not null unique,
    lead_id uuid not null references lead_record(id) on delete cascade,
    source_email_id uuid references email_message(id),
    assigned_sales_email varchar(320) not null,
    status varchar(64) not null,
    task_type varchar(64) not null,
    priority varchar(32) not null default 'NORMAL',
    title varchar(512) not null,
    summary text,
    reason text,
    suggested_action text,
    display_summary varchar(512),
    customer_need_summary text,
    source_event_summary text,
    action_items jsonb not null default '[]'::jsonb,
    context_snapshot jsonb not null default '{}'::jsonb,
    priority_reason text,
    due_at timestamptz,
    accepted_at timestamptz,
    completed_at timestamptz,
    dismissed_at timestamptz,
    close_reason text,
    created_by varchar(64) not null default 'LEADS_AGENT',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    version bigint not null default 0
);

create index idx_task_sales_status_due on follow_up_task(assigned_sales_email, status, due_at);
create index idx_task_lead on follow_up_task(lead_id);
create index idx_task_source_email on follow_up_task(source_email_id);

create table sales_activity (
    id uuid primary key,
    activity_no varchar(64) not null unique,
    lead_id uuid not null references lead_record(id) on delete cascade,
    source_email_id uuid references email_message(id),
    related_task_id uuid references follow_up_task(id),
    sales_email varchar(320) not null,
    activity_type varchar(64) not null,
    occurred_at timestamptz not null,
    title varchar(512),
    summary text not null,
    key_points jsonb not null default '[]'::jsonb,
    customer_signals jsonb not null default '[]'::jsonb,
    next_step_signals jsonb not null default '[]'::jsonb,
    progress_signal varchar(32) not null default 'UNKNOWN',
    progress_reason text,
    stage_after_activity varchar(64),
    extracted_payload jsonb not null default '{}'::jsonb,
    confidence numeric(5,4),
    created_by varchar(64) not null default 'LEADS_AGENT',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_activity_lead_time on sales_activity(lead_id, occurred_at desc);
create index idx_activity_sales_time on sales_activity(sales_email, occurred_at desc);
create index idx_activity_progress on sales_activity(lead_id, progress_signal, occurred_at desc);
create index idx_activity_source_email on sales_activity(source_email_id);
create index idx_activity_payload_gin on sales_activity using gin (extracted_payload);

create table lead_activity_summary (
    id uuid primary key,
    lead_id uuid not null references lead_record(id) on delete cascade,
    summary_window varchar(32) not null,
    window_start_at timestamptz not null,
    window_end_at timestamptz not null,
    overall_summary text not null,
    customer_intent varchar(64) not null default 'UNCLEAR',
    current_stage varchar(64) not null,
    trend varchar(64) not null,
    trend_reason text,
    progress_activity_count integer not null default 0,
    no_progress_activity_count integer not null default 0,
    last_progress_at timestamptz,
    last_activity_at timestamptz,
    next_recommended_action text,
    source_activity_ids jsonb not null default '[]'::jsonb,
    confidence numeric(5,4),
    generated_by varchar(64) not null default 'LEADS_AGENT',
    generated_at timestamptz not null default now(),
    constraint uk_lead_activity_summary_window unique (lead_id, summary_window, window_end_at)
);

create index idx_lead_activity_summary_lead_time on lead_activity_summary(lead_id, generated_at desc);
create index idx_lead_activity_summary_stage_trend on lead_activity_summary(current_stage, trend);

alter table lead_record
    add constraint fk_lead_latest_summary
    foreign key (latest_timeline_summary_id) references lead_activity_summary(id);

create table agent_processing_log (
    id uuid primary key,
    source_type varchar(64) not null,
    source_id varchar(256) not null,
    action varchar(128) not null,
    status varchar(64) not null,
    request_payload jsonb,
    result_payload jsonb,
    error_message text,
    started_at timestamptz not null default now(),
    finished_at timestamptz,
    constraint uk_agent_processing unique (source_type, source_id, action)
);

create index idx_agent_log_status on agent_processing_log(status, started_at desc);
