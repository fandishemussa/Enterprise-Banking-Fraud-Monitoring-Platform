import type {
  Account,
  AuditLog,
  BankingTransaction,
  Customer,
  DashboardSummary,
  DataQualityResult,
  DataQualityRun,
  DeadLetterTransaction,
  FraudAlert,
  InvestigationCase,
  MlHealth,
  MlModelInfo,
  PipelineMetrics,
  PipelineRun,
  RiskScore,
} from "@/types";

export const mockCustomers: Customer[] = [
  {
    customerId: "CUS-1001",
    fullName: "Alice Johnson",
    email: "alice.johnson@example.com",
    phone: "+1-555-0101",
    country: "United States",
    riskLevel: "LOW",
    status: "ACTIVE",
    createdAt: "2026-06-01T09:12:00Z",
    updatedAt: "2026-06-01T09:12:00Z",
  },
  {
    customerId: "CUS-1002",
    fullName: "Bob Martinez",
    email: "bob.martinez@example.com",
    phone: "+1-555-0102",
    country: "Mexico",
    riskLevel: "HIGH",
    status: "ACTIVE",
    createdAt: "2026-06-02T14:20:00Z",
    updatedAt: "2026-07-01T10:00:00Z",
  },
  {
    customerId: "CUS-1003",
    fullName: "Chidi Okafor",
    email: "chidi.okafor@example.com",
    phone: "+234-802-555-0110",
    country: "Nigeria",
    riskLevel: "CRITICAL",
    status: "SUSPENDED",
    createdAt: "2026-05-20T08:00:00Z",
    updatedAt: "2026-07-05T11:30:00Z",
  },
  {
    customerId: "CUS-1004",
    fullName: "Diana Chen",
    email: "diana.chen@example.com",
    phone: "+1-555-0103",
    country: "United States",
    riskLevel: "MEDIUM",
    status: "ACTIVE",
    createdAt: "2026-06-10T16:45:00Z",
    updatedAt: "2026-06-10T16:45:00Z",
  },
  {
    customerId: "CUS-1005",
    fullName: "Elena Petrova",
    email: "elena.petrova@example.com",
    phone: "+44-20-7946-0958",
    country: "United Kingdom",
    riskLevel: "LOW",
    status: "ACTIVE",
    createdAt: "2026-06-15T12:00:00Z",
    updatedAt: "2026-06-15T12:00:00Z",
  },
];

export const mockAccounts: Account[] = [
  { accountId: "ACC-1001", customerId: "CUS-1001", accountType: "CHECKING", balance: 15320.5, currency: "USD", status: "ACTIVE", createdAt: "2026-06-01T09:15:00Z", updatedAt: "2026-06-01T09:15:00Z" },
  { accountId: "ACC-1002", customerId: "CUS-1002", accountType: "SAVINGS", balance: 8420.0, currency: "USD", status: "ACTIVE", createdAt: "2026-06-02T14:22:00Z", updatedAt: "2026-06-02T14:22:00Z" },
  { accountId: "ACC-1003", customerId: "CUS-1003", accountType: "BUSINESS", balance: 152000.75, currency: "USD", status: "BLOCKED", createdAt: "2026-05-20T08:05:00Z", updatedAt: "2026-07-05T11:30:00Z" },
  { accountId: "ACC-1004", customerId: "CUS-1004", accountType: "CHECKING", balance: 3200.2, currency: "USD", status: "ACTIVE", createdAt: "2026-06-10T16:50:00Z", updatedAt: "2026-06-10T16:50:00Z" },
  { accountId: "ACC-1005", customerId: "CUS-1005", accountType: "SAVINGS", balance: 42500.0, currency: "GBP", status: "ACTIVE", createdAt: "2026-06-15T12:05:00Z", updatedAt: "2026-06-15T12:05:00Z" },
];

export const mockTransactions: BankingTransaction[] = [
  { transactionId: "TXN-1001", customerId: "CUS-1001", sourceAccountId: "ACC-1001", destinationAccountId: null, amount: 120.5, currency: "USD", transactionType: "PAYMENT", channel: "WEB", merchantCategory: "GROCERY", country: "United States", deviceId: "device-alice-01", ipAddress: "203.0.113.10", status: "SUCCESS", riskLevel: "LOW", createdAt: "2026-07-08T09:15:00Z" },
  { transactionId: "TXN-1002", customerId: "CUS-1002", sourceAccountId: "ACC-1002", destinationAccountId: null, amount: 12000.0, currency: "USD", transactionType: "TRANSFER", channel: "MOBILE", merchantCategory: "CRYPTO", country: "Nigeria", deviceId: "device-bob-unknown", ipAddress: "198.51.100.23", status: "SUCCESS", riskLevel: "CRITICAL", createdAt: "2026-07-08T02:40:00Z" },
  { transactionId: "TXN-1003", customerId: "CUS-1003", sourceAccountId: "ACC-1003", destinationAccountId: "ACC-1002", amount: 45000.0, currency: "USD", transactionType: "TRANSFER", channel: "WEB", merchantCategory: "HIGH_RISK_TRANSFER", country: "Nigeria", deviceId: "device-chidi-02", ipAddress: "198.51.100.44", status: "SUCCESS", riskLevel: "CRITICAL", createdAt: "2026-07-09T03:12:00Z" },
  { transactionId: "TXN-1004", customerId: "CUS-1004", sourceAccountId: "ACC-1004", destinationAccountId: null, amount: 65.99, currency: "USD", transactionType: "CARD_PAYMENT", channel: "POS", merchantCategory: "RETAIL", country: "United States", deviceId: "device-diana-01", ipAddress: "203.0.113.55", status: "SUCCESS", riskLevel: "LOW", createdAt: "2026-07-09T13:05:00Z" },
  { transactionId: "TXN-1005", customerId: "CUS-1005", sourceAccountId: "ACC-1005", destinationAccountId: null, amount: 7800.0, currency: "GBP", transactionType: "WITHDRAWAL", channel: "ATM", merchantCategory: null, country: "United Kingdom", deviceId: "device-elena-01", ipAddress: "203.0.113.90", status: "SUCCESS", riskLevel: "HIGH", createdAt: "2026-07-09T22:15:00Z" },
  { transactionId: "TXN-1006", customerId: "CUS-1002", sourceAccountId: "ACC-1002", destinationAccountId: null, amount: 250.0, currency: "USD", transactionType: "DEPOSIT", channel: "BRANCH", merchantCategory: null, country: "Mexico", deviceId: "device-bob-02", ipAddress: "198.51.100.24", status: "SUCCESS", riskLevel: "LOW", createdAt: "2026-07-08T11:00:00Z" },
];

export const mockRiskScores: RiskScore[] = [
  { transactionId: "TXN-1001", ruleScore: 0, mlScore: 8, finalScore: 6, riskLevel: "LOW", scoringSource: "HYBRID", triggeredRules: "", explanation: "No risk rules triggered", createdAt: "2026-07-08T09:15:01Z" },
  { transactionId: "TXN-1002", ruleScore: 100, mlScore: 92, finalScore: 100, riskLevel: "CRITICAL", scoringSource: "HYBRID", triggeredRules: "HighRiskMerchantRule(+25), LargeAmountRule(+50), NewCountryRule(+25), NewDeviceRule(+20)", explanation: "Merchant category flagged as high risk: CRYPTO; Amount exceeds 10,000", createdAt: "2026-07-08T02:40:01Z" },
  { transactionId: "TXN-1003", ruleScore: 100, mlScore: 95, finalScore: 100, riskLevel: "CRITICAL", scoringSource: "HYBRID", triggeredRules: "HighRiskMerchantRule(+25), LargeAmountRule(+50), UnusualHourRule(+15)", explanation: "Large transfer at unusual hour to high-risk merchant category", createdAt: "2026-07-09T03:12:01Z" },
  { transactionId: "TXN-1004", ruleScore: 0, mlScore: 4, finalScore: 3, riskLevel: "LOW", scoringSource: "HYBRID", triggeredRules: "", explanation: "No risk rules triggered", createdAt: "2026-07-09T13:05:01Z" },
  { transactionId: "TXN-1005", ruleScore: 65, mlScore: 58, finalScore: 62, riskLevel: "HIGH", scoringSource: "HYBRID", triggeredRules: "LargeAmountRule(+30), UnusualHourRule(+15)", explanation: "Large withdrawal during unusual hours", createdAt: "2026-07-09T22:15:01Z" },
  { transactionId: "TXN-1006", ruleScore: 0, mlScore: 2, finalScore: 2, riskLevel: "LOW", scoringSource: "HYBRID", triggeredRules: "", explanation: "No risk rules triggered", createdAt: "2026-07-08T11:00:01Z" },
];

export const mockAlerts: FraudAlert[] = [
  { alertId: "ALERT-1001", transactionId: "TXN-1002", customerId: "CUS-1002", riskScoreValue: 100, alertType: "HYBRID", priority: "CRITICAL", message: "Transaction flagged as CRITICAL risk (score 100)", status: "INVESTIGATING", assignedTo: "analyst.smith", createdAt: "2026-07-08T02:40:05Z", updatedAt: "2026-07-08T09:00:00Z", resolvedAt: null },
  { alertId: "ALERT-1002", transactionId: "TXN-1003", customerId: "CUS-1003", riskScoreValue: 100, alertType: "HYBRID", priority: "CRITICAL", message: "Large transfer to high-risk merchant at unusual hour", status: "OPEN", assignedTo: null, createdAt: "2026-07-09T03:12:05Z", updatedAt: "2026-07-09T03:12:05Z", resolvedAt: null },
  { alertId: "ALERT-1003", transactionId: "TXN-1005", customerId: "CUS-1005", riskScoreValue: 62, alertType: "RULE_BASED", priority: "HIGH", message: "Large withdrawal during unusual hours", status: "ACKNOWLEDGED", assignedTo: "analyst.jones", createdAt: "2026-07-09T22:16:00Z", updatedAt: "2026-07-10T08:00:00Z", resolvedAt: null },
  { alertId: "ALERT-1004", transactionId: "TXN-1002", customerId: "CUS-1002", riskScoreValue: 88, alertType: "ML_ANOMALY", priority: "HIGH", message: "Anomalous transaction pattern detected", status: "CONFIRMED_FRAUD", assignedTo: "analyst.smith", createdAt: "2026-07-05T10:00:00Z", updatedAt: "2026-07-06T09:00:00Z", resolvedAt: "2026-07-06T09:00:00Z" },
  { alertId: "ALERT-1005", transactionId: "TXN-1006", customerId: "CUS-1002", riskScoreValue: 40, alertType: "RULE_BASED", priority: "MEDIUM", message: "Minor deviation from customer profile", status: "FALSE_POSITIVE", assignedTo: "analyst.jones", createdAt: "2026-07-04T10:00:00Z", updatedAt: "2026-07-04T15:00:00Z", resolvedAt: "2026-07-04T15:00:00Z" },
];

export const mockCases: InvestigationCase[] = [
  { caseId: "CASE-1001", alertId: "ALERT-1001", customerId: "CUS-1002", assignedTo: "analyst.smith", priority: "CRITICAL", status: "IN_REVIEW", decision: "PENDING", notes: "Reviewing large crypto transfer from a new device and country", createdAt: "2026-07-08T09:10:00Z", updatedAt: "2026-07-09T09:00:00Z", closedAt: null },
  { caseId: "CASE-1002", alertId: "ALERT-1004", customerId: "CUS-1002", assignedTo: "analyst.smith", priority: "HIGH", status: "CLOSED", decision: "CONFIRMED_FRAUD", notes: "Confirmed unauthorized access, account suspended", createdAt: "2026-07-05T10:30:00Z", updatedAt: "2026-07-06T09:00:00Z", closedAt: "2026-07-06T09:00:00Z" },
  { caseId: "CASE-1003", alertId: "ALERT-1005", customerId: "CUS-1002", assignedTo: "analyst.jones", priority: "MEDIUM", status: "RESOLVED", decision: "FALSE_POSITIVE", notes: "Customer confirmed transaction was legitimate", createdAt: "2026-07-04T11:00:00Z", updatedAt: "2026-07-04T15:30:00Z", closedAt: "2026-07-04T15:30:00Z" },
];

export const mockAuditLogs: AuditLog[] = [
  { eventType: "FRAUD_ALERT_CREATED", username: "system", role: "SYSTEM", entityType: "FraudAlert", entityId: "ALERT-1002", description: "Fraud alert ALERT-1002 created for transaction TXN-1003", createdAt: "2026-07-09T03:12:05Z" },
  { eventType: "RISK_SCORE_GENERATED", username: "system", role: "SYSTEM", entityType: "RiskScore", entityId: "TXN-1003", description: "Risk score generated for transaction TXN-1003", createdAt: "2026-07-09T03:12:01Z" },
  { eventType: "TRANSACTION_CREATED", username: "system", role: "SYSTEM", entityType: "BankingTransaction", entityId: "TXN-1003", description: "Transaction TXN-1003 created for customer CUS-1003", createdAt: "2026-07-09T03:12:00Z" },
  { eventType: "CASE_DECISION_UPDATED", username: "system", role: "SYSTEM", entityType: "InvestigationCase", entityId: "CASE-1002", description: "Case CASE-1002 decision changed from PENDING to CONFIRMED_FRAUD", createdAt: "2026-07-06T09:00:00Z" },
  { eventType: "ALERT_STATUS_UPDATED", username: "system", role: "SYSTEM", entityType: "FraudAlert", entityId: "ALERT-1003", description: "Alert ALERT-1003 status changed from OPEN to ACKNOWLEDGED", createdAt: "2026-07-10T08:00:00Z" },
];

export const mockPipelineRuns: PipelineRun[] = [
  {
    runId: "PIPE-1002", pipelineType: "DATA_QUALITY", status: "SUCCESS", triggeredBy: "auto-post-ingestion:ING-1001",
    recordsProcessed: 3, recordsAccepted: 3, recordsRejected: 0, recordsFailed: 0, durationMs: 101,
    startedAt: "2026-07-09T18:19:26Z", finishedAt: "2026-07-09T18:19:26Z",
    tasks: [
      { taskName: "AccountReferenceCheck", status: "SUCCESS", recordsProcessed: 3, startedAt: "2026-07-09T18:19:26Z", finishedAt: "2026-07-09T18:19:26Z" },
      { taskName: "AmountRangeCheck", status: "SUCCESS", recordsProcessed: 3, startedAt: "2026-07-09T18:19:26Z", finishedAt: "2026-07-09T18:19:26Z" },
    ],
  },
  {
    runId: "PIPE-1001", pipelineType: "INGESTION", status: "PARTIAL_SUCCESS", triggeredBy: "csv-upload:sample_transactions.csv",
    recordsProcessed: 10, recordsAccepted: 3, recordsRejected: 6, recordsFailed: 1, durationMs: 311,
    startedAt: "2026-07-09T18:19:25Z", finishedAt: "2026-07-09T18:19:26Z",
    tasks: [
      { taskName: "parse-and-stage", status: "SUCCESS", recordsProcessed: 10, startedAt: "2026-07-09T18:19:25Z", finishedAt: "2026-07-09T18:19:26Z" },
      { taskName: "validate-and-load", status: "SUCCESS", recordsProcessed: 9, startedAt: "2026-07-09T18:19:26Z", finishedAt: "2026-07-09T18:19:26Z" },
    ],
  },
];

export const mockPipelineMetrics: PipelineMetrics = {
  totalRuns: 2,
  successfulRuns: 1,
  failedRuns: 0,
  successRatePercent: 50,
  averageDurationMs: 206,
  lastSuccessfulRunAt: "2026-07-09T18:19:26Z",
  lastFailureReason: null,
};

export const mockDataQualityRuns: DataQualityRun[] = [
  { runId: "DQ-1002", status: "SUCCESS", triggeredBy: "manual", totalRecordsChecked: 3, totalIssuesFound: 0, startedAt: "2026-07-09T18:19:45Z", finishedAt: "2026-07-09T18:19:45Z" },
  { runId: "DQ-1001", status: "SUCCESS", triggeredBy: "auto-post-ingestion:ING-1001", totalRecordsChecked: 3, totalIssuesFound: 0, startedAt: "2026-07-09T18:19:26Z", finishedAt: "2026-07-09T18:19:26Z" },
];

export const mockDataQualityResults: DataQualityResult[] = [
  { runId: "DQ-1002", checkName: "VolumeAnomalyCheck", recordsChecked: 3, recordsPassed: 3, recordsFailed: 0, passed: true },
  { runId: "DQ-1002", checkName: "ValidStatusCheck", recordsChecked: 3, recordsPassed: 3, recordsFailed: 0, passed: true },
  { runId: "DQ-1002", checkName: "UniqueCheck", recordsChecked: 3, recordsPassed: 3, recordsFailed: 0, passed: true },
  { runId: "DQ-1002", checkName: "NotNullCheck", recordsChecked: 3, recordsPassed: 3, recordsFailed: 0, passed: true },
  { runId: "DQ-1002", checkName: "FreshnessCheck", recordsChecked: 3, recordsPassed: 3, recordsFailed: 0, passed: true },
  { runId: "DQ-1002", checkName: "AmountRangeCheck", recordsChecked: 3, recordsPassed: 3, recordsFailed: 0, passed: true },
  { runId: "DQ-1002", checkName: "AccountReferenceCheck", recordsChecked: 3, recordsPassed: 3, recordsFailed: 0, passed: true },
];

export const mockDeadLetterTransactions: DeadLetterTransaction[] = [
  { eventId: "DLQ-1001", rawPayload: "this,row,has,too,few,columns", errorType: "IllegalArgumentException", errorReason: "Expected 14 columns but found 6", processedStatus: "IGNORED", receivedAt: "2026-07-09T18:19:26Z", lastRetryAt: "2026-07-09T18:19:44Z" },
  { eventId: "DLQ-1002", rawPayload: "TXN-9001,CUS-1001,ACC-1001,,abc,USD,PAYMENT,WEB,,US,,,,SUCCESS,2026-07-09", errorType: "NumberFormatException", errorReason: "amount not numeric: abc", processedStatus: "NEW", receivedAt: "2026-07-09T20:02:10Z", lastRetryAt: null },
];

export const mockMlHealth: MlHealth = {
  status: "healthy",
  service: "ml-fraud-scoring-service",
  version: "1.0.0",
  modelLoaded: true,
  datasetAvailable: true,
  fallbackMode: false,
};

export const mockMlModelInfo: MlModelInfo = {
  modelName: "PaySim Isolation Forest Fraud Detector",
  modelType: "IsolationForest",
  modelVersion: "1.0.0",
  trainingDate: "2026-07-09T18:55:09Z",
  datasetName: "PaySim Synthetic Financial Transactions",
  numberOfTrainingRows: 6362620,
  featureList: [
    "amount", "hour_of_day", "transaction_frequency", "failed_attempt_count",
    "country_risk_score", "new_device", "new_country", "merchant_risk_score",
    "customer_average_amount_ratio", "source_old_balance", "source_new_balance",
    "destination_old_balance", "destination_new_balance", "balance_delta",
    "transaction_type_encoded", "flagged_fraud",
  ],
  modelPath: "models/fraud_model.joblib",
  modelLoaded: true,
  fallbackMode: false,
};

export const mockDashboardSummary: DashboardSummary = {
  totalTransactions: 6,
  highRiskTransactions: 1,
  criticalRiskTransactions: 2,
  openFraudAlerts: 2,
  confirmedFraudCases: 1,
  falsePositiveRate: 20,
  averageRiskScore: 45.5,
  pipelineSuccessRate: 50,
  recordsProcessedToday: 13,
  mlServiceOnline: true,
  fraudTrend: [
    { date: "Jul 3", alerts: 1, transactions: 42 },
    { date: "Jul 4", alerts: 2, transactions: 51 },
    { date: "Jul 5", alerts: 1, transactions: 38 },
    { date: "Jul 6", alerts: 0, transactions: 45 },
    { date: "Jul 7", alerts: 1, transactions: 60 },
    { date: "Jul 8", alerts: 3, transactions: 55 },
    { date: "Jul 9", alerts: 2, transactions: 63 },
  ],
  riskDistribution: [
    { riskLevel: "LOW", count: 3 },
    { riskLevel: "MEDIUM", count: 0 },
    { riskLevel: "HIGH", count: 1 },
    { riskLevel: "CRITICAL", count: 2 },
  ],
  alertStatusDistribution: [
    { status: "OPEN", count: 1 },
    { status: "ACKNOWLEDGED", count: 1 },
    { status: "INVESTIGATING", count: 1 },
    { status: "CONFIRMED_FRAUD", count: 1 },
    { status: "FALSE_POSITIVE", count: 1 },
  ],
  pipelineRecordsChart: [
    { name: "ING-1001", processed: 10, failed: 1 },
    { name: "DQ-1001", processed: 3, failed: 0 },
    { name: "DQ-1002", processed: 3, failed: 0 },
  ],
  recentAlerts: mockAlerts.slice(0, 3),
};
