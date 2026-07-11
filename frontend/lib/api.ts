import { endpoints } from "@/lib/endpoints";
import { clearSession, getToken } from "@/lib/auth";
import {
  buildMockCustomerRiskProfile,
  mockAccounts,
  mockAlertEscalations,
  mockAnalyticsSummary,
  mockAlerts,
  mockAlertStatusHistory,
  mockAuditLogs,
  mockCaseNotes,
  mockCases,
  mockCaseStatusHistory,
  mockCaseTimelineEvents,
  mockCustomers,
  mockDashboardSummary,
  mockDataQualityResults,
  mockDataQualityRuns,
  mockDeadLetterTransactions,
  mockFeatureDrift,
  mockFraudRules,
  mockFraudRuleVersions,
  mockMlHealth,
  mockMlModelInfo,
  mockMlPredictions,
  mockModelMonitoringSummary,
  mockPipelineMetrics,
  mockPipelineRuns,
  mockRetrainingHistory,
  mockRiskScores,
  mockScoreDistribution,
  mockSlaPolicies,
  mockSlaResults,
  mockSlaSummary,
  mockStreamingDeadLetterEvents,
  mockStreamingEvents,
  mockStreamingMetric,
  mockTransactions,
} from "@/lib/mock-data";
import type {
  Account,
  AlertEscalation,
  AlertEscalationPayload,
  AlertSlaPolicy,
  AnalyticsSummary,
  AlertSlaPolicyPayload,
  AlertSlaResult,
  AlertStatusHistory,
  ApiResult,
  AppUser,
  AssignableUser,
  BulkOperationResponse,
  NotificationItem,
  AuditLog,
  BankingTransaction,
  CaseNote,
  CaseNotePayload,
  CaseStatusHistory,
  CaseTimelineEvent,
  CreateUserPayload,
  Customer,
  CustomerLockRequest,
  CustomerRiskProfile,
  DashboardSummary,
  DataQualityResult,
  DataQualityRun,
  DeadLetterEvent,
  DeadLetterTransaction,
  FeatureDriftResult,
  FraudAlert,
  FraudRule,
  FraudRulePayload,
  FraudRuleVersion,
  IngestionRun,
  InvestigationCase,
  LinkedCustomer,
  LoginResponse,
  MlHealth,
  MlModelInfo,
  MlPredictionLog,
  ModelMonitoringSummary,
  PipelineMetrics,
  PipelineRun,
  PublishStreamingTransactionPayload,
  RetrainingHistoryEntry,
  RiskScore,
  ScoreDistribution,
  SlaSummary,
  StreamingEventLog,
  StreamingMetric,
  TestTransactionPayload,
  TestTransactionResult,
  UpdateUserPayload,
} from "@/types";

const REQUEST_TIMEOUT_MS = 4000;

/** Thrown when the backend rejects a request as unauthenticated (401) or unauthorized (403) so
 * callers/UI can distinguish "please log in" from "you don't have permission" from a generic
 * network failure that should fall back to mock data. */
export class ApiAuthError extends Error {
  constructor(public status: 401 | 403) {
    super(status === 401 ? "Authentication required" : "Access denied");
  }
}

async function fetchWithTimeout(url: string, init?: RequestInit): Promise<Response> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  try {
    const token = getToken();
    const headers = new Headers(init?.headers);
    if (token) headers.set("Authorization", `Bearer ${token}`);

    const response = await fetch(url, { ...init, headers, signal: controller.signal, cache: "no-store" });

    if (response.status === 401 || response.status === 403) {
      if (response.status === 401 && token) {
        clearSession();
        if (typeof window !== "undefined") {
          window.dispatchEvent(new CustomEvent("auth:session-expired"));
        }
      }
      throw new ApiAuthError(response.status);
    }
    if (!response.ok) {
      // Backend errors follow ErrorResponse { status, error, message, path, traceId, timestamp } -
      // surface that message (e.g. "password must be at least 8 characters...") instead of a bare
      // status code, since it's the only place that explains *why* the request was rejected.
      let message = `Request to ${url} failed with status ${response.status}`;
      try {
        const body = await response.clone().json();
        if (typeof body?.message === "string" && body.message.length > 0) {
          message = body.message;
        }
      } catch {
        // response body wasn't JSON (or was empty) - fall back to the generic message above
      }
      throw new Error(message);
    }
    return response;
  } finally {
    clearTimeout(timeout);
  }
}

/** Spring Boot responses are wrapped in ApiResponse<T> = { success, message, data, timestamp }. */
async function fetchSpring<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetchWithTimeout(url, init);
  const body = await response.json();
  return body.data as T;
}

/** FastAPI ML service responses are returned directly (no envelope). */
async function fetchMl<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetchWithTimeout(url, init);
  return (await response.json()) as T;
}

async function withFallback<T>(fetcher: () => Promise<T>, mockData: T): Promise<ApiResult<T>> {
  try {
    const data = await fetcher();
    return { data, usingMockData: false };
  } catch {
    return { data: mockData, usingMockData: true };
  }
}

export async function getCustomers(): Promise<ApiResult<Customer[]>> {
  return withFallback(() => fetchSpring<Customer[]>(endpoints.customers), mockCustomers);
}

export async function getCustomerRiskProfile(customerId: string): Promise<ApiResult<CustomerRiskProfile | null>> {
  return withFallback(
    () => fetchSpring<CustomerRiskProfile>(endpoints.customerRiskProfile(customerId)),
    buildMockCustomerRiskProfile(customerId),
  );
}

export async function getLinkedCustomers(customerId: string): Promise<ApiResult<LinkedCustomer[]>> {
  return withFallback(() => fetchSpring<LinkedCustomer[]>(endpoints.linkedCustomers(customerId)), []);
}

// --- Customer lock workflow ---------------------------------------------------------------

export async function lockCustomer(customerId: string, reason: string): Promise<CustomerLockRequest> {
  return fetchSpring<CustomerLockRequest>(endpoints.customerLock(customerId), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ reason }),
  });
}

export async function unlockCustomer(customerId: string): Promise<void> {
  await fetchSpring<void>(endpoints.customerUnlock(customerId), { method: "POST" });
}

export async function getCustomerLockRequests(customerId: string): Promise<CustomerLockRequest[]> {
  return fetchSpring<CustomerLockRequest[]>(endpoints.customerLockRequests(customerId));
}

export async function getPendingLockRequests(): Promise<ApiResult<CustomerLockRequest[]>> {
  return withFallback(() => fetchSpring<CustomerLockRequest[]>(endpoints.pendingLockRequests), []);
}

export async function approveLockRequest(lockRequestId: string, notes?: string): Promise<CustomerLockRequest> {
  return fetchSpring<CustomerLockRequest>(endpoints.lockRequestApprove(lockRequestId), {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ notes }),
  });
}

export async function rejectLockRequest(lockRequestId: string, notes?: string): Promise<CustomerLockRequest> {
  return fetchSpring<CustomerLockRequest>(endpoints.lockRequestReject(lockRequestId), {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ notes }),
  });
}

export async function getAccounts(): Promise<ApiResult<Account[]>> {
  return withFallback(() => fetchSpring<Account[]>(endpoints.accounts), mockAccounts);
}

export async function getTransactions(): Promise<ApiResult<BankingTransaction[]>> {
  return withFallback(() => fetchSpring<BankingTransaction[]>(endpoints.transactions), mockTransactions);
}

export async function getRiskScores(): Promise<ApiResult<RiskScore[]>> {
  return withFallback(() => fetchSpring<RiskScore[]>(endpoints.riskScores), mockRiskScores);
}

export async function getAlerts(): Promise<ApiResult<FraudAlert[]>> {
  return withFallback(() => fetchSpring<FraudAlert[]>(endpoints.alerts), mockAlerts);
}

export async function updateAlertStatus(alertId: string, payload: { status: string }): Promise<ApiResult<FraudAlert | null>> {
  try {
    const data = await fetchSpring<FraudAlert>(endpoints.alertStatus(alertId), {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    return { data, usingMockData: false };
  } catch {
    return { data: null, usingMockData: true };
  }
}

export async function assignAlert(alertId: string, payload: { assignedTo: string }): Promise<ApiResult<FraudAlert | null>> {
  try {
    const data = await fetchSpring<FraudAlert>(endpoints.alertAssign(alertId), {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    return { data, usingMockData: false };
  } catch {
    return { data: null, usingMockData: true };
  }
}

export async function bulkAssignAlerts(alertIds: string[], assignedTo: string): Promise<BulkOperationResponse> {
  return fetchSpring<BulkOperationResponse>(endpoints.alertBulkAssign, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ alertIds, assignedTo }),
  });
}

export async function bulkEscalateAlerts(
  alertIds: string[],
  escalatedTo: string,
  reason?: string,
): Promise<BulkOperationResponse> {
  return fetchSpring<BulkOperationResponse>(endpoints.alertBulkEscalate, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ alertIds, escalatedTo, reason }),
  });
}

export async function bulkUpdateAlertStatus(alertIds: string[], status: string): Promise<BulkOperationResponse> {
  return fetchSpring<BulkOperationResponse>(endpoints.alertBulkStatus, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ alertIds, status }),
  });
}

export async function getNotifications(since?: string): Promise<NotificationItem[]> {
  return fetchSpring<NotificationItem[]>(endpoints.notifications(since));
}

export async function bulkUpdateCases(
  caseIds: string[],
  payload: { status?: string; assignedTo?: string },
): Promise<BulkOperationResponse> {
  return fetchSpring<BulkOperationResponse>(endpoints.caseBulkUpdate, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ caseIds, ...payload }),
  });
}

export async function getCases(): Promise<ApiResult<InvestigationCase[]>> {
  return withFallback(() => fetchSpring<InvestigationCase[]>(endpoints.cases), mockCases);
}

export async function getCaseById(caseId: string): Promise<ApiResult<InvestigationCase | null>> {
  return withFallback(
    () => fetchSpring<InvestigationCase>(endpoints.caseById(caseId)),
    mockCases.find((c) => c.caseId === caseId) ?? null,
  );
}

export async function createCase(payload: {
  alertId: string;
  assignedTo?: string;
  notes?: string;
}): Promise<ApiResult<InvestigationCase | null>> {
  try {
    const data = await fetchSpring<InvestigationCase>(endpoints.cases, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    return { data, usingMockData: false };
  } catch {
    return { data: null, usingMockData: true };
  }
}

export async function updateCase(
  caseId: string,
  payload: { status?: string; assignedTo?: string; notes?: string },
): Promise<ApiResult<InvestigationCase | null>> {
  try {
    const data = await fetchSpring<InvestigationCase>(endpoints.caseById(caseId), {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    return { data, usingMockData: false };
  } catch {
    return { data: null, usingMockData: true };
  }
}

export async function updateCaseDecision(caseId: string, decision: string): Promise<ApiResult<InvestigationCase | null>> {
  try {
    const data = await fetchSpring<InvestigationCase>(endpoints.caseDecision(caseId), {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ decision }),
    });
    return { data, usingMockData: false };
  } catch {
    return { data: null, usingMockData: true };
  }
}

export async function getAuditLogs(): Promise<ApiResult<AuditLog[]>> {
  return withFallback(() => fetchSpring<AuditLog[]>(endpoints.auditLogs), mockAuditLogs);
}

export async function getPipelineRuns(): Promise<ApiResult<PipelineRun[]>> {
  return withFallback(() => fetchSpring<PipelineRun[]>(endpoints.pipelineRuns), mockPipelineRuns);
}

export async function getPipelineMetrics(): Promise<ApiResult<PipelineMetrics>> {
  return withFallback(() => fetchSpring<PipelineMetrics>(endpoints.pipelineMetrics), mockPipelineMetrics);
}

export async function uploadIngestionCsv(file: File): Promise<IngestionRun> {
  const formData = new FormData();
  formData.append("file", file);
  return fetchSpring<IngestionRun>(endpoints.ingestionUpload, { method: "POST", body: formData });
}

export async function getDataQualityRuns(): Promise<ApiResult<DataQualityRun[]>> {
  return withFallback(() => fetchSpring<DataQualityRun[]>(endpoints.dataQualityRuns), mockDataQualityRuns);
}

export async function getDataQualityResults(): Promise<ApiResult<DataQualityResult[]>> {
  return withFallback(() => fetchSpring<DataQualityResult[]>(endpoints.dataQualityResults), mockDataQualityResults);
}

export async function getDeadLetterTransactions(): Promise<ApiResult<DeadLetterTransaction[]>> {
  return withFallback(() => fetchSpring<DeadLetterTransaction[]>(endpoints.deadLetterTransactions), mockDeadLetterTransactions);
}

export async function retryDeadLetterTransaction(id: string): Promise<ApiResult<DeadLetterTransaction | null>> {
  try {
    const data = await fetchSpring<DeadLetterTransaction>(endpoints.deadLetterRetry(id), { method: "POST" });
    return { data, usingMockData: false };
  } catch {
    return { data: null, usingMockData: true };
  }
}

export async function ignoreDeadLetterTransaction(id: string): Promise<ApiResult<DeadLetterTransaction | null>> {
  try {
    const data = await fetchSpring<DeadLetterTransaction>(endpoints.deadLetterIgnore(id), { method: "PATCH" });
    return { data, usingMockData: false };
  } catch {
    return { data: null, usingMockData: true };
  }
}

export async function getMlHealth(): Promise<ApiResult<MlHealth>> {
  return withFallback(() => fetchMl<MlHealth>(endpoints.mlHealth), mockMlHealth);
}

export async function getMlModelInfo(): Promise<ApiResult<MlModelInfo>> {
  return withFallback(() => fetchMl<MlModelInfo>(endpoints.mlModelInfo), mockMlModelInfo);
}

// --- Model monitoring and drift detection -------------------------------------------------

export async function getModelMonitoringSummary(): Promise<ApiResult<ModelMonitoringSummary>> {
  return withFallback(() => fetchMl<ModelMonitoringSummary>(endpoints.mlMonitoringSummary), mockModelMonitoringSummary);
}

export async function getModelPredictions(): Promise<ApiResult<MlPredictionLog[]>> {
  return withFallback(() => fetchMl<MlPredictionLog[]>(endpoints.mlMonitoringPredictions), mockMlPredictions);
}

export async function getModelScoreDistribution(): Promise<ApiResult<ScoreDistribution>> {
  return withFallback(() => fetchMl<ScoreDistribution>(endpoints.mlMonitoringScoreDistribution), mockScoreDistribution);
}

export async function getModelDrift(): Promise<ApiResult<FeatureDriftResult[]>> {
  return withFallback(() => fetchMl<FeatureDriftResult[]>(endpoints.mlMonitoringDrift), mockFeatureDrift);
}

export async function getRetrainingHistory(): Promise<ApiResult<RetrainingHistoryEntry[]>> {
  return withFallback(
    () => fetchMl<RetrainingHistoryEntry[]>(endpoints.mlMonitoringRetrainingHistory),
    mockRetrainingHistory,
  );
}

/**
 * Spring Boot doesn't expose a single dashboard-summary endpoint, so this composes several
 * calls and derives the KPIs client-side. If any core call fails, the whole summary falls back
 * to mock data (rather than a part-real/part-mock blend) to keep the demo-data banner meaningful.
 */
export async function getDashboardSummary(): Promise<ApiResult<DashboardSummary>> {
  try {
    const [transactions, riskScores, alerts, cases, pipelineMetrics, mlHealth] = await Promise.all([
      fetchSpring<BankingTransaction[]>(endpoints.transactions),
      fetchSpring<RiskScore[]>(endpoints.riskScores),
      fetchSpring<FraudAlert[]>(endpoints.alerts),
      fetchSpring<InvestigationCase[]>(endpoints.cases),
      fetchSpring<PipelineMetrics>(endpoints.pipelineMetrics),
      fetchMl<MlHealth>(endpoints.mlHealth).catch(() => null),
    ]);

    const highRisk = riskScores.filter((r) => r.riskLevel === "HIGH").length;
    const criticalRisk = riskScores.filter((r) => r.riskLevel === "CRITICAL").length;
    const openAlerts = alerts.filter((a) => a.status === "OPEN" || a.status === "INVESTIGATING" || a.status === "ACKNOWLEDGED").length;
    const confirmedCases = cases.filter((c) => c.decision === "CONFIRMED_FRAUD").length;
    const falsePositives = alerts.filter((a) => a.status === "FALSE_POSITIVE").length;
    const falsePositiveRate = alerts.length > 0 ? (falsePositives / alerts.length) * 100 : 0;
    const averageRiskScore = riskScores.length > 0
      ? riskScores.reduce((sum, r) => sum + r.finalScore, 0) / riskScores.length
      : 0;

    const riskDistribution = (["LOW", "MEDIUM", "HIGH", "CRITICAL"] as const).map((riskLevel) => ({
      riskLevel,
      count: riskScores.filter((r) => r.riskLevel === riskLevel).length,
    }));

    const summary: DashboardSummary = {
      totalTransactions: transactions.length,
      highRiskTransactions: highRisk,
      criticalRiskTransactions: criticalRisk,
      openFraudAlerts: openAlerts,
      confirmedFraudCases: confirmedCases,
      falsePositiveRate,
      averageRiskScore,
      pipelineSuccessRate: pipelineMetrics.successRatePercent,
      recordsProcessedToday: transactions.length,
      mlServiceOnline: mlHealth !== null,
      fraudTrend: mockDashboardSummary.fraudTrend,
      riskDistribution,
      alertStatusDistribution: mockDashboardSummary.alertStatusDistribution,
      pipelineRecordsChart: mockDashboardSummary.pipelineRecordsChart,
      recentAlerts: alerts.slice(0, 5),
    };

    return { data: summary, usingMockData: false };
  } catch {
    return { data: mockDashboardSummary, usingMockData: true };
  }
}

// --- Auth / user management / test transactions -----------------------------------------
// These hit the live backend directly (no mock fallback - there's nothing meaningful to fake
// for a login or an admin action) and let ApiAuthError/Error propagate to the caller.

export async function login(username: string, password: string): Promise<LoginResponse> {
  return fetchSpring<LoginResponse>(endpoints.login, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
}

export async function getCurrentUser(): Promise<AppUser> {
  return fetchSpring<AppUser>(endpoints.me);
}

export async function getUsers(): Promise<AppUser[]> {
  return fetchSpring<AppUser[]>(endpoints.users);
}

export async function getAssignableUsers(): Promise<AssignableUser[]> {
  return fetchSpring<AssignableUser[]>(endpoints.assignableUsers);
}

export async function createUser(payload: CreateUserPayload): Promise<AppUser> {
  return fetchSpring<AppUser>(endpoints.users, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function updateUser(userId: string, payload: UpdateUserPayload): Promise<AppUser> {
  return fetchSpring<AppUser>(endpoints.userById(userId), {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function updateUserStatus(userId: string, status: "ACTIVE" | "DISABLED" | "LOCKED"): Promise<AppUser> {
  return fetchSpring<AppUser>(endpoints.userStatus(userId), {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
  });
}

export async function resetUserPassword(userId: string, newPassword: string): Promise<AppUser> {
  return fetchSpring<AppUser>(endpoints.userResetPassword(userId), {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ newPassword }),
  });
}

export async function disableUser(userId: string): Promise<AppUser> {
  return fetchSpring<AppUser>(endpoints.userById(userId), { method: "DELETE" });
}

export async function submitTestTransaction(payload: TestTransactionPayload): Promise<TestTransactionResult> {
  return fetchSpring<TestTransactionResult>(endpoints.testTransactions, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

// --- Fraud rules management --------------------------------------------------------------

export async function getFraudRules(): Promise<ApiResult<FraudRule[]>> {
  return withFallback(() => fetchSpring<FraudRule[]>(endpoints.fraudRules), mockFraudRules);
}

export async function getFraudRuleById(ruleId: string): Promise<FraudRule> {
  return fetchSpring<FraudRule>(endpoints.fraudRuleById(ruleId));
}

export async function createFraudRule(payload: FraudRulePayload): Promise<FraudRule> {
  return fetchSpring<FraudRule>(endpoints.fraudRules, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function updateFraudRule(ruleId: string, payload: FraudRulePayload): Promise<FraudRule> {
  return fetchSpring<FraudRule>(endpoints.fraudRuleById(ruleId), {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function enableFraudRule(ruleId: string): Promise<FraudRule> {
  return fetchSpring<FraudRule>(endpoints.fraudRuleEnable(ruleId), { method: "PATCH" });
}

export async function disableFraudRule(ruleId: string): Promise<FraudRule> {
  return fetchSpring<FraudRule>(endpoints.fraudRuleDisable(ruleId), { method: "PATCH" });
}

export async function getFraudRuleVersions(ruleId: string): Promise<ApiResult<FraudRuleVersion[]>> {
  return withFallback(
    () => fetchSpring<FraudRuleVersion[]>(endpoints.fraudRuleVersions(ruleId)),
    mockFraudRuleVersions.filter((v) => v.ruleId === ruleId),
  );
}

// --- Case timeline and analyst notes ------------------------------------------------------

export async function getCaseTimeline(caseId: string): Promise<ApiResult<CaseTimelineEvent[]>> {
  return withFallback(
    () => fetchSpring<CaseTimelineEvent[]>(endpoints.caseTimeline(caseId)),
    mockCaseTimelineEvents.filter((e) => e.caseId === caseId),
  );
}

export async function getCaseNotes(caseId: string): Promise<ApiResult<CaseNote[]>> {
  return withFallback(
    () => fetchSpring<CaseNote[]>(endpoints.caseNotes(caseId)),
    mockCaseNotes.filter((n) => n.caseId === caseId),
  );
}

export async function addCaseNote(caseId: string, payload: CaseNotePayload): Promise<CaseNote> {
  return fetchSpring<CaseNote>(endpoints.caseNotes(caseId), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function updateCaseNote(caseId: string, noteId: string, payload: CaseNotePayload): Promise<CaseNote> {
  return fetchSpring<CaseNote>(endpoints.caseNoteById(caseId, noteId), {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function deleteCaseNote(caseId: string, noteId: string): Promise<void> {
  await fetchWithTimeout(endpoints.caseNoteById(caseId, noteId), { method: "DELETE" });
}

export async function getCaseStatusHistory(caseId: string): Promise<ApiResult<CaseStatusHistory[]>> {
  return withFallback(
    () => fetchSpring<CaseStatusHistory[]>(endpoints.caseStatusHistory(caseId)),
    mockCaseStatusHistory.filter((h) => h.caseId === caseId),
  );
}

// --- Analytics & reporting -----------------------------------------------------------------

export async function getAnalyticsSummary(): Promise<ApiResult<AnalyticsSummary>> {
  return withFallback(() => fetchSpring<AnalyticsSummary>(endpoints.analyticsSummary), mockAnalyticsSummary);
}

// --- Alert SLA and escalation monitoring --------------------------------------------------

export async function getSlaSummary(): Promise<ApiResult<SlaSummary>> {
  return withFallback(() => fetchSpring<SlaSummary>(endpoints.slaSummary), mockSlaSummary);
}

export async function getSlaAlerts(): Promise<ApiResult<AlertSlaResult[]>> {
  return withFallback(() => fetchSpring<AlertSlaResult[]>(endpoints.slaAlerts), mockSlaResults);
}

export async function getBreachedSlaAlerts(): Promise<ApiResult<AlertSlaResult[]>> {
  return withFallback(
    () => fetchSpring<AlertSlaResult[]>(endpoints.slaBreached),
    mockSlaResults.filter((r) => r.status === "BREACHED"),
  );
}

export async function getNearBreachSlaAlerts(): Promise<ApiResult<AlertSlaResult[]>> {
  return withFallback(
    () => fetchSpring<AlertSlaResult[]>(endpoints.slaNearBreach),
    mockSlaResults.filter((r) => r.status === "NEAR_BREACH"),
  );
}

export async function getSlaPolicies(): Promise<ApiResult<AlertSlaPolicy[]>> {
  return withFallback(() => fetchSpring<AlertSlaPolicy[]>(endpoints.slaPolicies), mockSlaPolicies);
}

export async function updateSlaPolicy(policyId: string, payload: AlertSlaPolicyPayload): Promise<AlertSlaPolicy> {
  return fetchSpring<AlertSlaPolicy>(endpoints.slaPolicyById(policyId), {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function escalateAlert(alertId: string, payload: AlertEscalationPayload): Promise<AlertEscalation> {
  return fetchSpring<AlertEscalation>(endpoints.alertEscalate(alertId), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function getAlertStatusHistory(alertId: string): Promise<ApiResult<AlertStatusHistory[]>> {
  return withFallback(
    () => fetchSpring<AlertStatusHistory[]>(endpoints.alertStatusHistory(alertId)),
    mockAlertStatusHistory.filter((h) => h.alertId === alertId),
  );
}

export async function getAlertEscalations(alertId: string): Promise<ApiResult<AlertEscalation[]>> {
  return withFallback(
    () => fetchSpring<AlertEscalation[]>(endpoints.alertEscalations(alertId)),
    mockAlertEscalations.filter((e) => e.alertId === alertId),
  );
}

// --- Kafka/Redpanda streaming ingestion ---------------------------------------------------

export async function publishStreamingTransaction(payload: PublishStreamingTransactionPayload): Promise<string> {
  return fetchSpring<string>(endpoints.streamingPublish, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function getStreamingMetrics(): Promise<ApiResult<StreamingMetric>> {
  return withFallback(() => fetchSpring<StreamingMetric>(endpoints.streamingMetrics), mockStreamingMetric);
}

export async function getStreamingEvents(): Promise<ApiResult<StreamingEventLog[]>> {
  return withFallback(() => fetchSpring<StreamingEventLog[]>(endpoints.streamingEvents), mockStreamingEvents);
}

export async function getStreamingDeadLetterEvents(): Promise<ApiResult<DeadLetterEvent[]>> {
  return withFallback(
    () => fetchSpring<DeadLetterEvent[]>(endpoints.streamingDeadLetterEvents),
    mockStreamingDeadLetterEvents,
  );
}

export async function retryStreamingDeadLetterEvent(eventId: string): Promise<DeadLetterEvent> {
  return fetchSpring<DeadLetterEvent>(endpoints.streamingDeadLetterRetry(eventId), { method: "POST" });
}

export async function ignoreStreamingDeadLetterEvent(eventId: string): Promise<DeadLetterEvent> {
  return fetchSpring<DeadLetterEvent>(endpoints.streamingDeadLetterIgnore(eventId), { method: "PATCH" });
}
