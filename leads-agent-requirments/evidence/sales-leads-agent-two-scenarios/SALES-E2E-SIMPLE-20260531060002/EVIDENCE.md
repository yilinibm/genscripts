# Simple one-customer multi-turn scenario Evidence

- Status: PASS
- Run ID: `SALES-E2E-SIMPLE-20260531060002`
- Session ID: `e3da8860-cd78-4b41-84d0-b90b03e7d9dc`
- Counts: `{'leads': 1, 'emails': 3, 'tasks': 2, 'activities': 1}`
- Product code: `CPBU_ANTIBODIES_RUO_ANTIBODIES_PRIMARY_ANTIBODIES`

## Timeline

| Time | Actor | Action | Result |
|---|---|---|---|
| 2026-05-31T06:00:21.813077Z | orchestrator | cleanup | {'backend': 'cleanup+product initialize', 'mailboxes': {'gs_sales_01@163.com': {'deletedCount': 3, 'deletedSample': [{'folder': 'INBOX', 'uid': '1780044767', 'messageId': '<178020687756.65065.4153599996649498159.SALES-E2E-SIMPLE-20260531055423.customer-a-inquiry@leads-e2e.local>'}, {'folder': 'INBOX', 'uid': '1780044768', 'messageId': '<178020701231.65065.5431423603080176203.SALES-E2E-SIMPLE-20260531055423.customer-a-followup@leads-e2e.local>'}, {'folder': '已发送', 'uid': '1780107709', 'messageId': '<178020698417.65065.7047721400208021334.SALES-E2E-SIMPLE-20260531055423.sales-reply-a@leads-e2e.local>'}]}, 'gs_customer_01@163.com': {'deletedCount': 3, 'deletedSample': [{'folder': 'INBOX', 'uid': '1780044839', 'messageId': '<178020698417.65065.7047721400208021334.SALES-E2E-SIMPLE-20260531055423.sales-reply-a@leads-e2e.local>'}, {'folder': '已发送', 'uid': '1780045521', 'messageId': '<178020687756.65065.4153599996649498159.SALES-E2E-SIMPLE-20260531055423.customer-a-inquiry@leads-e2e.local>'}, {'folder': '已发送', 'uid': '1780045522', 'messageId': '<178020701231.65065.5431423603080176203.SALES-E2E-SIMPLE-20260531055423.customer-a-followup@leads-e2e.local>'}]}, 'gs_customer_02@163.com': {'deletedCount': 0, 'deletedSample': []}}} |
| 2026-05-31T06:00:26.802497Z | gs_customer_01@163.com | CUSTOMER-A-INQUIRY | delivered |
| 2026-05-31T06:01:02.145992Z | Sales Leads Agent | 请检查销售邮箱上下文，处理新的客户咨询邮件，并在需要时创建 lead 与 follow-up task。 | {'leads': 1, 'emails': 1, 'tasks': 1, 'activities': 0} |
| 2026-05-31T06:01:07.443715Z | gs_sales_01@163.com | SALES-REPLY-A | delivered |
| 2026-05-31T06:01:26.699435Z | Sales Leads Agent | 请检查销售最近已发送邮件，把销售对客户的业务跟进同步成 activity。不要发送邮件。 | {'leads': 1, 'emails': 2, 'tasks': 1, 'activities': 1} |
| 2026-05-31T06:01:31.584650Z | gs_customer_01@163.com | CUSTOMER-A-FOLLOWUP | delivered |
| 2026-05-31T06:02:01.801391Z | Sales Leads Agent | 请检查销售邮箱上下文，处理客户新的追问，并在需要时创建 follow-up task。 | {'leads': 1, 'emails': 3, 'tasks': 2, 'activities': 1} |
