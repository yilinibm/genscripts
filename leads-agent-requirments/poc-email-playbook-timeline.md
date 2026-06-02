# POC 邮件往来时间轴

本文档按真实 E2E 证据中的邮件时间顺序，整理 POC 中模拟的客户与销售邮件内容。邮件均来自归档证据 `mailbox-records.json`，用于说明 Sales Leads Agent 观察邮箱上下文时面对的真实输入。

关键说明：Agent 不给客户发邮件；客户邮件由 customer 邮箱真实发出，销售回复由 sales 邮箱真实发出，Agent 只读取邮箱并在后端生成 lead、follow-up task、sales activity。

## 单客户多轮场景

- Run ID: `SALES-E2E-SIMPLE-20260531060002`
- 证据来源：[evidence/sales-leads-agent-two-scenarios/SALES-E2E-SIMPLE-20260531060002/mailbox-records.json](./evidence/sales-leads-agent-two-scenarios/SALES-E2E-SIMPLE-20260531060002/mailbox-records.json)

| 时间 UTC | 方向 | 发件人 | 收件人 | 标题 |
|---|---|---|---|---|
| 2026-05-31T06:00:21Z | 客户 -> 销售 | Alex Chen <gs_customer_01@163.com> | Hui Li <gs_sales_01@163.com> | [SALES-E2E-SIMPLE-20260531060002] 一抗产品咨询：WB 和 IHC 验证 |
| 2026-05-31T06:01:02Z | 销售 -> 客户 | Hui Li <gs_sales_01@163.com> | Alex Chen <gs_customer_01@163.com> | [SALES-E2E-SIMPLE-20260531060002] Re: 一抗产品咨询：WB 和 IHC 验证 |
| 2026-05-31T06:01:26Z | 客户 -> 销售 | Alex Chen <gs_customer_01@163.com> | Hui Li <gs_sales_01@163.com> | [SALES-E2E-SIMPLE-20260531060002] Re: 一抗产品咨询：WB 和 IHC 验证 |

### 1. 客户 -> 销售

- 时间：`2026-05-31T06:00:21Z`
- 发件人：`Alex Chen <gs_customer_01@163.com>`
- 收件人：`Hui Li <gs_sales_01@163.com>`
- 标题：`[SALES-E2E-SIMPLE-20260531060002] 一抗产品咨询：WB 和 IHC 验证`
- Message-ID：`<178020722189.67985.910683473170934424.SALES-E2E-SIMPLE-20260531060002.customer-a-inquiry@leads-e2e.local>`

正文：

```text
[SALES-E2E-SIMPLE-20260531060002]
Step: CUSTOMER-A-INQUIRY

Hui 你好，

我们实验室正在评估科研用一抗产品，主要用于人源样本 Western blot，后续也希望做 IHC 验证。请推荐合适的一抗产品，并提供 datasheet、验证数据、现货情况、交期和报价。

谢谢，
Alex Chen
```

### 2. 销售 -> 客户

- 时间：`2026-05-31T06:01:02Z`
- 发件人：`Hui Li <gs_sales_01@163.com>`
- 收件人：`Alex Chen <gs_customer_01@163.com>`
- 标题：`[SALES-E2E-SIMPLE-20260531060002] Re: 一抗产品咨询：WB 和 IHC 验证`
- Message-ID：`<178020726214.67985.6703318543589357489.SALES-E2E-SIMPLE-20260531060002.sales-reply-a@leads-e2e.local>`

正文：

```text
[SALES-E2E-SIMPLE-20260531060002]
Step: SALES-REPLY-A

Alex 你好，

感谢咨询 GenScript 科研用一抗产品。我们会优先筛选适合 WB、同时具备 IHC 验证信息的一抗候选产品。请补充目标蛋白名称、样本物种和样本类型，我会整理 datasheet、库存、交期和报价。

谢谢，
Hui Li
```

### 3. 客户 -> 销售

- 时间：`2026-05-31T06:01:26Z`
- 发件人：`Alex Chen <gs_customer_01@163.com>`
- 收件人：`Hui Li <gs_sales_01@163.com>`
- 标题：`[SALES-E2E-SIMPLE-20260531060002] Re: 一抗产品咨询：WB 和 IHC 验证`
- Message-ID：`<178020728670.67985.11873386422533946234.SALES-E2E-SIMPLE-20260531060002.customer-a-followup@leads-e2e.local>`

正文：

```text
[SALES-E2E-SIMPLE-20260531060002]
Step: CUSTOMER-A-FOLLOWUP

Hui 你好，

补充一下：目标是人源肿瘤相关 marker，样本是细胞裂解液。请优先推荐 2 个科研用一抗候选产品，并附 datasheet、推荐稀释比例、验证依据、库存、交期和报价。

谢谢，
Alex
```

## 双客户多轮复杂场景

- Run ID: `SALES-E2E-COMPLEX-20260531060204`
- 证据来源：[evidence/sales-leads-agent-two-scenarios/SALES-E2E-COMPLEX-20260531060204/mailbox-records.json](./evidence/sales-leads-agent-two-scenarios/SALES-E2E-COMPLEX-20260531060204/mailbox-records.json)

| 时间 UTC | 方向 | 发件人 | 收件人 | 标题 |
|---|---|---|---|---|
| 2026-05-31T06:02:23Z | 客户 -> 销售 | Alex Chen <gs_customer_01@163.com> | Hui Li <gs_sales_01@163.com> | [SALES-E2E-COMPLEX-20260531060204] 一抗产品咨询：WB 和 IHC 验证 |
| 2026-05-31T06:02:28Z | 客户 -> 销售 | Morgan Lee <gs_customer_02@163.com> | Hui Li <gs_sales_01@163.com> | [SALES-E2E-COMPLEX-20260531060204] 一抗产品咨询：IF 染色筛选 |
| 2026-05-31T06:03:28Z | 销售 -> 客户 | Hui Li <gs_sales_01@163.com> | Alex Chen <gs_customer_01@163.com> | [SALES-E2E-COMPLEX-20260531060204] Re: 一抗产品咨询：WB 和 IHC 验证 |
| 2026-05-31T06:03:33Z | 销售 -> 客户 | Hui Li <gs_sales_01@163.com> | Morgan Lee <gs_customer_02@163.com> | [SALES-E2E-COMPLEX-20260531060204] Re: 一抗产品咨询：IF 染色筛选 |
| 2026-05-31T06:04:14Z | 客户 -> 销售 | Alex Chen <gs_customer_01@163.com> | Hui Li <gs_sales_01@163.com> | [SALES-E2E-COMPLEX-20260531060204] Re: 一抗产品咨询：WB 和 IHC 验证 |
| 2026-05-31T06:04:19Z | 客户 -> 销售 | Morgan Lee <gs_customer_02@163.com> | Hui Li <gs_sales_01@163.com> | [SALES-E2E-COMPLEX-20260531060204] Re: 一抗产品咨询：IF 染色筛选 |

### 1. 客户 -> 销售

- 时间：`2026-05-31T06:02:23Z`
- 发件人：`Alex Chen <gs_customer_01@163.com>`
- 收件人：`Hui Li <gs_sales_01@163.com>`
- 标题：`[SALES-E2E-COMPLEX-20260531060204] 一抗产品咨询：WB 和 IHC 验证`
- Message-ID：`<178020734326.67985.5802677928728210702.SALES-E2E-COMPLEX-20260531060204.customer-a-inquiry@leads-e2e.local>`

正文：

```text
[SALES-E2E-COMPLEX-20260531060204]
Step: CUSTOMER-A-INQUIRY

Hui 你好，

我们实验室正在评估科研用一抗产品，主要用于人源样本 Western blot，后续也希望做 IHC 验证。请推荐合适的一抗产品，并提供 datasheet、验证数据、现货情况、交期和报价。

谢谢，
Alex Chen
```

### 2. 客户 -> 销售

- 时间：`2026-05-31T06:02:28Z`
- 发件人：`Morgan Lee <gs_customer_02@163.com>`
- 收件人：`Hui Li <gs_sales_01@163.com>`
- 标题：`[SALES-E2E-COMPLEX-20260531060204] 一抗产品咨询：IF 染色筛选`
- Message-ID：`<178020734805.67985.12329721343477162594.SALES-E2E-COMPLEX-20260531060204.customer-b-inquiry@leads-e2e.local>`

正文：

```text
[SALES-E2E-COMPLEX-20260531060204]
Step: CUSTOMER-B-INQUIRY

Hui 你好，

我们正在为细胞免疫荧光实验筛选科研用一抗产品，样本是培养的人源细胞。请推荐适合 IF 应用的一抗产品，并提供 datasheet、验证图片、推荐稀释比例、现货情况、交期和报价。

谢谢，
Morgan Lee
```

### 3. 销售 -> 客户

- 时间：`2026-05-31T06:03:28Z`
- 发件人：`Hui Li <gs_sales_01@163.com>`
- 收件人：`Alex Chen <gs_customer_01@163.com>`
- 标题：`[SALES-E2E-COMPLEX-20260531060204] Re: 一抗产品咨询：WB 和 IHC 验证`
- Message-ID：`<178020740857.67985.1426169870740266615.SALES-E2E-COMPLEX-20260531060204.sales-reply-a@leads-e2e.local>`

正文：

```text
[SALES-E2E-COMPLEX-20260531060204]
Step: SALES-REPLY-A

Alex 你好，

感谢咨询。请补充目标蛋白名称、样本物种和样本类型，我们会整理 2 个 WB/IHC 一抗候选产品、datasheet、库存、交期和报价。

谢谢，
Hui Li
```

### 4. 销售 -> 客户

- 时间：`2026-05-31T06:03:33Z`
- 发件人：`Hui Li <gs_sales_01@163.com>`
- 收件人：`Morgan Lee <gs_customer_02@163.com>`
- 标题：`[SALES-E2E-COMPLEX-20260531060204] Re: 一抗产品咨询：IF 染色筛选`
- Message-ID：`<178020741350.67985.8768390984671638874.SALES-E2E-COMPLEX-20260531060204.sales-reply-b@leads-e2e.local>`

正文：

```text
[SALES-E2E-COMPLEX-20260531060204]
Step: SALES-REPLY-B

Morgan 你好，

感谢咨询。针对 IF 染色筛选，请补充目标 marker 和细胞类型，我们会整理 3 个 IF 一抗候选产品、验证图片、库存、交期和报价。

谢谢，
Hui Li
```

### 5. 客户 -> 销售

- 时间：`2026-05-31T06:04:14Z`
- 发件人：`Alex Chen <gs_customer_01@163.com>`
- 收件人：`Hui Li <gs_sales_01@163.com>`
- 标题：`[SALES-E2E-COMPLEX-20260531060204] Re: 一抗产品咨询：WB 和 IHC 验证`
- Message-ID：`<178020745407.67985.424166695196856758.SALES-E2E-COMPLEX-20260531060204.customer-a-followup@leads-e2e.local>`

正文：

```text
[SALES-E2E-COMPLEX-20260531060204]
Step: CUSTOMER-A-FOLLOWUP

Hui 你好，

补充：目标是人源肿瘤相关 marker，样本是细胞裂解液。请优先推荐 2 个科研用一抗候选产品，并附 datasheet、推荐稀释比例、验证依据、库存、交期和报价。

谢谢，
Alex
```

### 6. 客户 -> 销售

- 时间：`2026-05-31T06:04:19Z`
- 发件人：`Morgan Lee <gs_customer_02@163.com>`
- 收件人：`Hui Li <gs_sales_01@163.com>`
- 标题：`[SALES-E2E-COMPLEX-20260531060204] Re: 一抗产品咨询：IF 染色筛选`
- Message-ID：`<178020745941.67985.10601322670607138162.SALES-E2E-COMPLEX-20260531060204.customer-b-followup@leads-e2e.local>`

正文：

```text
[SALES-E2E-COMPLEX-20260531060204]
Step: CUSTOMER-B-FOLLOWUP

Hui 你好，

补充：样本是固定后的人源培养细胞，应用只做 IF。请提供 3 个科研用一抗候选产品的 datasheet、IF 验证图片、推荐稀释比例、库存状态、交期和报价。

谢谢，
Morgan
```
