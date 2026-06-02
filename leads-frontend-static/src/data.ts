export type Stage = string;
export type Trend = string;
export type Potential = 'HIGH' | 'MEDIUM' | 'LOW';
export type TimelineType = 'CUSTOMER_EMAIL' | 'SALES_EMAIL' | 'SALES_ACTIVITY' | 'FOLLOW_UP_TASK' | 'SUMMARY';

export interface Lead {
  id: string;
  leadNo: string;
  name: string;
  customerName: string;
  customerEmail: string;
  company: string;
  owner: string;
  productBundle: string;
  status: string;
  intentLevel: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  potential: Potential;
  potentialScore: number;
  similarityScore: number;
  currentStage: Stage;
  trend: Trend;
  openTask: string;
  lastActivityAt: string;
  lastActivity: string;
  activitySummary: LeadActivitySummary;
  timeline: TimelineItem[];
  tasks: FollowUpTask[];
  similarRecords: SimilarRecord[];
}

export interface LeadActivitySummary {
  overallSummary: string;
  customerIntent: 'YES' | 'NO' | 'UNCLEAR' | string;
  currentStage: Stage;
  trend: Trend;
  progressActivityCount: number;
  noProgressActivityCount: number;
  lastProgressAt: string;
  nextRecommendedAction: string;
}

export interface TimelineItem {
  id: string;
  type: TimelineType;
  occurredAt: string;
  title: string;
  summary: string;
  actor: string;
  progressSignal?: 'PROGRESS' | 'NO_PROGRESS' | 'UNKNOWN' | string;
  stageAfterActivity?: Stage;
  keyPoints?: string[];
  nextStepSignals?: string[];
}

export interface FollowUpTask {
  id: string;
  taskNo: string;
  leadId: string;
  sourceEmailId?: string | null;
  assignedSalesEmail: string;
  status: 'PROPOSED' | 'ACCEPTED' | 'DONE' | 'DISMISSED' | string;
  priority: 'HIGH' | 'NORMAL' | 'LOW' | string;
  taskType: string;
  title: string;
  summary?: string | null;
  reason?: string | null;
  suggestedAction?: string | null;
  displaySummary: string;
  customerNeedSummary?: string | null;
  sourceEventSummary?: string | null;
  dueAt: string;
  actionItems: string[];
  raw: unknown;
}

export interface SimilarRecord {
  id: string;
  type: 'Lead' | 'Oppo';
  title: string;
  owner: string;
  score: number;
  age: string;
  reason: string;
}
