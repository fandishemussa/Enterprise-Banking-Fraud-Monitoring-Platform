"use client";

import { useState } from "react";
import { disableFraudRule, enableFraudRule, getFraudRules, updateFraudRule } from "@/lib/api";
import { useApiResource } from "@/hooks/use-api-resource";
import type { FraudRulePayload } from "@/types";

export function useFraudRules() {
  const resource = useApiResource(getFraudRules);
  const [mutating, setMutating] = useState(false);

  async function setEnabled(ruleId: string, enabled: boolean) {
    setMutating(true);
    try {
      const result = enabled ? await enableFraudRule(ruleId) : await disableFraudRule(ruleId);
      resource.refetch();
      return result;
    } finally {
      setMutating(false);
    }
  }

  async function update(ruleId: string, payload: FraudRulePayload) {
    setMutating(true);
    try {
      const result = await updateFraudRule(ruleId, payload);
      resource.refetch();
      return result;
    } finally {
      setMutating(false);
    }
  }

  return { ...resource, mutating, setEnabled, update };
}
