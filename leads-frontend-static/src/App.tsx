import { useEffect, useState } from 'react';
import { Link, NavLink, useParams } from 'react-router-dom';
import {
  Activity,
  ArrowLeft,
  CheckCircle2,
  Clock3,
  Filter,
  Flame,
  ListChecks,
  Mail,
  Search,
  Sparkles,
  TrendingDown,
  TrendingUp,
  UserRound,
} from 'lucide-react';
import { fetchLeadDetail, fetchLeadList, updateTaskStatus } from './api';
import type { FollowUpTask, Lead, SimilarRecord, TimelineItem } from './data';

export function App({ view }: { view: 'list' | 'detail' }) {
  return (
    <div className="app-shell">
      <Sidebar />
      <main className="main-shell">
        {view === 'list' ? <LeadListPage /> : <LeadDetailPage />}
      </main>
    </div>
  );
}

function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">AI</div>
        <div>
          <strong>Leads AI</strong>
          <span>CRM Workspace</span>
        </div>
      </div>
      <nav className="nav">
        <NavLink to="/leads" className="nav-item">
          <Activity size={18} /> Leads
        </NavLink>
        <a className="nav-item muted"><ListChecks size={18} /> Tasks</a>
        <a className="nav-item muted"><Sparkles size={18} /> AI Insights</a>
      </nav>
    </aside>
  );
}

function LeadListPage() {
  const [leads, setLeads] = useState<Lead[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    fetchLeadList()
      .then((result) => {
        if (active) {
          setLeads(result.leads);
          setError(null);
        }
      })
      .catch((err: Error) => active && setError(err.message))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, []);

  const stats = {
    high: leads.filter((lead) => lead.potential === 'HIGH').length,
    engaged: leads.filter((lead) => lead.currentStage.toUpperCase().includes('ENGAGED') || lead.currentStage.toUpperCase().includes('CONTACTED')).length,
    stalled: leads.filter((lead) => lead.currentStage.toUpperCase().includes('STALLED') || lead.currentStage.toUpperCase().includes('INACTIVE')).length,
    tasks: leads.reduce((total, lead) => total + lead.tasks.filter((task) => task.status !== 'DONE').length, 0),
  };

  return (
    <div className="page">
      <PageTitle
        eyebrow="Salesforce / Leads"
        title="Leads workspace"
        description="Review lead potential, similarity signals, current stage and pending follow-up tasks."
      />

      <section className="metric-grid">
        <Metric label="High Potential" value={stats.high} tone="hot" />
        <Metric label="Engaged" value={stats.engaged} tone="good" />
        <Metric label="Stalled" value={stats.stalled} tone="warn" />
        <Metric label="Pending Tasks" value={stats.tasks} tone="info" />
      </section>

      <section className="filter-bar">
        <div className="search-box">
          <Search size={16} />
          <span>Search lead, account, product</span>
        </div>
        {['Owner', 'Stage', 'Intent', 'Potential', 'Similarity', 'Last Activity'].map((filter) => (
          <button className="filter-button" key={filter}>
            <Filter size={14} /> {filter}
          </button>
        ))}
      </section>

      <section className="data-panel">
        <div className="panel-heading">
          <div>
            <h2>Lead list</h2>
            <p>Live data from the Leads storage service.</p>
          </div>
          <span>{leads.length} records</span>
        </div>
        {loading && <p className="empty-state">Loading backend leads...</p>}
        {error && <p className="empty-state error-state">Backend request failed: {error}</p>}
        {!loading && !error && leads.length === 0 && <p className="empty-state">No leads are stored yet. Run the Sales Leads Agent E2E to populate this workspace.</p>}
        {!loading && !error && leads.length > 0 && (
        <div className="lead-table-wrap">
          <table className="lead-table">
            <thead>
              <tr>
                <th>Lead</th>
                <th>Customer</th>
                <th>Product bundle</th>
                <th>Similarity</th>
                <th>Potential</th>
                <th>Stage</th>
                <th>Trend</th>
                <th>Open task</th>
                <th>Last activity</th>
              </tr>
            </thead>
            <tbody>
              {leads.map((lead) => (
                <tr key={lead.id}>
                  <td>
                    <Link className="lead-link" to={`/leads/${lead.id}`}>{lead.name}</Link>
                    <span className="subtle">{lead.leadNo}</span>
                  </td>
                  <td>
                    <strong>{lead.customerName}</strong>
                    <span className="subtle">{lead.company}</span>
                  </td>
                  <td className="product-cell">{lead.productBundle}</td>
                  <td><Score value={lead.similarityScore} label="match" inverted /></td>
                  <td><Score value={lead.potentialScore} label={lead.potential} /></td>
                  <td><Badge label={lead.currentStage} /></td>
                  <td><TrendBadge trend={lead.trend} /></td>
                  <td>{lead.openTask}</td>
                  <td>
                    <span>{lead.lastActivityAt}</span>
                    <span className="subtle">{lead.lastActivity}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        )}
      </section>
    </div>
  );
}

function LeadDetailPage() {
  const { leadId } = useParams();
  const [lead, setLead] = useState<Lead | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [updatingTaskId, setUpdatingTaskId] = useState<string | null>(null);

  const loadLead = () => {
    if (!leadId) return;
    setLoading(true);
    fetchLeadDetail(leadId)
      .then((result) => {
        setLead(result.lead);
        setError(null);
      })
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadLead();
  }, [leadId]);

  const handleTaskStatus = async (task: FollowUpTask, status: 'ACCEPTED' | 'DONE' | 'DISMISSED') => {
    setUpdatingTaskId(task.id);
    try {
      await updateTaskStatus(task, status);
      await fetchLeadDetail(task.leadId).then((result) => setLead(result.lead));
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setUpdatingTaskId(null);
    }
  };

  if (loading) {
    return (
      <div className="page">
        <Link to="/leads" className="back-link"><ArrowLeft size={16} /> Back to leads</Link>
        <p className="empty-state">Loading lead detail...</p>
      </div>
    );
  }

  if (error || !lead) {
    return (
      <div className="page">
        <Link to="/leads" className="back-link"><ArrowLeft size={16} /> Back to leads</Link>
        <p className="empty-state error-state">Unable to load lead detail: {error || 'Lead not found'}</p>
      </div>
    );
  }

  const salesActivityTimeline = lead.timeline.filter((item) => item.type === 'SALES_ACTIVITY' || item.type === 'SALES_EMAIL');

  return (
    <div className="page">
      <Link to="/leads" className="back-link"><ArrowLeft size={16} /> Back to leads</Link>
      <section className="detail-hero">
        <div>
          <span className="eyebrow">{lead.leadNo}</span>
          <h1>{lead.name}</h1>
          <div className="lead-meta">
            <span><UserRound size={15} /> {lead.customerName}</span>
            <span>{lead.company}</span>
            <span>{lead.owner}</span>
          </div>
          <p>{lead.productBundle}</p>
        </div>
        <div className="score-stack">
          <Score value={lead.potentialScore} label="Potential" />
          <Score value={lead.similarityScore} label="Similarity" inverted />
        </div>
      </section>

      <section className="detail-grid">
        <SummaryCard lead={lead} />
        <TasksCard tasks={lead.tasks} />
      </section>

      <section className="tab-strip" aria-label="Lead detail sections">
        <a href="#timeline">Timeline</a>
        <a href="#tasks">Tasks</a>
        <a href="#similar">Similar Leads / Oppo</a>
      </section>

      <section id="timeline" className="content-grid">
        <div className="data-panel">
          <div className="panel-heading">
            <div>
              <h2>Sales Activity Timeline</h2>
              <p>Sales-side activities only. Customer emails and follow-up tasks are shown in their own sections.</p>
            </div>
          </div>
          <div className="timeline">
            {salesActivityTimeline.map((item) => <TimelineEntry key={item.id} item={item} />)}
            {salesActivityTimeline.length === 0 && <p className="empty-state">No sales activity has been stored for this lead yet.</p>}
          </div>
        </div>
        <div className="insight-rail">
          <Insight title="Current Stage" value={lead.currentStage} />
          <Insight title="Trend" value={lead.trend} />
          <Insight title="Customer Intent" value={lead.activitySummary.customerIntent} />
          <Insight title="Last Progress" value={lead.activitySummary.lastProgressAt} />
        </div>
      </section>

      <section id="tasks" className="data-panel">
        <div className="panel-heading">
          <div>
            <h2>Follow-up Tasks</h2>
            <p>Task cards use the same shape planned for the storage API.</p>
          </div>
        </div>
        <div className="task-grid">
          {lead.tasks.map((task) => (
            <TaskCard
              task={task}
              key={task.id}
              updating={updatingTaskId === task.id}
              onStatusChange={handleTaskStatus}
            />
          ))}
          {lead.tasks.length === 0 && <p className="empty-state">No open follow-up tasks stored for this lead.</p>}
        </div>
      </section>

      <section id="similar" className="data-panel">
        <div className="panel-heading">
          <div>
            <h2>Lead Intent Similarity Evaluation</h2>
            <p>Similarity score and reason blocks inspired by prototype section 1.</p>
          </div>
        </div>
        <div className="similar-grid">
          {lead.similarRecords.map((record) => <SimilarCard record={record} key={record.id} />)}
        </div>
      </section>
    </div>
  );
}

function SummaryCard({ lead }: { lead: Lead }) {
  return (
    <article className="summary-card">
      <div className="section-kicker"><Sparkles size={16} /> Lead Activity Timeline Summary</div>
      <h2>{lead.activitySummary.overallSummary}</h2>
      <div className="summary-facts">
        <Badge label={lead.activitySummary.currentStage} />
        <TrendBadge trend={lead.activitySummary.trend} />
        <span>{lead.activitySummary.progressActivityCount} progress activities</span>
        <span>{lead.activitySummary.noProgressActivityCount} no-progress</span>
      </div>
      <div className="next-action">
        <strong>Recommended next action</strong>
        <p>{lead.activitySummary.nextRecommendedAction}</p>
      </div>
    </article>
  );
}

function TasksCard({ tasks }: { tasks: FollowUpTask[] }) {
  const open = tasks.filter((task) => task.status !== 'DONE').length;
  return (
    <article className="mini-panel">
      <div className="section-kicker"><Clock3 size={16} /> Task Snapshot</div>
      <strong>{open}</strong>
      <span>open follow-up tasks</span>
      <p>{tasks[0]?.displaySummary ?? 'No task currently requires action.'}</p>
    </article>
  );
}

function TimelineEntry({ item }: { item: TimelineItem }) {
  const Icon = item.type === 'CUSTOMER_EMAIL' || item.type === 'SALES_EMAIL' ? Mail : item.type === 'FOLLOW_UP_TASK' ? ListChecks : Activity;
  const tone =
    item.type === 'SALES_ACTIVITY' || item.type === 'SALES_EMAIL'
      ? 'sales'
      : item.type === 'CUSTOMER_EMAIL'
        ? 'customer'
        : item.type === 'FOLLOW_UP_TASK'
          ? 'task'
          : 'summary';
  const label =
    tone === 'sales'
      ? 'Sales'
      : tone === 'customer'
        ? 'Customer inbound'
        : tone === 'task'
          ? 'Follow-up task'
          : 'Summary';
  return (
    <article className={`timeline-entry timeline-entry-${tone}`}>
      <div className="timeline-rail">
        <span className="timeline-time">{item.occurredAt}</span>
        <div className="timeline-icon"><Icon size={17} /></div>
      </div>
      <div className="timeline-body">
        <div className="timeline-head">
          <div>
            <span className="timeline-type">{label}</span>
            <h3>{item.title}</h3>
            <span>{item.actor}</span>
          </div>
          {item.progressSignal && <Badge label={item.progressSignal} />}
        </div>
        <p>{item.summary}</p>
        {item.keyPoints && (
          <ul className="chip-list">
            {item.keyPoints.map((point) => <li key={point}>{point}</li>)}
          </ul>
        )}
        {item.nextStepSignals && (
          <div className="next-signals">
            <strong>Next step signals</strong>
            <span>{item.nextStepSignals.join(' · ')}</span>
          </div>
        )}
      </div>
    </article>
  );
}

function TaskCard({
  task,
  updating = false,
  onStatusChange,
}: {
  task: FollowUpTask;
  updating?: boolean;
  onStatusChange?: (task: FollowUpTask, status: 'ACCEPTED' | 'DONE' | 'DISMISSED') => void;
}) {
  const canAccept = task.status === 'PROPOSED';
  const canClose = !['DONE', 'DISMISSED'].includes(task.status);
  return (
    <article className="task-card">
      <div className="task-head">
        <Badge label={task.status} />
        <span className={`priority priority-${task.priority.toLowerCase()}`}>{task.priority}</span>
      </div>
      <h3>{task.title}</h3>
      <p>{task.displaySummary}</p>
      <div className="due"><Clock3 size={15} /> Due {task.dueAt}</div>
      <ul>
        {task.actionItems.map((item) => <li key={item}><CheckCircle2 size={14} /> {item}</li>)}
      </ul>
      {onStatusChange && (
        <div className="task-actions">
          <button disabled={!canAccept || updating} onClick={() => onStatusChange(task, 'ACCEPTED')}>Accept</button>
          <button disabled={!canClose || updating} onClick={() => onStatusChange(task, 'DONE')}>Done</button>
          <button disabled={!canClose || updating} onClick={() => onStatusChange(task, 'DISMISSED')}>Dismiss</button>
        </div>
      )}
    </article>
  );
}

function SimilarCard({ record }: { record: SimilarRecord }) {
  return (
    <article className="similar-card">
      <div className="similar-head">
        <Badge label={record.type} />
        <Score value={record.score} label="similarity" inverted />
      </div>
      <h3>{record.title}</h3>
      <p>{record.reason}</p>
      <span className="subtle">{record.owner} · {record.age}</span>
    </article>
  );
}

function PageTitle({ eyebrow, title, description }: { eyebrow: string; title: string; description: string }) {
  return (
    <header className="page-title">
      <span className="eyebrow">{eyebrow}</span>
      <h1>{title}</h1>
      <p>{description}</p>
    </header>
  );
}

function Metric({ label, value, tone }: { label: string; value: number; tone: 'hot' | 'good' | 'warn' | 'info' }) {
  return (
    <article className={`metric metric-${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function Score({ value, label, inverted = false }: { value: number; label: string; inverted?: boolean }) {
  const width = Math.max(4, Math.min(value, 100));
  return (
    <div className="score">
      <div className="score-top">
        <strong>{value}</strong>
        <span>{label}</span>
      </div>
      <div className={`score-track ${inverted ? 'inverted' : ''}`}>
        <i style={{ width: `${width}%` }} />
      </div>
    </div>
  );
}

function Badge({ label }: { label: string }) {
  return <span className={`badge badge-${label.toLowerCase().replaceAll('_', '-')}`}>{label}</span>;
}

function TrendBadge({ trend }: { trend: string }) {
  const Icon = trend === 'DECLINING' ? TrendingDown : TrendingUp;
  return <span className={`trend trend-${trend.toLowerCase()}`}><Icon size={14} /> {trend}</span>;
}

function Insight({ title, value }: { title: string; value: string }) {
  return (
    <article className="insight">
      <span>{title}</span>
      <strong>{value}</strong>
    </article>
  );
}
