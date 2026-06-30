export type Role = "ADMIN" | "USER" | "CLIENT";
export type SlaStatus = "ACTIVE" | "INACTIVE" | "WARNING" | "BREACHED" | "ARCHIVED";
export type AlertStatus = "NEW" | "READ" | "RESOLVED";
export type AlertType = "EMAIL" | "WEB";
export type ReportFormat = "PDF" | "CSV";
export type IncidentSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
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
  severity: IncidentSeverity;
  description: string;
  slaId: number;
  createdAt?: string;
  updatedAt?: string;
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
}

export interface IncidentUpdateRequest {
  startTime: string;
  endTime?: string | null;
  severity: IncidentSeverity;
  description: string;
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
