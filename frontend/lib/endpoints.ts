export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export const ML_SERVICE_URL =
  process.env.NEXT_PUBLIC_ML_SERVICE_URL ?? "http://localhost:8000";

export const endpoints = {
  customers: `${API_BASE_URL}/api/v1/customers`,
  customerRiskProfile: (customerId: string) => `${API_BASE_URL}/api/v1/customers/${customerId}/risk-profile`,
  accounts: `${API_BASE_URL}/api/v1/accounts`,
  transactions: `${API_BASE_URL}/api/v1/transactions`,
  riskScores: `${API_BASE_URL}/api/v1/risk-scores`,
  alerts: `${API_BASE_URL}/api/v1/alerts`,
  alertStatus: (alertId: string) => `${API_BASE_URL}/api/v1/alerts/${alertId}/status`,
  alertAssign: (alertId: string) => `${API_BASE_URL}/api/v1/alerts/${alertId}/assign`,
  cases: `${API_BASE_URL}/api/v1/cases`,
  caseById: (caseId: string) => `${API_BASE_URL}/api/v1/cases/${caseId}`,
  caseDecision: (caseId: string) => `${API_BASE_URL}/api/v1/cases/${caseId}/decision`,
  auditLogs: `${API_BASE_URL}/api/v1/audit-logs`,
  pipelineRuns: `${API_BASE_URL}/api/v1/pipelines/runs`,
  pipelineMetrics: `${API_BASE_URL}/api/v1/pipelines/metrics`,
  pipelineErrors: `${API_BASE_URL}/api/v1/pipelines/errors`,
  dataQualityRuns: `${API_BASE_URL}/api/v1/data-quality/runs`,
  dataQualityResults: `${API_BASE_URL}/api/v1/data-quality/results`,
  dataQualityIssues: `${API_BASE_URL}/api/v1/data-quality/issues`,
  ingestionRuns: `${API_BASE_URL}/api/v1/ingestion/runs`,
  rejectedRecords: `${API_BASE_URL}/api/v1/ingestion/rejected-records`,
  deadLetterTransactions: `${API_BASE_URL}/api/v1/dead-letter/transactions`,
  deadLetterRetry: (id: string) => `${API_BASE_URL}/api/v1/dead-letter/transactions/${id}/retry`,
  deadLetterIgnore: (id: string) => `${API_BASE_URL}/api/v1/dead-letter/transactions/${id}/ignore`,

  mlHealth: `${ML_SERVICE_URL}/health`,
  mlModelInfo: `${ML_SERVICE_URL}/api/v1/model-info`,
  mlScore: `${ML_SERVICE_URL}/api/v1/score`,

  login: `${API_BASE_URL}/api/v1/auth/login`,
  me: `${API_BASE_URL}/api/v1/auth/me`,
  users: `${API_BASE_URL}/api/v1/users`,
  userById: (userId: string) => `${API_BASE_URL}/api/v1/users/${userId}`,
  userStatus: (userId: string) => `${API_BASE_URL}/api/v1/users/${userId}/status`,
  userResetPassword: (userId: string) => `${API_BASE_URL}/api/v1/users/${userId}/reset-password`,
  testTransactions: `${API_BASE_URL}/api/v1/test-transactions`,
};
