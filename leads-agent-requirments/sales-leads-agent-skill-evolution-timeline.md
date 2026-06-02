# Sales Leads Agent Skill 设计、更新与 Bugfix 时间线

本文档梳理 Sales Leads Agent 在 POC 场景中的 skill 设计演进过程。重点不是列代码变更，而是记录我们如何从“固定剧本验证”逐步收敛到“通用、可配置、有围栏、可观测、可复盘”的 Sales Leads Agent。

## 1. 初始阶段：Daniel/E.coli 专用验证

### 目标

最初的目标是证明 Agent 能把真实邮箱里的客户咨询沉淀到 Leads 后端。验证对象是 Daniel 咨询 E.coli protein expression package 的固定故事线。

### 当时的处理方式

- 邮件剧本固定为 D1-D9。
- 产品和客户场景高度固定。
- 后端写入主要依赖旧的业务 skills：
  - `create_lead_skill`
  - `create_follow_up_task_skill`
  - `create_activity_skill`
- 真实邮件由 customer/sales 邮箱发送，脚本采集邮箱与后端证据。

### 暴露的问题

- 场景过于专用，容易被理解成 Daniel/E.coli hardcode。
- 脚本直接调 skill 的路径太强，不能证明 Agent 自主调度能力。
- Agent 是否真正“服务 sales”不够清晰，容易被误解为 Agent 代替销售发邮件。

### 形成的经验

第一轮验证证明真实邮箱、后端写入、证据采集可行，但也明确了下一步方向：必须保留旧 agent 和旧 skills，新建一套通用 Sales Leads Agent，并让 Agent 通过 chat 自主调度 skills。

## 2. 通用 Sales Leads Agent 阶段：新 Agent、新业务 Skills

### 目标

把能力从固定 Daniel/E.coli 场景扩展为通用 sales leads assistant。Agent 负责理解客户邮件内容、选择商品类目、判断 lead/task/activity；skill 负责校验和写入。

### 新增的通用业务 Skills

| Skill | 类型 | 设计职责 |
| --- | --- | --- |
| `sales_lead_create_or_update_skill` | 写入型 | 处理客户入站邮件，校验商品类目，写入 lead、email、lead-email |
| `sales_follow_up_task_create_skill` | 写入型 | 根据 Agent 生成的任务标题、原因、建议动作写入 follow-up task |
| `sales_activity_create_skill` | 写入型 | 根据销售已发送邮件写入 outbound email 和 sales activity |

### 关键设计取舍

- 不修改旧 `Leads Agent` 和旧 Daniel/E.coli skills。
- 新 skill 不复用旧名字，避免覆盖旧能力。
- Skill 不做产品推断，只校验 Agent 传入的 `productBundleCode` 是否存在。
- Lead 去重基于 `customerEmailNormalized + productBundleCode`。
- Agent 是业务决策 owner，后端是 storage/context service。

### 暴露的问题

这个阶段虽然通用化了 skill 名称和数据模型，但早期 E2E prompt 仍然像“遥控 Agent”：

- prompt 里直接告诉 Agent `runId`、Step、productCode、uid、leadId、emailId。
- Agent 更像按测试脚本指令执行，而不是自主观察邮箱上下文。
- 用户期望的是一句简单指令：“检查是否有新线索、看看是否需要 follow-up task、帮 sales 生成 activity。”

### 形成的经验

通用 skill 只是第一步。真正的通用 Agent 还需要只读上下文 skills，让 Agent 能自己观察和判断，而不是依赖测试脚本传入所有答案。

## 3. 自主决策阶段：增加观察型 Skills

### 目标

让用户不再提供 runId、uid、productCode、leadId、emailId。Agent 只接收自然语言指令，自己读取邮箱和后端上下文，判断是否创建 lead、task 或 activity。

### 新增的观察型 Skills

| Skill | 类型 | 设计职责 |
| --- | --- | --- |
| `mail_163_recent_scan_skill` | 邮箱只读 | 扫描 `INBOX` 或 `已发送/Sent` 最近邮件，不要求用户提供 UID |
| `sales_product_bundle_lookup_skill` | 商品只读 | 基于邮件主题/正文查询商品类目候选 |
| `sales_lead_context_lookup_skill` | 业务上下文只读 | 按客户、商品、主题查询已有 lead、open tasks、recent activities |

### Agent Prompt 的核心变化

Agent prompt 被改造成“观察上下文 -> 分类邮件 -> 查询上下文 -> 决策写入”的流程：

- 检查客户咨询时，先读 `INBOX`，再识别是否客户入站邮件。
- 识别产品时，必须调用商品类目查询 skill。
- 写入前，先调用 lead context 查询 skill 判断是否已有 lead。
- 处理销售活动时，扫描销售 `已发送/Sent`，匹配已有 lead 后只创建 activity。
- 普通检查流程禁止调用 `mail_163_sender`。

### 暴露的问题

- 如果只读 unread，客户追问邮件可能因为已读状态而漏处理。
- 如果同时处理 “新咨询 + activity”，Agent 可能把已发送邮件和客户追问混在一轮里。
- 如果产品识别完全靠模型，中文商品名容易误判。

### 形成的经验

自主决策不能等于“让模型自由发挥”。必须给 Agent 配齐观察型 skills，同时通过 prompt 固化决策顺序，并在写入型 skills 中继续做围栏。

## 4. 商品类目 Reference 阶段：降低产品识别不稳定

### 目标

解决商品识别依赖模型自由判断的问题，让 Agent 在后端已有商品类目中选择，而不是凭空生成产品路径。

### Skill 更新

`sales_product_bundle_lookup_skill` 增强为：

- 内置 `references/product-bundles.md` 商品类目参考表。
- 先用 reference 做中文/英文关键词匹配。
- 再兜底调用 Leads 后端商品类目搜索接口。
- 返回候选的 `code/pathEn/pathCn/synonyms/score/reason`，由 Agent 最终选择。

### 解决的问题类别

- 商品名中英文混杂导致误判。
- 用户咨询中只出现应用场景，没有标准产品 code。
- 模型可能输出后端不存在的类目。

### 形成的经验

业务知识不应只写在 prompt 里。稳定的领域知识要沉淀为 reference，由 skill 查询和返回候选，Agent 在可控候选集合里决策。

## 5. 邮件方向围栏阶段：避免销售邮件被误处理成客户线索

### 触发背景

在双客户同类目测试中，出现过两个关键问题：

- 销售已发送邮件被误处理为 customer inbound，导致多建 lead/task。
- 创建 activity 时因收件人字段不一致导致少生成 activity。

### Skill 与 Prompt 更新

Agent prompt 增加明确方向规则：

- `INBOX` 只处理“发件人不是 sales mailbox、收件人包含 sales mailbox”的客户邮件。
- `已发送/Sent` 只处理“发件人是 sales mailbox、收件人不是 sales mailbox”的销售跟进邮件。
- 处理 `已发送/Sent` 时禁止调用 lead/task 写入 skill。
- 客户入站邮件不能创建 activity，销售出站邮件不能创建 task。

写入型 skills 增加防御：

- `sales_lead_create_or_update_skill` 动态解析 sales mailbox，发现销售发件则拒绝创建 lead。
- `sales_follow_up_task_create_skill` 校验 source email 方向，只允许 inbound customer email 创建 task。
- `sales_activity_create_skill` 校验 from 必须是 sales mailbox，且能解析客户收件人。

### 形成的经验

邮件方向是 sales agent 的核心安全边界，不能只靠 prompt。必须在 prompt 和 skill 两层同时建立围栏：prompt 引导 Agent，skill 拒绝越界写入。

## 6. 幂等与重复扫描阶段：让 Agent 可以反复检查上下文

### 触发背景

真实邮箱测试中，Agent 可能多次扫描同一封邮件：

- 同一封客户邮件可能出现在 unread scan 和 recent scan。
- 销售已发送邮件可能在多次 activity 同步中被重复看到。
- 客户追问邮件可能不是 unread，需要 recent scan fallback。

### Skill 更新

- `sales_lead_create_or_update_skill` 使用 provider email/messageId/uid 等稳定 key 写入 email。
- `sales_follow_up_task_create_skill` 的幂等维度加入 source email、subject、body 摘要，避免任务被覆盖或重复。
- `sales_activity_create_skill` 基于 outbound source email 做 activity 幂等。
- `mail_163_recent_scan_skill` 支持 `sinceMinutes`、`max_fetch`、`include_body`，让 Agent 在受控窗口内扫描最近邮件。

### 形成的经验

Agent 的工作方式天然会“反复观察”。所以 skill 不能假设一次调用只发生一次，必须默认支持重复调用、重复扫描和重复上下文检查。

## 7. 动态配置阶段：避免邮箱和场景 Hardcode

### 触发背景

测试从一个客户扩展到两个真实客户邮箱后，需要证明流程不是绑定某个固定客户邮箱、固定商品、固定 runId。

### 更新内容

- Agent metadata/env 中提供 sales mailbox 上下文。
- Skills 通过 `mailbox / ownerSalesEmail / configuredSalesEmail / LEADS_SALES_EMAIL` 动态识别当前销售邮箱。
- 判断方向时基于 folder、from、to/cc 和当前 sales mailbox，而不是写死邮箱地址。
- E2E chat prompt 不再暴露 runId、uid、productCode、leadId、emailId。

### 形成的经验

测试中可以用 runId 做证据标签，但 Agent 的业务决策不能依赖 runId。业务逻辑必须基于真实上下文：谁发给谁、邮件在哪个 folder、客户是谁、商品是什么、后端已有记录是什么。

## 8. 输出与 Trace 阶段：让执行细节可视化、总结可读

### 触发背景

用户希望像“智能旅行规划助手”那样看到执行细节，同时最终总结不要输出生硬 JSON。

### 更新内容

Agent prompt 调整为：

- 执行细节交给系统 trace 面板展示。
- 最终回复只保留三段：
  - Leads：是否新建/更新/未新建，以及原因。
  - Follow-up task：是否创建，以及原因。
  - Activity：是否同步，以及原因。
- 不输出原始 JSON、长日志、skill 名称清单。

Trace 侧保留：

- Agent 开始/结束。
- 每个 skill 的开始/结束、输入、输出、耗时。
- 错误定位信息。

### 形成的经验

业务用户需要的是可读结论，工程验收需要的是可观察过程。两者应分层：前台总结简洁，后台 trace 完整。

## 9. E2E 证据化阶段：从“能跑”到“可证明”

### 目标

把 skill 设计和 Agent 决策能力用真实端到端证据证明出来。

### 新增和演进的验证脚本

| 脚本 | 目的 |
| --- | --- |
| `e2e_sales_leads_agent_general_flow.py` | 早期通用 Sales Leads Agent 验证，仍有较强脚本遥控 |
| `e2e_sales_leads_agent_autonomous_flow.py` | 验证自然语言输入下的自主处理 |
| `e2e_sales_leads_agent_deepseek_reference_flow.py` | 验证 DeepSeek V4 Pro 与商品 reference 后的稳定性 |
| `e2e_sales_leads_agent_two_scenarios.py` | 验证单客户多轮与双客户多轮两个核心场景 |
| `e2e_sales_leads_agent_three_rounds.py` | 连续三轮执行 simple + complex 场景，输出验证表单 |

### 证据产物

每轮保留：

- `mailbox-records.json`：真实邮箱收件箱/已发送邮件证据。
- `backend-records.json`：真实后端 lead/email/task/activity/timeline。
- `agent-chat-transcript.json`：Agent 输入、输出和 trace events。
- `EVIDENCE.md`：人可读执行时间线。
- `TEST_CASE.md`：邮件输入、Agent 输入、预期结果、真实结果、验证结论。
- `SUMMARY.md/json`：批次级汇总。

### 发现并修复的验证链路问题

三轮包装测试时发现：新 runId 前缀 `SALES-E2E-R...` 没有被清理函数覆盖，导致 simple 场景遗留的 sales sent 邮件污染 complex 场景，Agent 多同步一条 activity。

修复方式：

- 清理 token 增加 `SALES-E2E-R`。
- 保留失败 batch 作为问题证据。
- 重新跑三轮，最终 `3/3 PASS`。

### 形成的经验

E2E 脚本本身也需要幂等和隔离设计。测试 runId、邮箱清理、后端 cleanup、证据目录不能互相污染，否则会把测试框架问题误判为 Agent 问题。

## 10. 当前稳定形态

当前 Sales Leads Agent 的稳定设计可以概括为：

1. Agent 用自然语言被调度，不需要用户提供技术 ID。
2. Agent 先观察邮箱和后端上下文，再决策。
3. 只读 skills 提供上下文，写入 skills 执行业务落库。
4. Skill 层做方向校验、商品校验、幂等保护和错误返回。
5. 后端保持 storage/context service，不承载业务推断。
6. Trace 展示执行细节，最终回复只给业务结论。
7. Evidence 证明每次执行真实发生、数据真实落库、结果可复盘。

## 11. Skill 设计方法论沉淀

| 方法论 | 含义 |
| --- | --- |
| Context before write | 所有写入前先查询业务上下文，避免孤立判断 |
| Guardrails in skills | prompt 引导之外，skill 必须能拒绝越界动作 |
| Idempotency by default | Agent 可以重复检查，skill 不应重复污染数据 |
| Configuration over hardcode | sales mailbox、客户、商品都来自上下文和配置 |
| Reference over prompt memory | 稳定业务知识沉淀为 reference，可查询可版本化 |
| Model decides, skill verifies | 模型做业务理解，skill 做确定性校验和执行 |
| Evidence as deliverable | 证据文件是 POC 交付的一部分，不是调试副产物 |

## 12. 关联文档与证据

- POC 汇报：[leads-agent-poc-report.md](./leads-agent-poc-report.md)
- 邮件时间轴：[poc-email-playbook-timeline.md](./poc-email-playbook-timeline.md)
- 两场景归档证据：[evidence/sales-leads-agent-two-scenarios/SUMMARY.md](./evidence/sales-leads-agent-two-scenarios/SUMMARY.md)
- 三轮验证证据：`../evidence/sales-leads-agent-three-rounds/SALES-E2E-THREE-ROUNDS-20260531070231/SUMMARY.md`
