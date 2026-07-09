export type RiskLevel = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export type CustomerStatus = "ACTIVE" | "SUSPENDED" | "CLOSED";

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
