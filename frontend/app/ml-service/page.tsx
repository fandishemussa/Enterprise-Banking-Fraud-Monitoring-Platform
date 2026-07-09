"use client";

import { CheckCircle2, Cpu, Database, XCircle } from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
import { SectionCard } from "@/components/shared/section-card";
import { MetricCard } from "@/components/dashboard/metric-card";
import { Badge } from "@/components/ui/badge";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { useMlHealth, useMlModelInfo } from "@/hooks/use-ml-service";
import { formatDate, formatNumber } from "@/lib/formatters";

export default function MlServicePage() {
  const { data: health, isLoading: healthLoading, error: healthError, usingMockData: healthMock, refetch: refetchHealth } = useMlHealth();
  const { data: modelInfo, isLoading: modelLoading, error: modelError, usingMockData: modelMock, refetch: refetchModel } = useMlModelInfo();

  const usingMockData = healthMock || modelMock;

  return (
    <div className="space-y-6">
      <PageHeader title="ML Service" description="FastAPI fraud scoring service health and model information" />
      {usingMockData && <DemoDataBanner />}

      {healthLoading ? (
        <LoadingState label="Checking ML service health..." />
      ) : healthError || !health ? (
        <ErrorState message={healthError ?? "ML service unavailable."} onRetry={refetchHealth} />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <MetricCard label="Service Status" value={health.status} icon={Cpu} tone={health.status === "healthy" ? "success" : "destructive"} />
          <MetricCard label="Model Loaded" value={health.modelLoaded ? "Yes" : "No"} icon={CheckCircle2} tone={health.modelLoaded ? "success" : "warning"} />
          <MetricCard label="Fallback Mode" value={health.fallbackMode ? "Active" : "Inactive"} icon={XCircle} tone={health.fallbackMode ? "warning" : "success"} />
          <MetricCard label="Dataset Available" value={health.datasetAvailable ? "Yes" : "No"} icon={Database} tone={health.datasetAvailable ? "success" : "neutral"} />
        </div>
      )}

      <SectionCard title="Model Information" description="Details about the currently loaded fraud detection model">
        {modelLoading ? (
          <LoadingState label="Loading model info..." />
        ) : modelError || !modelInfo ? (
          <ErrorState message={modelError ?? "Unable to load model info."} onRetry={refetchModel} />
        ) : (
          <div className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <div>
                <p className="text-xs text-muted-foreground">Model Name</p>
                <p className="text-sm font-medium text-foreground">{modelInfo.modelName ?? "Not trained yet"}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Model Type</p>
                <p className="text-sm font-medium text-foreground">{modelInfo.modelType ?? "-"}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Model Version</p>
                <p className="text-sm font-medium text-foreground">{modelInfo.modelVersion ?? "-"}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Training Date</p>
                <p className="text-sm font-medium text-foreground">{formatDate(modelInfo.trainingDate)}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Dataset</p>
                <p className="text-sm font-medium text-foreground">{modelInfo.datasetName ?? "-"}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Training Rows</p>
                <p className="text-sm font-medium text-foreground">
                  {modelInfo.numberOfTrainingRows ? formatNumber(modelInfo.numberOfTrainingRows) : "-"}
                </p>
              </div>
            </div>

            <div>
              <p className="mb-2 text-xs text-muted-foreground">Feature List</p>
              <div className="flex flex-wrap gap-1.5">
                {modelInfo.featureList.length ? (
                  modelInfo.featureList.map((feature) => (
                    <Badge key={feature} variant="neutral">
                      {feature}
                    </Badge>
                  ))
                ) : (
                  <span className="text-sm text-muted-foreground">No features available</span>
                )}
              </div>
            </div>
          </div>
        )}
      </SectionCard>
    </div>
  );
}
