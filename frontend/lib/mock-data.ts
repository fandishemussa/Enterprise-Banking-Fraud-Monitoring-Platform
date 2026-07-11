import type {
  Account,
  AlertEscalation,
  AnalyticsSummary,
  AlertSlaPolicy,
  AlertSlaResult,
  AlertStatusHistory,
  AuditLog,
  BankingTransaction,
  CaseNote,
  CaseStatusHistory,
  CaseTimelineEvent,
  Customer,
  CustomerRiskProfile,
  DashboardSummary,
  DataQualityResult,
  DataQualityRun,
  DeadLetterEvent,
  DeadLetterTransaction,
  FeatureDriftResult,
  FraudAlert,
  FraudRule,
  FraudRuleVersion,
  InvestigationCase,
  MlHealth,
  MlModelInfo,
  MlPredictionLog,
  ModelMonitoringSummary,
  PipelineMetrics,
  PipelineRun,
  RetrainingHistoryEntry,
  RiskScore,
  ScoreDistribution,
  SlaSummary,
  StreamingEventLog,
  StreamingMetric,
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

export const mockFraudRules: FraudRule[] = [
  {
    ruleId: "RULE-1001", ruleCode: "LARGE_AMOUNT_RULE", name: "Large Amount",
    description: "Flags transactions above a large-amount threshold", category: "AMOUNT",
    enabled: true, thresholdValue: "5000", secondaryThresholdValue: "10000",
    scoreImpact: 30, secondaryScoreImpact: 50, severity: "HIGH", ruleType: "AMOUNT",
    createdAt: "2026-06-01T00:00:00Z", updatedAt: "2026-06-01T00:00:00Z", updatedBy: "system",
  },
  {
    ruleId: "RULE-1002", ruleCode: "NEW_DEVICE_RULE", name: "New Device",
    description: "Flags the first transaction from an unseen device", category: "DEVICE",
    enabled: true, thresholdValue: null, secondaryThresholdValue: null,
    scoreImpact: 20, secondaryScoreImpact: null, severity: "MEDIUM", ruleType: "DEVICE",
    createdAt: "2026-06-01T00:00:00Z", updatedAt: "2026-06-01T00:00:00Z", updatedBy: "system",
  },
  {
    ruleId: "RULE-1003", ruleCode: "NEW_COUNTRY_RULE", name: "New Country",
    description: "Flags the first transaction from an unseen country", category: "COUNTRY",
    enabled: true, thresholdValue: null, secondaryThresholdValue: null,
    scoreImpact: 25, secondaryScoreImpact: null, severity: "HIGH", ruleType: "COUNTRY",
    createdAt: "2026-06-01T00:00:00Z", updatedAt: "2026-06-01T00:00:00Z", updatedBy: "system",
  },
  {
    ruleId: "RULE-1004", ruleCode: "HIGH_FREQUENCY_RULE", name: "High Frequency",
    description: "Flags more than the threshold count of transactions in a 10-minute window", category: "FREQUENCY",
    enabled: true, thresholdValue: "5", secondaryThresholdValue: null,
    scoreImpact: 35, secondaryScoreImpact: null, severity: "HIGH", ruleType: "FREQUENCY",
    createdAt: "2026-06-01T00:00:00Z", updatedAt: "2026-06-01T00:00:00Z", updatedBy: "system",
  },
  {
    ruleId: "RULE-1005", ruleCode: "HIGH_RISK_MERCHANT_RULE", name: "High-Risk Merchant",
    description: "Flags transactions in high-risk merchant categories", category: "MERCHANT",
    enabled: true, thresholdValue: "CRYPTO,GAMBLING,HIGH_RISK_TRANSFER", secondaryThresholdValue: null,
    scoreImpact: 25, secondaryScoreImpact: null, severity: "HIGH", ruleType: "MERCHANT",
    createdAt: "2026-06-01T00:00:00Z", updatedAt: "2026-06-01T00:00:00Z", updatedBy: "system",
  },
  {
    ruleId: "RULE-1006", ruleCode: "UNUSUAL_HOUR_RULE", name: "Unusual Hour",
    description: "Flags transactions occurring during unusual overnight hours", category: "TIME",
    enabled: true, thresholdValue: "0", secondaryThresholdValue: "5",
    scoreImpact: 15, secondaryScoreImpact: null, severity: "MEDIUM", ruleType: "TIME",
    createdAt: "2026-06-01T00:00:00Z", updatedAt: "2026-06-01T00:00:00Z", updatedBy: "system",
  },
];

export const mockFraudRuleVersions: FraudRuleVersion[] = [
  {
    ruleId: "RULE-1001", versionNumber: 1,
    oldConfigJson: null,
    newConfigJson: JSON.stringify({ ruleCode: "LARGE_AMOUNT_RULE", enabled: true, scoreImpact: 30 }),
    changedBy: "system", changeReason: "Rule created", createdAt: "2026-06-01T00:00:00Z",
  },
];

export const mockCaseTimelineEvents: CaseTimelineEvent[] = [
  {
    eventId: "EVENT-1001", caseId: "CASE-1001", eventType: "CASE_CREATED", title: "Case created",
    description: "Created from alert ALERT-1001", actorUsername: "analyst.smith", actorRole: "ANALYST",
    createdAt: "2026-07-08T09:10:00Z",
  },
  {
    eventId: "EVENT-1002", caseId: "CASE-1001", eventType: "CASE_ASSIGNED", title: "Case assigned",
    description: "Assigned to analyst.smith", actorUsername: "analyst.smith", actorRole: "ANALYST",
    createdAt: "2026-07-08T09:12:00Z",
  },
  {
    eventId: "EVENT-1003", caseId: "CASE-1001", eventType: "NOTE_ADDED", title: "Note added",
    description: "Reviewing large crypto transfer from a new device and country", actorUsername: "analyst.smith",
    actorRole: "ANALYST", createdAt: "2026-07-09T09:00:00Z",
  },
];

export const mockCaseNotes: CaseNote[] = [
  {
    noteId: "NOTE-1001", caseId: "CASE-1001", authorUsername: "analyst.smith", authorRole: "ANALYST",
    noteText: "Reviewing large crypto transfer from a new device and country", internalOnly: false,
    createdAt: "2026-07-09T09:00:00Z", updatedAt: "2026-07-09T09:00:00Z",
  },
];

export const mockCaseStatusHistory: CaseStatusHistory[] = [
  {
    caseId: "CASE-1001", oldStatus: "OPEN", newStatus: "IN_REVIEW", changedBy: "analyst.smith",
    reason: "Status changed via case update", createdAt: "2026-07-09T09:00:00Z",
  },
];

/** Derives a demo-mode customer risk profile from the other mock arrays (same shape the backend
 * computes) rather than hand-maintaining a separate static object that would drift out of sync. */
export function buildMockCustomerRiskProfile(customerId: string): CustomerRiskProfile | null {
  const customer = mockCustomers.find((c) => c.customerId === customerId);
  if (!customer) return null;

  const transactions = mockTransactions.filter((t) => t.customerId === customerId);
  const transactionIds = new Set(transactions.map((t) => t.transactionId));
  const riskScores = mockRiskScores.filter((r) => transactionIds.has(r.transactionId));
  const alerts = mockAlerts.filter((a) => a.customerId === customerId);
  const cases = mockCases.filter((c) => c.customerId === customerId);

  const totalAmount = transactions.reduce((sum, t) => sum + t.amount, 0);
  const devicesUsed = [...new Set(transactions.map((t) => t.deviceId).filter((d): d is string => !!d))];
  const countriesUsed = [...new Set(transactions.map((t) => t.country))];
  const highRiskCategories = new Set(["CRYPTO", "GAMBLING", "HIGH_RISK_TRANSFER"]);
  const highRiskMerchantCategoriesUsed = [
    ...new Set(transactions.map((t) => t.merchantCategory).filter((m): m is string => !!m && highRiskCategories.has(m))),
  ];

  return {
    customerId: customer.customerId,
    fullName: customer.fullName,
    email: customer.email,
    phone: customer.phone,
    country: customer.country,
    riskLevel: customer.riskLevel,
    status: customer.status,
    createdAt: customer.createdAt,

    totalTransactions: transactions.length,
    totalTransactionAmount: totalAmount,
    averageTransactionAmount: transactions.length > 0 ? totalAmount / transactions.length : 0,
    maxTransactionAmount: transactions.reduce((max, t) => Math.max(max, t.amount), 0),
    totalAlerts: alerts.length,
    openAlerts: alerts.filter((a) => a.status === "OPEN").length,
    confirmedFraudCount: alerts.filter((a) => a.status === "CONFIRMED_FRAUD").length,
    falsePositiveCount: alerts.filter((a) => a.status === "FALSE_POSITIVE").length,
    totalCases: cases.length,
    openCases: cases.filter((c) => c.status !== "RESOLVED" && c.status !== "CLOSED").length,
    criticalTransactionCount: riskScores.filter((r) => r.riskLevel === "CRITICAL").length,
    highRiskTransactionCount: riskScores.filter((r) => r.riskLevel === "HIGH").length,

    behavior: {
      devicesUsed,
      countriesUsed,
      highRiskMerchantCategoriesUsed,
      mostUsedChannel: transactions[0]?.channel ?? null,
      mostUsedCountry: transactions[0]?.country ?? null,
      lastTransactionAt: transactions[0]?.createdAt ?? null,
      lastAlertAt: alerts[0]?.createdAt ?? null,
    },

    recentTransactions: transactions.slice(0, 5),
    recentRiskScores: riskScores.slice(0, 5),
    recentAlerts: alerts.slice(0, 5),
    recentCases: cases.slice(0, 5),

    riskTrendData: riskScores.map((r) => ({ date: r.createdAt.slice(0, 10), averageRiskScore: r.finalScore })),
    transactionVolumeTrend: transactions.map((t) => ({ date: t.createdAt.slice(0, 10), count: 1, totalAmount: t.amount })),
    alertTrend: alerts.map((a) => ({ date: a.createdAt.slice(0, 10), count: 1, totalAmount: 0 })),
  };
}

export const mockSlaPolicies: AlertSlaPolicy[] = [
  { policyId: "SLA-1001", priority: "CRITICAL", responseTimeMinutes: 15, resolutionTimeMinutes: 240, enabled: true },
  { policyId: "SLA-1002", priority: "HIGH", responseTimeMinutes: 60, resolutionTimeMinutes: 480, enabled: true },
  { policyId: "SLA-1003", priority: "MEDIUM", responseTimeMinutes: 240, resolutionTimeMinutes: 1440, enabled: true },
  { policyId: "SLA-1004", priority: "LOW", responseTimeMinutes: 1440, resolutionTimeMinutes: 4320, enabled: true },
];

export const mockSlaResults: AlertSlaResult[] = [
  {
    alertId: "ALERT-1001", policyId: "SLA-1001",
    responseDeadline: "2026-07-08T02:55:05Z", resolutionDeadline: "2026-07-08T06:40:05Z",
    firstAcknowledgedAt: "2026-07-08T09:00:00Z", resolvedAt: null,
    responseBreached: true, resolutionBreached: true, status: "BREACHED",
  },
  {
    alertId: "ALERT-1002", policyId: "SLA-1001",
    responseDeadline: "2026-07-09T03:27:05Z", resolutionDeadline: "2026-07-09T07:12:05Z",
    firstAcknowledgedAt: null, resolvedAt: null,
    responseBreached: false, resolutionBreached: false, status: "NEAR_BREACH",
  },
  {
    alertId: "ALERT-1003", policyId: "SLA-1002",
    responseDeadline: "2026-07-09T23:16:00Z", resolutionDeadline: "2026-07-10T06:16:00Z",
    firstAcknowledgedAt: "2026-07-10T08:00:00Z", resolvedAt: null,
    responseBreached: true, resolutionBreached: false, status: "ON_TRACK",
  },
  {
    alertId: "ALERT-1004", policyId: "SLA-1002",
    responseDeadline: "2026-07-05T11:00:00Z", resolutionDeadline: "2026-07-05T18:00:00Z",
    firstAcknowledgedAt: "2026-07-05T10:30:00Z", resolvedAt: "2026-07-06T09:00:00Z",
    responseBreached: false, resolutionBreached: true, status: "COMPLETED",
  },
  {
    alertId: "ALERT-1005", policyId: "SLA-1003",
    responseDeadline: "2026-07-04T14:00:00Z", resolutionDeadline: "2026-07-05T10:00:00Z",
    firstAcknowledgedAt: "2026-07-04T10:15:00Z", resolvedAt: "2026-07-04T15:00:00Z",
    responseBreached: false, resolutionBreached: false, status: "COMPLETED",
  },
];

export const mockAlertEscalations: AlertEscalation[] = [
  {
    escalationId: "ESC-1001", alertId: "ALERT-1001", escalatedFrom: "analyst.smith",
    escalatedTo: "supervisor.lee", reason: "Response SLA breached", escalationLevel: 1,
    createdAt: "2026-07-08T09:05:00Z",
  },
];

export const mockAlertStatusHistory: AlertStatusHistory[] = [
  {
    alertId: "ALERT-1001", oldStatus: "OPEN", newStatus: "INVESTIGATING", changedBy: "analyst.smith",
    reason: "Status changed via alert update", createdAt: "2026-07-08T09:00:00Z",
  },
];

export const mockSlaSummary: SlaSummary = {
  totalAlerts: mockSlaResults.length,
  onTrack: mockSlaResults.filter((r) => r.status === "ON_TRACK").length,
  nearBreach: mockSlaResults.filter((r) => r.status === "NEAR_BREACH").length,
  breached: mockSlaResults.filter((r) => r.status === "BREACHED").length,
  completed: mockSlaResults.filter((r) => r.status === "COMPLETED").length,
  slaComplianceRatePercent: 50,
  averageResponseTimeMinutes: 42,
  averageResolutionTimeMinutes: 620,
};

export const mockAnalyticsSummary: AnalyticsSummary = {
  totalAlerts: 5,
  totalCases: 4,
  alertsByMerchantCategory: { CRYPTO: 2, GAMBLING: 1, HIGH_RISK_TRANSFER: 1, GROCERY: 1 },
  alertsByCountry: { Nigeria: 2, Russia: 1, Ukraine: 1, "United States": 1 },
  alertsByHourOfDay: { "9": 1, "14": 2, "20": 1, "23": 1 },
  casesByStatus: { OPEN: 2, IN_REVIEW: 1, RESOLVED: 1 },
  casesByDecision: { PENDING: 3, CONFIRMED_FRAUD: 1 },
};

export const mockStreamingMetric: StreamingMetric = {
  metricDate: "2026-07-10",
  eventsProduced: 42,
  eventsConsumed: 39,
  eventsFailed: 3,
  alertsGenerated: 5,
  averageProcessingLatencyMs: 145.2,
  lastEventAt: "2026-07-10T21:00:00Z",
};

export const mockStreamingEvents: StreamingEventLog[] = [
  { eventId: "EVT-1001", topic: "raw-transactions", eventType: "RAW_TRANSACTION", transactionId: "TXN-1010", status: "SUCCESS", message: "Processed as transaction TXN-1010", createdAt: "2026-07-10T20:58:00Z" },
  { eventId: "EVT-1002", topic: "raw-transactions", eventType: "RAW_TRANSACTION", transactionId: "TXN-1011", status: "SUCCESS", message: "Processed as transaction TXN-1011", createdAt: "2026-07-10T20:59:00Z" },
  { eventId: "EVT-1003", topic: "raw-transactions", eventType: "RAW_TRANSACTION", transactionId: null, status: "FAILED", message: "amount must be greater than 0", createdAt: "2026-07-10T21:00:00Z" },
];

export const mockStreamingDeadLetterEvents: DeadLetterEvent[] = [
  {
    eventId: "EVT-1003", sourceTopic: "raw-transactions",
    rawPayload: '{"sourceAccountId":"ACC-1001","amount":0,"currency":"USD"}',
    errorReason: "amount must be greater than 0", errorType: "VALIDATION_ERROR", status: "NEW",
    createdAt: "2026-07-10T21:00:00Z", updatedAt: "2026-07-10T21:00:00Z",
  },
];

export const mockModelMonitoringSummary: ModelMonitoringSummary = {
  totalPredictions: 128,
  averageMlScore: 34.6,
  highRiskPredictionCount: 18,
  criticalRiskPredictionCount: 6,
  anomalyCount: 22,
  fallbackModeCount: 12,
  fallbackRate: 9.4,
  modelVersion: "1.0.0",
  lastTrainingDate: "2026-07-09T19:25:49.960101Z",
  datasetAvailable: true,
  modelLoaded: true,
};

export const mockMlPredictions: MlPredictionLog[] = [
  {
    predictionId: "pred-1001", transactionId: "TXN-1002", mlScore: 92, riskLevel: "CRITICAL",
    isAnomaly: true, fallbackMode: false, modelVersion: "1.0.0",
    featureValues: { amount: 12000, hour_of_day: 2, transaction_frequency: 7 },
    createdAt: "2026-07-08T02:40:01Z",
  },
  {
    predictionId: "pred-1002", transactionId: "TXN-1003", mlScore: 95, riskLevel: "CRITICAL",
    isAnomaly: true, fallbackMode: false, modelVersion: "1.0.0",
    featureValues: { amount: 45000, hour_of_day: 3, transaction_frequency: 2 },
    createdAt: "2026-07-09T03:12:01Z",
  },
];

export const mockScoreDistribution: ScoreDistribution = { low: 70, medium: 34, high: 18, critical: 6 };

export const mockFeatureDrift: FeatureDriftResult[] = [
  { featureName: "amount", baselineAverage: 1250.4, currentAverage: 1310.2, driftScore: 0.05, driftStatus: "STABLE" },
  { featureName: "transaction_frequency", baselineAverage: 2.1, currentAverage: 3.4, driftScore: 0.62, driftStatus: "DRIFTED" },
  { featureName: "country_risk_score", baselineAverage: 18.0, currentAverage: 24.5, driftScore: 0.36, driftStatus: "WARNING" },
];

export const mockRetrainingHistory: RetrainingHistoryEntry[] = [
  {
    modelVersion: "1.0.0", trainingDate: "2026-07-09T19:25:49.960101Z",
    datasetName: "PaySim Synthetic Financial Transactions", numberOfTrainingRows: 200000,
    status: "SUCCESS", metrics: { decisionScoreMin: -0.42, decisionScoreMax: 0.38 },
  },
];
