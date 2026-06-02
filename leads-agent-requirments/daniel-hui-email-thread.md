# Daniel 与 Hui 邮件往来测试数据：大肠杆菌蛋白表达组合线索

## 1. 用途说明

本文档用于 Leads Agent 测试。内容基于金斯瑞商品类目结构和当前用户故事线整理，模拟一个客户主动咨询 `RSBU > Protein > Protein Expression E.coli` 商品组合后，销售在 Lead 阶段持续跟进的完整邮件线程。

当前测试重点不是单纯识别客户邮箱，而是验证 Agent 是否能按照以下唯一标准创建和匹配线索：

```text
Lead Unique Key = 客户邮箱 + 商品组合
```

本故事线中的唯一线索判定：

```text
客户邮箱: gs_customer_01@163.com
商品组合: RSBU > Protein > Protein Expression E.coli
Lead Unique Key: gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli
```

测试约束：

- 只覆盖 Lead 线索阶段。
- 不进入 Opportunity。
- 只有一个销售：Hui。
- 销售邮箱：`gs_sales_01@163.com`
- 客户邮箱：`gs_customer_01@163.com`
- 商品组合：`RSBU > Protein > Protein Expression E.coli`
- 商品组合中文名：`RSBU > 蛋白 > 大肠杆菌蛋白表达组合`
- 邮件线程 ID：`THREAD-DANIEL-ECOLI-PROTEIN-001`

## 2. 商品组合定义

本线索对应的商品组合来自商品类目图中的 RSBU 业务线：

```text
RSBU
└── Protein（蛋白）
    └── Protein Expression E.coli（大肠杆菌蛋白表达）
        ├── E.coli - Protein（大肠杆菌蛋白表达）
        ├── E.coli - HTP（大肠杆菌高通量蛋白表达）
        └── E.coli Product（大肠杆菌蛋白产品）
```

Agent 应将客户邮件中的以下表达都归一到同一个商品组合：

- 大肠杆菌蛋白表达
- E.coli protein expression
- E.coli expression package
- E.coli HTP expression
- 大肠杆菌表达纯化
- E.coli recombinant protein

如果同一个客户邮箱后续咨询 `RSBU > mRNA`、`RSBU > Peptide` 或 `CPBU > Antibodies`，应创建不同 lead；如果仍围绕 `RSBU > Protein > Protein Expression E.coli` 补充序列、标签、表达量、纯化要求，则应匹配到本 lead。

## 3. 邮件索引

| Step | Email ID | 时间 | 方向 | 主题 | 业务含义 |
| --- | --- | --- | --- | --- | --- |
| D1 | EMAIL-DANIEL-001 | 2026-05-21 03:03 | Customer -> Sales | 大肠杆菌蛋白表达组合服务咨询 | 客户主动咨询 E.coli 蛋白表达组合，Agent 创建 lead |
| D2 | EMAIL-DANIEL-002 | 2026-05-21 05:30 | Customer -> Sales | Re: 大肠杆菌蛋白表达组合服务咨询 | 客户补充蛋白数量、标签和纯度要求 |
| D3 | EMAIL-DANIEL-003 | 2026-05-21 06:55 | Customer -> Sales | Re: 大肠杆菌蛋白表达组合服务咨询 | 客户确认已有序列并询问是否适合 E.coli 表达 |
| D4 | EMAIL-DANIEL-004 | 2026-05-21 09:20 | Sales -> Customer | Re: 大肠杆菌蛋白表达组合服务咨询 | 销售首次跟进并澄清表达、纯化和交付需求 |
| D5 | EMAIL-DANIEL-005 | 2026-05-22 02:15 | Customer -> Sales | Re: 大肠杆菌蛋白表达组合服务咨询 | 客户补充序列长度、标签、规模和期望 TAT |
| D6 | EMAIL-DANIEL-006 | 2026-05-22 05:40 | Sales -> Customer | Re: 大肠杆菌蛋白表达组合服务咨询 | 销售确认商品组合和下一步资料需求 |
| D7 | EMAIL-DANIEL-007 | 2026-05-23 01:10 | Customer -> Sales | Re: 大肠杆菌蛋白表达组合服务咨询 | 客户要求报价前说明样本交付形式和风险 |
| D8 | EMAIL-DANIEL-008 | 2026-05-23 04:35 | Sales -> Customer | Re: 大肠杆菌蛋白表达组合服务咨询 | 销售发送 E.coli 表达组合说明和会议时间 |
| D9 | EMAIL-DANIEL-009 | 2026-05-24 02:05 | Customer -> Sales | Re: 大肠杆菌蛋白表达组合服务咨询 | 客户选择会议时间，lead 到 Ready for Sales Review |

## 4. 邮件详情

### D1 - 客户主动咨询商品组合

```text
Email ID: EMAIL-DANIEL-001
Thread ID: THREAD-DANIEL-ECOLI-PROTEIN-001
Direction: Customer -> Sales
From: Daniel Villarreal <gs_customer_01@163.com>
To: Hui Li <gs_sales_01@163.com>
Date: 2026-05-21 03:03
Subject: 大肠杆菌蛋白表达组合服务咨询
Attachment: None
Lead Stage After Email: New Lead / Discovery
Lead Unique Key: gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli
```

Hui 你好，

我们正在评估一个重组蛋白项目，想咨询贵司是否可以提供大肠杆菌蛋白表达组合服务。

目前我们有 3 个候选蛋白，计划先做小规模表达和纯化评估，如果表达情况合适，后续可能会放大制备用于体外功能实验。

初步需求如下：

| 项目 | 当前信息 |
| --- | --- |
| 表达体系 | E.coli / 大肠杆菌表达 |
| 蛋白数量 | 3 个候选蛋白 |
| 用途 | 体外功能实验和 assay development |
| 期望服务 | 表达载体构建、表达测试、可溶性评估、纯化和交付 |
| 交付物 | 纯化蛋白、COA、SDS-PAGE / SEC-HPLC 等质控信息 |

请帮忙确认 GenScript 是否有适合这类项目的 E.coli protein expression package，以及是否可以提供一个初步服务范围和报价前所需信息清单。

谢谢，

Daniel Villarreal  
Manager, Analytical Development  
Bionova Scientific, LLC  
The Woodlands, TX  
Email: gs_customer_01@163.com

预期 Agent 输出：

- 创建新的 lead。
- 使用唯一线索判定：`gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli`。
- 商品组合：RSBU > Protein > Protein Expression E.coli。
- 商品组合中文名：大肠杆菌蛋白表达组合。
- 意向等级：High。
- 更新 lead 上下文：客户主动咨询大肠杆菌蛋白表达组合服务。
- 创建 proposed follow-up task 给 Hui：确认蛋白序列、标签、表达规模、纯化要求和交付形式。

---

### D2 - 客户补充蛋白数量、标签和纯度要求

```text
Email ID: EMAIL-DANIEL-002
Thread ID: THREAD-DANIEL-ECOLI-PROTEIN-001
Direction: Customer -> Sales
From: Daniel Villarreal <gs_customer_01@163.com>
To: Hui Li <gs_sales_01@163.com>
Date: 2026-05-21 05:30
Subject: Re: 大肠杆菌蛋白表达组合服务咨询
Attachment: None
Lead Stage After Email: Discovery
Lead Unique Key: gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli
```

Hui 你好，

补充一些项目背景：

这 3 个蛋白目前都计划使用 N-terminal His tag，目标纯度希望至少达到 85% 以上。如果可以的话，我们希望先做小规模表达筛选，看哪些 construct 适合在 E.coli 中表达。

我们目前还不确定是否所有蛋白都能以可溶形式表达，所以希望服务中能包含 soluble / insoluble fraction 的初步判断。

谢谢，

Daniel

预期 Agent 输出：

- 不创建新 lead。
- 根据客户邮箱和商品组合匹配已有 lead：`gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli`。
- 更新 lead 需求：
  - 3 个候选蛋白。
  - N-terminal His tag。
  - 目标纯度 85% 以上。
  - 需要小规模表达筛选和可溶性评估。
- 更新或补充 proposed follow-up task。

---

### D3 - 客户确认已有序列并询问 E.coli 表达适配性

```text
Email ID: EMAIL-DANIEL-003
Thread ID: THREAD-DANIEL-ECOLI-PROTEIN-001
Direction: Customer -> Sales
From: Daniel Villarreal <gs_customer_01@163.com>
To: Hui Li <gs_sales_01@163.com>
Date: 2026-05-21 06:55
Subject: Re: 大肠杆菌蛋白表达组合服务咨询
Attachment: Protein_Targets_Preliminary_Info.xlsx
Lead Stage After Email: Qualified Lead
Lead Unique Key: gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli
```

Hui 你好，

我们已经有 3 个蛋白的氨基酸序列和初步 construct 信息。我先附上一个简化版表格，里面包含蛋白长度、预测分子量、标签位置和是否包含二硫键的信息。

其中一个蛋白可能包含多个半胱氨酸位点，我们不确定它是否适合 E.coli 表达。如果 E.coli 不是最佳系统，也请帮忙说明是否需要考虑其它表达系统。不过我们目前优先希望先评估 E.coli 方案，因为成本和周期会更适合早期筛选。

谢谢，

Daniel

预期 Agent 输出：

- 匹配已有 lead，不重复创建。
- 识别附件为商品组合相关资料。
- 更新 lead 上下文：客户提供蛋白序列和 construct 初步信息。
- 更新 lead 阶段为 Qualified Lead。
- 创建 proposed follow-up task 给 Hui：评估 3 个蛋白是否适合 E.coli 表达，并确认是否需要替代表达系统建议。

---

### D4 - 销售首次跟进并澄清需求

```text
Email ID: EMAIL-DANIEL-004
Thread ID: THREAD-DANIEL-ECOLI-PROTEIN-001
Direction: Sales -> Customer
From: Hui Li <gs_sales_01@163.com>
To: Daniel Villarreal <gs_customer_01@163.com>
Date: 2026-05-21 09:20
Subject: Re: 大肠杆菌蛋白表达组合服务咨询
Attachment: None
Lead Stage After Email: Contacted / Discovery
Lead Unique Key: gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli
```

Daniel 你好，

感谢你提供项目背景和初步 construct 信息。根据你目前的描述，这个需求可以先归到我们的 `RSBU > Protein > Protein Expression E.coli` 商品组合下，也就是大肠杆菌蛋白表达服务。

我先把当前理解的需求整理如下：

- 3 个候选重组蛋白。
- 优先评估 E.coli 表达体系。
- N-terminal His tag。
- 需要小规模表达筛选。
- 需要判断 soluble / insoluble fraction。
- 目标纯度至少 85%。
- 初步用途是体外功能实验和 assay development。

为了帮你准备更准确的服务范围和报价前信息，我想再确认几个问题：

1. 每个蛋白最终希望获得多少 mg 纯化蛋白？
2. 是否接受先做小规模表达筛选，再决定是否进入纯化放大？
3. 对 buffer、内毒素水平或 tag removal 是否有要求？
4. 是否需要我们提供 codon optimization 和基因合成服务？
5. 如果某个蛋白在 E.coli 中不可溶，是否希望我们提供 mammalian 或 insect expression 的备选建议？

收到这些信息后，我可以帮你整理下一步需要提交的材料和初步服务路径。

祝好，

Hui Li  
Regional BD Manager  
GenScript  
Email: gs_sales_01@163.com

预期 Agent 输出：

- 创建 sales activity 到 lead timeline：销售已回复客户，并确认商品组合为 `RSBU > Protein > Protein Expression E.coli`。
- 将首次销售 follow-up task 标记为 done。
- 创建新的 proposed follow-up task：等待客户补充目标产量、buffer、内毒素、tag removal、是否需要 codon optimization 等信息。
- lead 保持在 Discovery。

---

### D5 - 客户补充目标产量、周期和交付要求

```text
Email ID: EMAIL-DANIEL-005
Thread ID: THREAD-DANIEL-ECOLI-PROTEIN-001
Direction: Customer -> Sales
From: Daniel Villarreal <gs_customer_01@163.com>
To: Hui Li <gs_sales_01@163.com>
Date: 2026-05-22 02:15
Subject: Re: 大肠杆菌蛋白表达组合服务咨询
Attachment: None
Lead Stage After Email: Qualified Lead
Lead Unique Key: gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli
```

Hui 你好，

针对你的问题，补充如下：

1. 第一阶段每个蛋白希望获得 2-5 mg 纯化蛋白即可。
2. 可以接受先做小规模表达筛选，再决定是否进入放大纯化。
3. 暂时不需要去除 His tag。
4. Buffer 方面，希望最终蛋白可以在 PBS 或类似中性 buffer 中交付。
5. 内毒素不是第一阶段最关键要求，但如果可以提供常规 endotoxin level 信息会更好。
6. 如果 E.coli 表达不可溶，可以提供 mammalian expression 的备选建议，但当前报价仍希望先聚焦 E.coli 方案。

我们希望如果可行，整体周期能控制在 4-6 周左右。

谢谢，

Daniel

预期 Agent 输出：

- 匹配已有 lead。
- 更新 lead 需求：
  - 每个蛋白目标产量 2-5 mg。
  - 接受先小规模表达筛选，再放大纯化。
  - 不需要 tag removal。
  - 交付 buffer 偏好 PBS 或中性 buffer。
  - 希望提供常规 endotoxin level 信息。
  - 期望周期 4-6 周。
- lead 阶段更新为 Qualified Lead。
- 创建 proposed follow-up task 给 Hui：整理 E.coli 表达组合服务路径和报价前信息。

---

### D6 - 销售确认商品组合和下一步资料需求

```text
Email ID: EMAIL-DANIEL-006
Thread ID: THREAD-DANIEL-ECOLI-PROTEIN-001
Direction: Sales -> Customer
From: Hui Li <gs_sales_01@163.com>
To: Daniel Villarreal <gs_customer_01@163.com>
Date: 2026-05-22 05:40
Subject: Re: 大肠杆菌蛋白表达组合服务咨询
Attachment: None
Lead Stage After Email: Follow-up Pending
Lead Unique Key: gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli
```

Daniel 你好，

谢谢补充，这些信息很清楚。

我会先按照 `Protein Expression E.coli（大肠杆菌蛋白表达组合）` 来整理服务路径，重点包括：

- 3 个蛋白的小规模表达筛选。
- His tag 融合表达。
- 可溶性判断。
- 表达条件初步优化。
- 纯化至目标纯度。
- 每个蛋白 2-5 mg 的初始交付目标。
- PBS 或类似中性 buffer 交付。
- 常规 QC 信息，包括 SDS-PAGE、纯度评估和必要的检测说明。

为了让评估更完整，能否请你确认一下：这 3 个蛋白是否已经有确定的 DNA 序列，还是目前只有氨基酸序列？如果只有氨基酸序列，我们可能需要一起考虑 codon optimization 和 gene synthesis。

另外，我建议我们安排一个 30 分钟沟通，快速确认 3 个 construct 的风险点，以及 E.coli 方案是否足够覆盖第一阶段筛选目标。

祝好，

Hui

预期 Agent 输出：

- 创建 sales activity 到 lead timeline：销售确认商品组合和服务路径。
- 创建 proposed follow-up task：等待客户确认 DNA 序列 / 氨基酸序列状态。
- 创建 proposed follow-up task：建议安排 30 分钟沟通。
- lead 阶段：Follow-up Pending。

---

### D7 - 客户要求会议并确认序列状态

```text
Email ID: EMAIL-DANIEL-007
Thread ID: THREAD-DANIEL-ECOLI-PROTEIN-001
Direction: Customer -> Sales
From: Daniel Villarreal <gs_customer_01@163.com>
To: Hui Li <gs_sales_01@163.com>
Date: 2026-05-23 01:10
Subject: Re: 大肠杆菌蛋白表达组合服务咨询
Attachment: None
Lead Stage After Email: Qualified Lead
Lead Unique Key: gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli
```

Hui 你好，

可以安排 30 分钟沟通。

目前我们只有氨基酸序列，还没有最终 DNA 序列。如果 GenScript 可以在 E.coli 表达项目中一并处理 codon optimization 和 gene synthesis，也请在服务路径里列出来。

会议前，如果你能先发一份 E.coli 表达组合项目通常需要客户提供的信息清单，会很有帮助。我们可以提前准备 construct 名称、蛋白序列、标签偏好、目标产量和 buffer 要求。

谢谢，

Daniel

预期 Agent 输出：

- 匹配已有 lead。
- 更新 lead：客户只有氨基酸序列，可能需要 codon optimization 和 gene synthesis。
- 更新 lead 上下文：客户要求会议，并请求 E.coli 表达组合信息清单。
- 创建 proposed follow-up task 给 Hui：发送 E.coli 项目信息清单并提供会议时间。

---

### D8 - 销售发送信息清单和会议时间

```text
Email ID: EMAIL-DANIEL-008
Thread ID: THREAD-DANIEL-ECOLI-PROTEIN-001
Direction: Sales -> Customer
From: Hui Li <gs_sales_01@163.com>
To: Daniel Villarreal <gs_customer_01@163.com>
Date: 2026-05-23 04:35
Subject: Re: 大肠杆菌蛋白表达组合服务咨询
Attachment: Ecoli_Protein_Expression_Project_Info_Checklist.pdf
Lead Stage After Email: Follow-up Pending
Lead Unique Key: gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli
```

Daniel 你好，

当然可以。我已附上 E.coli 蛋白表达项目的信息清单，供你们先行准备。

一般来说，报价和技术评估前建议准备以下信息：

1. 每个蛋白的名称或内部编号。
2. 氨基酸序列或 DNA 序列。
3. 期望标签位置，例如 N-terminal His tag。
4. 是否需要 codon optimization 和 gene synthesis。
5. 目标表达体系：本项目先按 E.coli 评估。
6. 期望交付量，例如每个蛋白 2-5 mg。
7. 目标纯度，例如 85% 以上。
8. 交付 buffer，例如 PBS 或类似中性 buffer。
9. 是否有内毒素、聚集、活性检测或特殊储存要求。
10. 如果 E.coli 表达不可溶，是否需要备选表达系统建议。

关于 30 分钟沟通，我目前有以下两个时间方便：

- 2026 年 5 月 26 日，周二，上午 10:00 PT
- 2026 年 5 月 27 日，周三，下午 2:00 PT

请告诉我哪个时间更适合你，或者你也可以建议其它方便的时间。

祝好，

Hui

预期 Agent 输出：

- 创建 sales activity 到 lead timeline：销售发送 E.coli 蛋白表达项目信息清单，并提供可选会议时间。
- 将 send project info checklist task 标记为 done。
- 创建 follow-up task：等待客户确认会议时间。
- lead 阶段：Follow-up Pending。

---

### D9 - 客户选择会议时间

```text
Email ID: EMAIL-DANIEL-009
Thread ID: THREAD-DANIEL-ECOLI-PROTEIN-001
Direction: Customer -> Sales
From: Daniel Villarreal <gs_customer_01@163.com>
To: Hui Li <gs_sales_01@163.com>
Date: 2026-05-24 02:05
Subject: Re: 大肠杆菌蛋白表达组合服务咨询
Attachment: None
Lead Stage After Email: Ready for Sales Review
Lead Unique Key: gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli
```

Hui 你好，

2026 年 5 月 26 日周二上午 10:00 PT 对我来说可以。

我会在会议前和团队一起整理 3 个蛋白的氨基酸序列、标签要求和目标交付量。如果会议后确认 E.coli 方案可行，我们会请你们基于这个商品组合准备下一步服务范围和费用信息。

谢谢，

Daniel

预期 Agent 输出：

- 匹配已有 lead。
- 更新 lead 上下文：客户选择会议时间，并表示会准备 3 个蛋白的序列、标签和目标交付量。
- 创建 proposed follow-up task 给 Hui：安排会议并准备 E.coli protein expression discussion points。
- 将 lead 标记为 Ready for Sales Review。
- 不创建 opportunity。

## 5. Lead 阶段总结

| 字段 | 最终状态 |
| --- | --- |
| Lead owner | Hui Li |
| Customer | Daniel Villarreal |
| Customer email | gs_customer_01@163.com |
| Sales email | gs_sales_01@163.com |
| Product category | RSBU > Protein > Protein Expression E.coli |
| Product category CN | RSBU > 蛋白 > 大肠杆菌蛋白表达组合 |
| Lead unique key | gs_customer_01@163.com + RSBU > Protein > Protein Expression E.coli |
| Lead source | Customer inbound inquiry email |
| Lead stage | Ready for Sales Review |
| Intent level | High |
| Key need | E.coli protein expression package for 3 recombinant proteins |
| Protein count | 3 candidate proteins |
| Expression system | E.coli |
| Tag | N-terminal His tag |
| Target purity | >=85% |
| Initial target yield | 2-5 mg purified protein per target |
| Potential additional services | Codon optimization, gene synthesis |
| Desired TAT | 4-6 weeks if feasible |
| Next sales action | Schedule 30-minute call and prepare E.coli protein expression discussion points |
| Scope boundary | Stop at lead stage; no opportunity creation |
