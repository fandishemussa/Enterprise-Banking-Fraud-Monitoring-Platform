export type RiskLevel = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export type CustomerStatus = "ACTIVE" | "SUSPENDED" | "CLOSED" | "LOCKED";

export type AccountType = "SAVINGS" | "CHECKING" | "BUSINESS";
export type AccountStatus = "ACTIVE" | "BLOCKED" | "CLOSED";

export type TransactionType = "TRANSFER" | "WITHDRAWAL" | "DEPOSIT" | "PAYMENT" | "CARD_PAYMENT";
export type TransactionChannel = "MOBILE" | "WEB" | "ATM" | "POS" | "BRANCH";
export type TransactionStatus = "SUCCESS" | "FAILED" | "PENDING" | "REVERSED";

export type AlertType = "RULE_BASED" | "ML_ANOMALY" | "HYBRID";
export type AlertPriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type AlertStatus =
  | "OPEN"
  | "ACKNOWLEDGED"
  | "INVESTIGATING"
  | "FALSE_POSITIVE"
  | "CONFIRMED_FRAUD"
  | "RESOLVED"
  | "ESCALATED";

export type CaseStatus = "OPEN" | "IN_REVIEW" | "ESCALATED" | "RESOLVED" | "CLOSED";
export type CasePriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type CaseDecision = "PENDING" | "FALSE_POSITIVE" | "CONFIRMED_FRAUD" | "NEEDS_MORE_REVIEW";

export type PipelineType = "INGESTION" | "DATA_QUALITY";
export type PipelineStatus = "RUNNING" | "SUCCESS" | "FAILED" | "PARTIAL_SUCCESS";

export type DeadLetterStatus = "NEW" | "RETRIED" | "IGNORED" | "RESOLVED";

export interface Customer {
  customerId: string;
  fullName: string;
  email: string;
  phone?: string;
  country: string;
  riskLevel: RiskLevel;
  status: CustomerStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Account {
  accountId: string;
  customerId: string;
  accountType: AccountType;
  balance: number;
  currency: string;
  status: AccountStatus;
  createdAt: string;
  updatedAt: string;
}

export interface BankingTransaction {
  transactionId: string;
  customerId: string;
  sourceAccountId: string;
  destinationAccountId?: string | null;
  amount: number;
  currency: string;
  transactionType: TransactionType;
  channel: TransactionChannel;
  merchantCategory?: string | null;
  country: string;
  deviceId?: string | null;
  ipAddress?: string | null;
  status: TransactionStatus;
  riskLevel?: RiskLevel;
  createdAt: string;
}

export interface RiskScore {
  transactionId: string;
  ruleScore: number;
  mlScore: number | null;
  finalScore: number;
  riskLevel: RiskLevel;
  scoringSource?: string;
  triggeredRules?: string;
  explanation?: string;
  mlExplanation?: string | null;
  createdAt: string;
}

export interface FraudAlert {
  alertId: string;
  transactionId: string;
  customerId: string;
  riskScoreValue?: number;
  alertType: AlertType;
  priority: AlertPriority;
  message?: string;
  status: AlertStatus;
  assignedTo?: string | null;
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string | null;
}

export interface InvestigationCase {
  caseId: string;
  alertId: string;
  customerId: string;
  assignedTo?: string | null;
  priority: CasePriority;
  status: CaseStatus;
  decision: CaseDecision;
  notes?: string | null;
  createdAt: string;
  updatedAt: string;
  closedAt?: string | null;
}

export interface AuditLog {
  eventType: string;
  username: string;
  role?: string;
  entityType: string;
  entityId: string;
  oldValue?: string | null;
  newValue?: string | null;
  description: string;
  createdAt: string;
}

export interface PipelineTaskRun {
  taskName: string;
  status: PipelineStatus;
  recordsProcessed: number;
  errorMessage?: string | null;
  startedAt: string;
  finishedAt?: string | null;
}

export interface PipelineRun {
  runId: string;
  pipelineType: PipelineType;
  status: PipelineStatus;
  triggeredBy?: string;
  recordsProcessed: number;
  recordsAccepted: number;
  recordsRejected: number;
  recordsFailed: number;
  durationMs?: number | null;
  failureReason?: string | null;
  startedAt: string;
  finishedAt?: string | null;
  tasks?: PipelineTaskRun[];
}

export interface IngestionRun {
  runId: string;
  fileName: string;
  status: "COMPLETED" | "FAILED";
  totalRows: number;
  acceptedRows: number;
  rejectedRows: number;
  deadLetterRows: number;
  startedAt: string;
  finishedAt?: string | null;
}

export interface PipelineError {
  pipelineRunId: string;
  taskName?: string;
  errorType: string;
  errorMessage: string;
  occurredAt: string;
}

export interface PipelineMetrics {
  totalRuns: number;
  successfulRuns: number;
  failedRuns: number;
  successRatePercent: number;
  averageDurationMs: number | null;
  lastSuccessfulRunAt?: string | null;
  lastFailureReason?: string | null;
}

export interface DataQualityRun {
  runId: string;
  status: PipelineStatus;
  triggeredBy?: string;
  totalRecordsChecked: number;
  totalIssuesFound: number;
  startedAt: string;
  finishedAt?: string | null;
}

export interface DataQualityResult {
  runId: string;
  checkName: string;
  recordsChecked: number;
  recordsPassed: number;
  recordsFailed: number;
  passed: boolean;
}

export interface DeadLetterTransaction {
  eventId: string;
  rawPayload: string;
  errorType: string;
  errorReason: string;
  processedStatus: DeadLetterStatus;
  receivedAt: string;
  lastRetryAt?: string | null;
}

export interface MlHealth {
  status: string;
  service: string;
  version: string;
  modelLoaded: boolean;
  datasetAvailable: boolean;
  fallbackMode: boolean;
}

export interface MlModelInfo {
  modelName?: string | null;
  modelType?: string | null;
  modelVersion?: string | null;
  trainingDate?: string | null;
  datasetName?: string | null;
  numberOfTrainingRows?: number | null;
  featureList: string[];
  modelPath?: string | null;
  modelLoaded: boolean;
  fallbackMode: boolean;
}

export interface DashboardSummary {
  totalTransactions: number;
  highRiskTransactions: number;
  criticalRiskTransactions: number;
  openFraudAlerts: number;
  confirmedFraudCases: number;
  falsePositiveRate: number;
  averageRiskScore: number;
  pipelineSuccessRate: number;
  recordsProcessedToday: number;
  mlServiceOnline: boolean;
  fraudTrend: { date: string; alerts: number; transactions: number }[];
  riskDistribution: { riskLevel: RiskLevel; count: number }[];
  alertStatusDistribution: { status: AlertStatus; count: number }[];
  pipelineRecordsChart: { name: string; processed: number; failed: number }[];
  recentAlerts: FraudAlert[];
}

export interface ApiResult<T> {
  data: T;
  usingMockData: boolean;
}

export type Role = "ADMIN" | "ANALYST" | "INVESTIGATOR" | "VIEWER" | "TESTER";
export type UserStatus = "ACTIVE" | "DISABLED" | "LOCKED";

export interface AppUser {
  userId: string;
  username: string;
  email: string;
  fullName: string;
  role: Role;
  status: UserStatus;
  createdAt: string;
  updatedAt: string;
  lastLoginAt?: string | null;
}

export interface AssignableUser {
  username: string;
  fullName: string;
  role: Role;
}

export interface LoginResponse {
  token: string;
  user: AppUser;
}

export interface CreateUserPayload {
  username: string;
  email: string;
  fullName: string;
  password: string;
  role: Role;
}

export interface UpdateUserPayload {
  email?: string;
  fullName?: string;
  role?: Role;
}

export interface TestTransactionPayload {
  customerId?: string;
  sourceAccountId: string;
  destinationAccountId?: string;
  amount: number;
  currency: string;
  transactionType: TransactionType;
  channel: TransactionChannel;
  merchantCategory?: string;
  country: string;
  deviceId?: string;
  ipAddress?: string;
}

export interface TestTransactionResult {
  transaction: BankingTransaction;
  riskScore: RiskScore;
  fraudAlert: FraudAlert | null;
  alertGenerated: boolean;
  message: string;
}

export type FraudRuleSeverity = RiskLevel;
export type FraudRuleType = "AMOUNT" | "DEVICE" | "COUNTRY" | "FREQUENCY" | "MERCHANT" | "TIME" | "CUSTOM";

export interface FraudRule {
  ruleId: string;
  ruleCode: string;
  name: string;
  description?: string | null;
  category?: string | null;
  enabled: boolean;
  thresholdValue?: string | null;
  secondaryThresholdValue?: string | null;
  scoreImpact: number;
  secondaryScoreImpact?: number | null;
  severity: FraudRuleSeverity;
  ruleType: FraudRuleType;
  createdAt: string;
  updatedAt: string;
  updatedBy?: string | null;
}

export interface FraudRuleVersion {
  ruleId: string;
  versionNumber: number;
  oldConfigJson?: string | null;
  newConfigJson: string;
  changedBy: string;
  changeReason?: string | null;
  createdAt: string;
}

export interface FraudRulePayload {
  ruleCode: string;
  name: string;
  description?: string;
  category?: string;
  enabled?: boolean;
  thresholdValue?: string;
  secondaryThresholdValue?: string;
  scoreImpact: number;
  secondaryScoreImpact?: number;
  severity: FraudRuleSeverity;
  ruleType: FraudRuleType;
  changeReason?: string;
}

export type CaseTimelineEventType =
  | "CASE_CREATED"
  | "CASE_ASSIGNED"
  | "NOTE_ADDED"
  | "NOTE_UPDATED"
  | "STATUS_CHANGED"
  | "DECISION_UPDATED"
  | "ALERT_LINKED"
  | "TRANSACTION_REVIEWED"
  | "CASE_ESCALATED"
  | "CASE_RESOLVED"
  | "CASE_CLOSED";

export interface CaseNote {
  noteId: string;
  caseId: string;
  authorUsername: string;
  authorRole?: string | null;
  noteText: string;
  internalOnly: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CaseNotePayload {
  noteText: string;
  internalOnly?: boolean;
}

export interface CaseTimelineEvent {
  eventId: string;
  caseId: string;
  eventType: CaseTimelineEventType;
  title: string;
  description?: string | null;
  actorUsername?: string | null;
  actorRole?: string | null;
  metadataJson?: string | null;
  createdAt: string;
}

export interface CaseStatusHistory {
  caseId: string;
  oldStatus: string;
  newStatus: string;
  changedBy: string;
  reason?: string | null;
  createdAt: string;
}

export interface CustomerBehaviorSummary {
  devicesUsed: string[];
  countriesUsed: string[];
  highRiskMerchantCategoriesUsed: string[];
  mostUsedChannel?: string | null;
  mostUsedCountry?: string | null;
  lastTransactionAt?: string | null;
  lastAlertAt?: string | null;
}

export interface CustomerRiskTrendPoint {
  date: string;
  averageRiskScore: number;
}

export interface CustomerTransactionTrendPoint {
  date: string;
  count: number;
  totalAmount: number;
}

export interface CustomerRiskProfile {
  customerId: string;
  fullName: string;
  email: string;
  phone?: string | null;
  country: string;
  riskLevel: RiskLevel;
  status: CustomerStatus;
  createdAt: string;

  totalTransactions: number;
  totalTransactionAmount: number;
  averageTransactionAmount: number;
  maxTransactionAmount: number;
  totalAlerts: number;
  openAlerts: number;
  confirmedFraudCount: number;
  falsePositiveCount: number;
  totalCases: number;
  openCases: number;
  criticalTransactionCount: number;
  highRiskTransactionCount: number;

  behavior: CustomerBehaviorSummary;

  recentTransactions: BankingTransaction[];
  recentRiskScores: RiskScore[];
  recentAlerts: FraudAlert[];
  recentCases: InvestigationCase[];

  riskTrendData: CustomerRiskTrendPoint[];
  transactionVolumeTrend: CustomerTransactionTrendPoint[];
  alertTrend: CustomerTransactionTrendPoint[];
}

export interface BulkOperationResponse {
  succeededIds: string[];
  failures: { id: string; reason: string }[];
}

export interface LinkedCustomer {
  customerId: string;
  fullName: string;
  sharedDeviceIds: string[];
  sharedIpAddresses: string[];
}

export interface AnalyticsSummary {
  totalAlerts: number;
  totalCases: number;
  alertsByMerchantCategory: Record<string, number>;
  alertsByCountry: Record<string, number>;
  alertsByHourOfDay: Record<string, number>;
  casesByStatus: Record<string, number>;
  casesByDecision: Record<string, number>;
}

export type NotificationType = "NEW_ALERT" | "ALERT_ESCALATED" | "LOCK_REQUEST_PENDING";

export interface NotificationItem {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  referenceId: string;
  createdAt: string;
}

export type CustomerLockRequestStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface CustomerLockRequest {
  lockRequestId: string;
  customerId: string;
  requestedBy: string;
  requestedByRole: Role;
  reason?: string | null;
  status: CustomerLockRequestStatus;
  reviewedBy?: string | null;
  reviewedAt?: string | null;
  reviewNotes?: string | null;
  createdAt: string;
}

export type SlaStatus = "ON_TRACK" | "NEAR_BREACH" | "BREACHED" | "COMPLETED";

export interface AlertSlaPolicy {
  policyId: string;
  priority: AlertPriority;
  responseTimeMinutes: number;
  resolutionTimeMinutes: number;
  enabled: boolean;
}

export interface AlertSlaPolicyPayload {
  responseTimeMinutes: number;
  resolutionTimeMinutes: number;
  enabled?: boolean;
}

export interface AlertSlaResult {
  alertId: string;
  policyId: string;
  responseDeadline: string;
  resolutionDeadline: string;
  firstAcknowledgedAt?: string | null;
  resolvedAt?: string | null;
  responseBreached: boolean;
  resolutionBreached: boolean;
  status: SlaStatus;
}

export interface AlertEscalation {
  escalationId: string;
  alertId: string;
  escalatedFrom: string;
  escalatedTo: string;
  reason?: string | null;
  escalationLevel: number;
  createdAt: string;
}

export interface AlertEscalationPayload {
  escalatedTo: string;
  reason?: string;
}

export interface AlertStatusHistory {
  alertId: string;
  oldStatus: string;
  newStatus: string;
  changedBy: string;
  reason?: string | null;
  createdAt: string;
}

export interface SlaSummary {
  totalAlerts: number;
  onTrack: number;
  nearBreach: number;
  breached: number;
  completed: number;
  slaComplianceRatePercent: number;
  averageResponseTimeMinutes: number;
  averageResolutionTimeMinutes: number;
}

export interface PublishStreamingTransactionPayload {
  sourceAccountId: string;
  destinationAccountId?: string;
  amount: number;
  currency: string;
  transactionType: TransactionType;
  channel: TransactionChannel;
  merchantCategory?: string;
  country: string;
  deviceId?: string;
  ipAddress?: string;
}

export interface StreamingMetric {
  metricDate: string;
  eventsProduced: number;
  eventsConsumed: number;
  eventsFailed: number;
  alertsGenerated: number;
  averageProcessingLatencyMs: number;
  lastEventAt?: string | null;
}

export interface StreamingEventLog {
  eventId: string;
  topic: string;
  eventType: string;
  transactionId?: string | null;
  status: string;
  message?: string | null;
  createdAt: string;
}

export type StreamingDeadLetterStatus = "NEW" | "RETRIED" | "IGNORED" | "RESOLVED";

export interface DeadLetterEvent {
  eventId: string;
  sourceTopic: string;
  rawPayload: string;
  errorReason: string;
  errorType: string;
  status: StreamingDeadLetterStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ModelMonitoringSummary {
  totalPredictions: number;
  averageMlScore: number;
  highRiskPredictionCount: number;
  criticalRiskPredictionCount: number;
  anomalyCount: number;
  fallbackModeCount: number;
  fallbackRate: number;
  modelVersion?: string | null;
  lastTrainingDate?: string | null;
  datasetAvailable: boolean;
  modelLoaded: boolean;
}

export interface MlPredictionLog {
  predictionId: string;
  transactionId: string;
  mlScore: number;
  riskLevel: string;
  isAnomaly: boolean;
  fallbackMode: boolean;
  modelVersion: string;
  featureValues: Record<string, number>;
  createdAt: string;
}

export interface ScoreDistribution {
  low: number;
  medium: number;
  high: number;
  critical: number;
}

export type FeatureDriftStatus = "STABLE" | "WARNING" | "DRIFTED";

export interface FeatureDriftResult {
  featureName: string;
  baselineAverage: number;
  currentAverage: number;
  driftScore: number;
  driftStatus: FeatureDriftStatus;
}

export interface RetrainingHistoryEntry {
  modelVersion: string;
  trainingDate: string;
  datasetName: string;
  numberOfTrainingRows: number;
  status: string;
  metrics: Record<string, number | null>;
}
