import type { FollowUpTask, Lead, LeadActivitySummary, Potential, SimilarRecord, TimelineItem, TimelineType } from './data';

interface Page<T> {
  content: T[];
}

interface ProductBundleDto {
  code?: string;
  pathEn?: string;
  pathCn?: string;
}

interface LeadDto {
  id: string;
  leadNo?: string;
  customerEmail?: string;
  customerEmailNormalized?: string;
  customerName?: string;
  company?: string;
  productBundle?: ProductBundleDto;
  ownerSalesEmail?: string;
  status?: string;
  intentLevel?: string;
  currentStage?: string;
  timelineTrend?: string;
  inquirySummary?: string;
  extractedRequirements?: Record<string, unknown>;
  latestEmailAt?: string;
  lastCustomerEmailAt?: string;
  lastSalesActivityAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

interface TaskDto {
  id: string;
  taskNo?: string;
  leadId: string;
  sourceEmailId?: string | null;
  assignedSalesEmail?: string;
  status?: string;
  taskType?: string;
  priority?: string;
  title?: string;
  summary?: string | null;
  reason?: string | null;
  suggestedAction?: string | null;
  displaySummary?: string | null;
  customerNeedSummary?: string | null;
  sourceEventSummary?: string | null;
  actionItems?: string[];
  contextSnapshot?: Record<string, unknown>;
  priorityReason?: string | null;
  dueAt?: string | null;
  acceptedAt?: string | null;
  completedAt?: string | null;
  dismissedAt?: string | null;
  closeReason?: string | null;
  createdBy?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

interface SalesActivityDto {
  id: string;
  leadId: string;
  sourceEmailId?: string | null;
  occurredAt?: string;
  title?: string | null;
  summary?: string;
  keyPoints?: string[];
  customerSignals?: string[];
  nextStepSignals?: string[];
  progressSignal?: string;
  stageAfterActivity?: string | null;
  salesEmail?: string;
}

interface ActivitySummaryDto {
  overallSummary?: string;
  customerIntent?: string;
  currentStage?: string;
  trend?: string;
  progressActivityCount?: number;
  noProgressActivityCount?: number;
  lastProgressAt?: string | null;
  lastActivityAt?: string | null;
  nextRecommendedAction?: string | null;
}

interface TimelineDto {
  activitySummary?: ActivitySummaryDto | null;
  items?: Array<{
    type: string;
    id: string;
    occurredAt?: string;
    summary?: string;
    display?: Record<string, unknown>;
  }>;
}

export interface LeadListResult {
  leads: Lead[];
  rawLeads: LeadDto[];
}

export interface LeadDetailResult {
  lead: Lead;
  rawLead: LeadDto;
  rawTasks: TaskDto[];
  rawActivities: SalesActivityDto[];
  rawTimeline: TimelineDto;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init?.body ? { 'Content-Type': 'application/json' } : {}),
      ...init?.headers,
    },
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(`${response.status} ${response.statusText}: ${text || path}`);
  }
  return response.json() as Promise<T>;
}

export async function fetchLeadList(): Promise<LeadListResult> {
  const page = await request<Page<LeadDto>>('/api/leads?size=200&sort=updatedAt,desc');
  const enriched = await Promise.all(page.content.map((lead) => fetchLeadBundle(lead)));
  return { leads: enriched.map((item) => item.lead), rawLeads: page.content };
}

export async function fetchLeadDetail(leadId: string): Promise<LeadDetailResult> {
  const rawLead = await request<LeadDto>(`/api/leads/${leadId}`);
  return fetchLeadBundle(rawLead);
}

export async function updateTaskStatus(task: FollowUpTask, status: 'ACCEPTED' | 'DONE' | 'DISMISSED'): Promise<void> {
  const raw = task.raw as TaskDto;
  const now = new Date().toISOString();
  const payload: TaskDto = {
    ...raw,
    id: task.id,
    status,
    acceptedAt: status === 'ACCEPTED' ? now : raw.acceptedAt,
    completedAt: status === 'DONE' ? now : raw.completedAt,
    dismissedAt: status === 'DISMISSED' ? now : raw.dismissedAt,
    closeReason: status === 'DISMISSED' ? 'Dismissed from CRM prototype' : raw.closeReason,
  };
  await request(`/api/follow-up-tasks/${task.id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

async function fetchLeadBundle(rawLead: LeadDto): Promise<LeadDetailResult> {
  const [tasksPage, activities, timeline] = await Promise.all([
    request<Page<TaskDto>>(`/api/follow-up-tasks?leadId=${rawLead.id}&size=200&sort=createdAt,desc`),
    request<SalesActivityDto[]>(`/api/leads/${rawLead.id}/activities`),
    request<TimelineDto>(`/api/leads/${rawLead.id}/timeline`),
  ]);
  const rawTasks = tasksPage.content ?? [];
  const rawActivities = activities ?? [];
  const rawTimeline = timeline ?? {};
  return {
    lead: mapLead(rawLead, rawTasks, rawActivities, rawTimeline),
    rawLead,
    rawTasks,
    rawActivities,
    rawTimeline,
  };
}

function mapLead(raw: LeadDto, rawTasks: TaskDto[], activities: SalesActivityDto[], timeline: TimelineDto): Lead {
  const productBundle = productPath(raw.productBundle);
  const tasks = rawTasks.map(mapTask);
  const timelineItems = mapTimeline(timeline, activities);
  const latest = latestTimelineItem(timelineItems, raw, activities);
  const potential = toPotential(raw.intentLevel);
  return {
    id: raw.id,
    leadNo: raw.leadNo || raw.id.slice(0, 8),
    name: raw.inquirySummary || `${raw.customerName || raw.customerEmail || 'Customer'} - ${productBundle}`,
    customerName: raw.customerName || raw.customerEmail || 'Unknown customer',
    customerEmail: raw.customerEmailNormalized || raw.customerEmail || '',
    company: raw.company || companyFromRequirements(raw.extractedRequirements) || 'Unspecified account',
    owner: raw.ownerSalesEmail || 'Unassigned',
    productBundle,
    status: raw.status || 'OPEN',
    intentLevel: raw.intentLevel || 'MEDIUM',
    potential,
    potentialScore: potentialScore(potential),
    similarityScore: similarityScore(raw.customerEmailNormalized || raw.customerEmail || '', raw.productBundle?.code),
    currentStage: raw.currentStage || raw.status || 'New Lead',
    trend: raw.timelineTrend || 'STABLE',
    openTask: openTaskLabel(tasks),
    lastActivityAt: formatDateTime(latest.when),
    lastActivity: latest.summary,
    activitySummary: mapSummary(timeline.activitySummary, raw, tasks, activities),
    timeline: timelineItems,
    tasks,
    similarRecords: similarRecords(raw),
  };
}

function mapTask(raw: TaskDto): FollowUpTask {
  return {
    id: raw.id,
    taskNo: raw.taskNo || raw.id.slice(0, 8),
    leadId: raw.leadId,
    sourceEmailId: raw.sourceEmailId,
    assignedSalesEmail: raw.assignedSalesEmail || '',
    status: raw.status || 'PROPOSED',
    priority: raw.priority || 'NORMAL',
    taskType: raw.taskType || 'FOLLOW_UP',
    title: raw.title || 'Follow up with customer',
    summary: raw.summary,
    reason: raw.reason,
    suggestedAction: raw.suggestedAction,
    displaySummary: raw.displaySummary || raw.summary || raw.suggestedAction || 'Review customer context and prepare next response.',
    customerNeedSummary: raw.customerNeedSummary,
    sourceEventSummary: raw.sourceEventSummary,
    dueAt: formatDateTime(raw.dueAt || raw.createdAt || raw.updatedAt),
    actionItems: raw.actionItems?.length ? raw.actionItems : [raw.suggestedAction || 'Review this follow-up request'],
    raw,
  };
}

function mapTimeline(timeline: TimelineDto, activities: SalesActivityDto[]): TimelineItem[] {
  const items = (timeline.items ?? []).map((item) => {
    const display = item.display ?? {};
    const type = mapTimelineType(item.type, display);
    return {
      id: item.id,
      type,
      occurredAt: formatDateTime(item.occurredAt),
      title: asString(display.title) || timelineTitle(type, display),
      summary: item.summary || asString(display.summary) || asString(display.snippet) || 'Stored timeline event',
      actor: asString(display.actor) || asString(display.fromEmail) || asString(display.salesEmail) || actorFor(type),
      progressSignal: asString(display.progressSignal),
      stageAfterActivity: asString(display.stageAfterActivity),
      keyPoints: asStringArray(display.keyPoints),
      nextStepSignals: asStringArray(display.nextStepSignals),
    };
  });
  const missingActivities = activities
    .filter((activity) => !items.some((item) => item.id === activity.id))
    .map((activity) => ({
      id: activity.id,
      type: 'SALES_ACTIVITY' as TimelineType,
      occurredAt: formatDateTime(activity.occurredAt),
      title: activity.title || 'Sales activity',
      summary: activity.summary || 'Sales activity stored by Agent',
      actor: activity.salesEmail || 'Sales',
      progressSignal: activity.progressSignal,
      stageAfterActivity: activity.stageAfterActivity || undefined,
      keyPoints: activity.keyPoints,
      nextStepSignals: activity.nextStepSignals,
    }));
  return [...items, ...missingActivities].sort((a, b) => Date.parse(b.occurredAt) - Date.parse(a.occurredAt));
}

function mapSummary(summary: ActivitySummaryDto | null | undefined, raw: LeadDto, tasks: FollowUpTask[], activities: SalesActivityDto[]): LeadActivitySummary {
  return {
    overallSummary: summary?.overallSummary || raw.inquirySummary || 'No generated activity summary is stored yet.',
    customerIntent: summary?.customerIntent || raw.intentLevel || 'UNCLEAR',
    currentStage: summary?.currentStage || raw.currentStage || raw.status || 'New Lead',
    trend: summary?.trend || raw.timelineTrend || 'STABLE',
    progressActivityCount: summary?.progressActivityCount ?? activities.length,
    noProgressActivityCount: summary?.noProgressActivityCount ?? 0,
    lastProgressAt: formatDateTime(summary?.lastProgressAt || raw.lastSalesActivityAt || raw.latestEmailAt),
    nextRecommendedAction: summary?.nextRecommendedAction || tasks.find((task) => task.status !== 'DONE')?.suggestedAction || 'Review the latest customer email and choose the next sales action.',
  };
}

function productPath(product?: ProductBundleDto): string {
  return product?.pathEn || product?.pathCn || product?.code || 'Unmapped product bundle';
}

function toPotential(intent?: string): Potential {
  const value = (intent || '').toUpperCase();
  if (value === 'HIGH') return 'HIGH';
  if (value === 'LOW') return 'LOW';
  return 'MEDIUM';
}

function potentialScore(potential: Potential): number {
  return potential === 'HIGH' ? 88 : potential === 'LOW' ? 38 : 64;
}

function similarityScore(email: string, code?: string): number {
  const seed = `${email}:${code || ''}`.split('').reduce((total, char) => total + char.charCodeAt(0), 0);
  return 18 + (seed % 64);
}

function openTaskLabel(tasks: FollowUpTask[]): string {
  const open = tasks.find((task) => !['DONE', 'DISMISSED'].includes(task.status));
  return open?.title || 'No open task';
}

function latestTimelineItem(items: TimelineItem[], raw: LeadDto, activities: SalesActivityDto[]) {
  if (items[0]) return { when: items[0].occurredAt, summary: items[0].summary };
  const when = raw.lastSalesActivityAt || raw.lastCustomerEmailAt || raw.latestEmailAt || raw.updatedAt || raw.createdAt;
  return {
    when,
    summary: activities[0]?.summary || raw.inquirySummary || 'Lead stored without timeline events yet.',
  };
}

function formatDateTime(value?: string | null): string {
  if (!value) return 'Not set';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString([], { month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}

function mapTimelineType(type: string, display: Record<string, unknown>): TimelineType {
  if (type === 'EMAIL') {
    return asString(display.direction)?.toUpperCase() === 'OUTBOUND' ? 'SALES_EMAIL' : 'CUSTOMER_EMAIL';
  }
  if (type === 'SALES_ACTIVITY') return 'SALES_ACTIVITY';
  if (type === 'FOLLOW_UP_TASK') return 'FOLLOW_UP_TASK';
  if (type === 'SUMMARY') return 'SUMMARY';
  return 'FOLLOW_UP_TASK';
}

function timelineTitle(type: TimelineType, display: Record<string, unknown>): string {
  if (type === 'CUSTOMER_EMAIL') return asString(display.subject) || 'Customer email';
  if (type === 'SALES_EMAIL') return asString(display.subject) || 'Sales email';
  if (type === 'SALES_ACTIVITY') return 'Sales activity';
  if (type === 'SUMMARY') return 'Lead summary';
  return 'Follow-up task';
}

function actorFor(type: TimelineType): string {
  if (type === 'SALES_ACTIVITY' || type === 'SALES_EMAIL') return 'Sales';
  if (type === 'CUSTOMER_EMAIL') return 'Customer';
  return 'Sales Leads Agent';
}

function asString(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim() ? value : undefined;
}

function asStringArray(value: unknown): string[] | undefined {
  if (Array.isArray(value)) {
    return value.map((item) => String(item)).filter(Boolean);
  }
  return undefined;
}

function companyFromRequirements(requirements?: Record<string, unknown>): string | undefined {
  return asString(requirements?.company) || asString(requirements?.account) || asString(requirements?.organization);
}

function similarRecords(raw: LeadDto): SimilarRecord[] {
  return [
    {
      id: `${raw.id}-same-customer`,
      type: 'Lead',
      title: 'Same-customer comparison slot',
      owner: raw.ownerSalesEmail || 'Sales',
      score: 0,
      age: 'live backend',
      reason: 'Similarity records are not stored in the backend yet. This panel is ready for the future matching API.',
    },
  ];
}
