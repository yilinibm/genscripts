# Complex two-customer multi-turn scenario Evidence

- Status: PASS
- Run ID: `SALES-E2E-COMPLEX-20260531060204`
- Session ID: `759b5db3-23f0-4b90-b270-8b7ad37bb459`
- Counts: `{'leads': 2, 'emails': 6, 'tasks': 4, 'activities': 2}`
- Product code: `CPBU_ANTIBODIES_RUO_ANTIBODIES_PRIMARY_ANTIBODIES`

## Timeline

| Time | Actor | Action | Result |
|---|---|---|---|
| 2026-05-31T06:02:23.206023Z | orchestrator | cleanup | {'backend': 'cleanup+product initialize', 'mailboxes': {'gs_sales_01@163.com': {'deletedCount': 3, 'deletedSample': [{'folder': 'INBOX', 'uid': '1780044769', 'messageId': '<178020722189.67985.910683473170934424.SALES-E2E-SIMPLE-20260531060002.customer-a-inquiry@leads-e2e.local>'}, {'folder': 'INBOX', 'uid': '1780044770', 'messageId': '<178020728670.67985.11873386422533946234.SALES-E2E-SIMPLE-20260531060002.customer-a-followup@leads-e2e.local>'}, {'folder': '已发送', 'uid': '1780107710', 'messageId': '<178020726214.67985.6703318543589357489.SALES-E2E-SIMPLE-20260531060002.sales-reply-a@leads-e2e.local>'}]}, 'gs_customer_01@163.com': {'deletedCount': 3, 'deletedSample': [{'folder': 'INBOX', 'uid': '1780044840', 'messageId': '<178020726214.67985.6703318543589357489.SALES-E2E-SIMPLE-20260531060002.sales-reply-a@leads-e2e.local>'}, {'folder': '已发送', 'uid': '1780045523', 'messageId': '<178020722189.67985.910683473170934424.SALES-E2E-SIMPLE-20260531060002.customer-a-inquiry@leads-e2e.local>'}, {'folder': '已发送', 'uid': '1780045524', 'messageId': '<178020728670.67985.11873386422533946234.SALES-E2E-SIMPLE-20260531060002.customer-a-followup@leads-e2e.local>'}]}, 'gs_customer_02@163.com': {'deletedCount': 0, 'deletedSample': []}}} |
| 2026-05-31T06:02:28.051219Z | gs_customer_01@163.com | CUSTOMER-A-INQUIRY | delivered |
| 2026-05-31T06:02:32.942739Z | gs_customer_02@163.com | CUSTOMER-B-INQUIRY | delivered |
| 2026-05-31T06:03:28.575823Z | Sales Leads Agent | 请检查销售邮箱上下文，处理新的客户咨询邮件，并在需要时创建 lead 与 follow-up task。 | {'leads': 2, 'emails': 2, 'tasks': 2, 'activities': 0} |
| 2026-05-31T06:03:33.505908Z | gs_sales_01@163.com | SALES-REPLY-A | delivered |
| 2026-05-31T06:03:39.110170Z | gs_sales_01@163.com | SALES-REPLY-B | delivered |
| 2026-05-31T06:04:14.077789Z | Sales Leads Agent | 请检查销售最近已发送邮件，把销售对客户的业务跟进同步成 activity。不要发送邮件。 | {'leads': 2, 'emails': 4, 'tasks': 2, 'activities': 2} |
| 2026-05-31T06:04:19.410655Z | gs_customer_01@163.com | CUSTOMER-A-FOLLOWUP | delivered |
| 2026-05-31T06:04:24.372588Z | gs_customer_02@163.com | CUSTOMER-B-FOLLOWUP | delivered |
| 2026-05-31T06:05:16.678061Z | Sales Leads Agent | 请检查销售邮箱上下文，处理客户新的追问，并在需要时创建 follow-up task。 | {'leads': 2, 'emails': 6, 'tasks': 4, 'activities': 2} |
