export type Role = "ADMIN" | "CLIENT" | "MANAGER" | "EMPLOYEE";
export type ProjectStatus = "ACTIVE" | "ARCHIVED";
export type SlaStatus = "ACTIVE" | "INACTIVE" | "WARNING" | "BREACHED" | "ARCHIVED";
export type AlertStatus = "NEW" | "READ" | "RESOLVED";
export type AlertType = "EMAIL" | "WEB";
export type ReportFormat = "PDF" | "CSV";
export type IncidentSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type IncidentStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED";
export type MetricStatus = "UP" | "DOWN";
export type ServiceStatus = "UP" | "DOWN";
export type SimulationScenario = "NORMAL" | "DEGRADED" | "OUTAGE";

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface AuthUser {
  userId: number;
  email: string;
  role: Role;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SlaLinkedProject {
  id: number;
  name: string;
}

export interface Sla {
  id: number;
  name: string;
  status: SlaStatus;
  uptimeTarget: number;
  responseTimeLimit: number;
  errorRateLimit: number;
  clientId: number;
  clientName?: string;
  serviceCount?: number;
  linkedProjects?: SlaLinkedProject[];
  createdAt: string;
  updatedAt: string;
}

export interface Client {
  id: number;
  name: string;
  email: string;
  projectName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Alert {
  id: number;
  type: AlertType;
  message: string;
  status: AlertStatus;
  slaId: number;
  slaName?: string;
  serviceId?: number;
  serviceName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Report {
  id: number;
  slaResult: number;
  periodStart: string;
  periodEnd: string;
  generatedAt: string;
  format: ReportFormat;
  slaId: number;
  createdAt: string;
  updatedAt: string;
}

export interface AlertNotification {
  alertId: number;
  type: AlertType;
  status: AlertStatus;
  message: string;
  slaId: number;
  slaName: string;
  clientName: string;
  createdAt: string;
}

export type ApprovalActionType =
  | "DELETE_PROJECT"
  | "DELETE_TEAM"
  | "DELETE_SLA"
  | "ARCHIVE_SLA"
  | "ACTIVATE_SLA"
  | "DEACTIVATE_SLA";

export type ApprovalTargetType = "PROJECT" | "TEAM" | "SLA";

export type ApprovalRequestStatus =
  | "PENDING"
  | "APPROVED"
  | "REJECTED"
  | "CANCELLED"
  | "EXECUTED"
  | "FAILED";

export type ApprovalNotificationKind = "SUBMITTED" | "APPROVED" | "REJECTED";

export interface ApprovalNotification {
  requestId: number;
  kind: ApprovalNotificationKind;
  actionType: ApprovalActionType;
  targetType: ApprovalTargetType;
  targetId: number;
  targetLabel: string;
  message: string;
  status: ApprovalRequestStatus;
  requesterName?: string;
  reviewerName?: string;
  reviewComment?: string;
  createdAt: string;
}

export interface ApprovalRequest {
  id: number;
  requesterId: number;
  requesterName: string;
  requesterEmail: string;
  actionType: ApprovalActionType;
  targetType: ApprovalTargetType;
  targetId: number;
  targetLabel: string;
  reason?: string;
  status: ApprovalRequestStatus;
  reviewerId?: number;
  reviewerName?: string;
  reviewComment?: string;
  reviewedAt?: string;
  executedAt?: string;
  createdAt: string;
}

export type LiveNotificationItem =
  | { id: string; source: "alert"; data: AlertNotification }
  | { id: string; source: "approval"; data: ApprovalNotification };

export interface ApprovalRequestCreatePayload {
  actionType: ApprovalActionType;
  targetType: ApprovalTargetType;
  targetId: number;
  reason?: string;
}

export type NotificationChannel = "WEBSOCKET" | "EMAIL";
export type NotificationDeliveryStatus = "SENT" | "FAILED";

export interface NotificationRecord {
  id: number;
  alertId?: number;
  channel: NotificationChannel;
  status: NotificationDeliveryStatus;
  recipient?: string;
  message: string;
  slaId: number;
  slaName: string;
  clientName: string;
  createdAt: string;
}

export interface Incident {
  id: number;
  startTime: string;
  endTime?: string;
  status: IncidentStatus;
  severity: IncidentSeverity;
  description: string;
  slaId: number;
  projectId?: number;
  projectName?: string;
  assigneeId?: number;
  assigneeName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface IncidentComment {
  id: number;
  incidentId: number;
  authorId: number;
  authorName: string;
  content: string;
  createdAt: string;
}

export interface TeamMember {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface Team {
  id: number;
  name: string;
  description?: string;
  managerId: number;
  managerName: string;
  members: TeamMember[];
  memberCount: number;
  projectCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Project {
  id: number;
  name: string;
  description?: string;
  status: ProjectStatus;
  clientId: number;
  clientName: string;
  teamId?: number;
  teamName?: string;
  slaId?: number;
  slaName?: string;
  managerName?: string;
  assignedMembers: TeamMember[];
  memberCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface TeamCreateRequest {
  name: string;
  description?: string;
  managerId: number;
  memberIds?: number[];
}

export interface TeamUpdateRequest extends TeamCreateRequest {}

export interface ProjectCreateRequest {
  name: string;
  description?: string;
  clientId: number;
  teamId?: number;
  memberIds?: number[];
}

export interface ProjectUpdateRequest {
  name: string;
  description?: string;
  status: ProjectStatus;
  clientId: number;
  teamId?: number;
  memberIds?: number[];
}

export interface MonitoringMetric {
  id: number;
  timestamp: string;
  responseTime: number;
  status: MetricStatus;
  errorRate: number;
  serviceId: number;
  slaId: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ClientCreateRequest {
  name: string;
  email: string;
  projectName?: string;
}

export interface ClientUpdateRequest {
  name: string;
  email: string;
  projectName?: string;
}

export interface SlaCreateRequest {
  name: string;
  status: SlaStatus;
  uptimeTarget: number;
  responseTimeLimit: number;
  errorRateLimit: number;
  clientId: number;
  services?: ServiceDraftRequest[];
}

export interface ServiceDraftRequest {
  name: string;
  status?: ServiceStatus;
}

export interface SlaUpdateRequest {
  name: string;
  uptimeTarget: number;
  responseTimeLimit: number;
  errorRateLimit: number;
  clientId: number;
}

export interface IncidentCreateRequest {
  startTime: string;
  severity: IncidentSeverity;
  description: string;
  slaId: number;
  projectId?: number;
  assigneeId?: number;
}

export interface IncidentUpdateRequest {
  startTime: string;
  endTime?: string | null;
  severity: IncidentSeverity;
  description: string;
  projectId?: number;
}

export interface IncidentAssignRequest {
  assigneeId?: number | null;
}

export interface IncidentStatusChangeRequest {
  status: IncidentStatus;
}

export interface IncidentCommentCreateRequest {
  content: string;
}

export type AiEstimatedPriority = "Low" | "Medium" | "High" | "Critical";

export interface IncidentAnalysis {
  summary: string;
  probableCause: string;
  businessImpact: string;
  estimatedPriority: AiEstimatedPriority | string;
  recommendedSteps: string[];
}

export interface AiChatMessage {
  role: "user" | "assistant";
  content: string;
}

export interface AiChatRequest {
  message: string;
  history?: AiChatMessage[];
}

export interface AiChatResponse {
  reply: string;
}

export interface ExecutiveReportRequest {
  projectId: number;
  periodStart: string;
  periodEnd: string;
}

export interface ExecutiveReportKpiSummary {
  slaScore: number;
  slaStatus: string;
  uptimePercentage: number;
  uptimeTarget: number;
  averageResponseTime: number;
  responseTimeLimit?: number | null;
  responseTimeCompliance: number;
  averageErrorRate: number;
  errorRateLimit?: number | null;
  incidentCount: number;
  criticalIncidentCount: number;
  alertCount: number;
  servicesDown: number;
  servicesDegraded: number;
  metricsAnalyzed: number;
}

export interface ExecutiveReport {
  id?: number;
  projectId: number;
  projectName: string;
  clientName: string;
  slaId: number;
  slaName: string;
  periodStart: string;
  periodEnd: string;
  generatedAt: string;
  generatedByName?: string | null;
  kpiSummary: ExecutiveReportKpiSummary;
  executiveSummary: string;
  kpiAnalysis: string;
  incidentAnalysis: string;
  performanceTrends: string;
  recommendations: string[];
  overallConclusion: string;
}

export interface ExecutiveReportListItem {
  id: number;
  projectId: number;
  projectName: string;
  clientName: string;
  slaId: number;
  slaName: string;
  periodStart: string;
  periodEnd: string;
  generatedAt: string;
  slaScore?: number | null;
  slaStatus?: string | null;
  incidentCount?: number | null;
  alertCount?: number | null;
  generatedByName?: string | null;
}

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: Role;
}

export interface UserUpdateRequest {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  role: Role;
  enabled?: boolean;
}

export interface ServiceEntity {
  id: number;
  name: string;
  status: ServiceStatus;
  slaId: number;
  slaName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceCreateRequest {
  name: string;
  status: ServiceStatus;
  slaId: number;
}

export interface ServiceUpdateRequest {
  name: string;
  status: ServiceStatus;
  slaId?: number;
}

export interface SlaWithServices {
  id: number;
  name: string;
  status: SlaStatus;
  uptimeTarget: number;
  responseTimeLimit: number;
  errorRateLimit: number;
  createdAt: string;
  updatedAt: string;
  services: ServiceEntity[];
}

export interface ClientPortfolio {
  client: Client;
  projects: Project[];
  slas: SlaWithServices[];
}

export interface SlaEvaluation {
  slaId: number;
  slaName: string;
  previousStatus: SlaStatus;
  currentStatus: SlaStatus;
  uptimePercentage: number;
  averageResponseTime: number;
  averageErrorRate: number;
  responseTimeCompliance: number;
  slaScore: number;
  periodStart: string;
  periodEnd: string;
  metricsAnalyzed: number;
  incidentsAnalyzed: number;
  statusChanged: boolean;
  alertCreated: boolean;
  reportCreated: boolean;
}

export interface MetricSimulationResult {
  scenario: SimulationScenario;
  servicesProcessed: number;
  metricsGenerated: number;
}
