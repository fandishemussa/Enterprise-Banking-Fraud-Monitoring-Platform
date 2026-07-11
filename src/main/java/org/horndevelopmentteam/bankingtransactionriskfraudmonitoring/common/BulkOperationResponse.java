package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Generic partial-success result for "do this to N ids" endpoints: one bad id (not found, wrong
 * state, etc.) should not abort the rest of the batch. Callers build one via forEach, which catches
 * and records per-item failures instead of letting an exception propagate and roll back/abort the
 * whole request.
 */
public record BulkOperationResponse(List<String> succeededIds, List<BulkOperationFailure> failures) {

    public static BulkOperationResponse forEach(List<String> ids, Consumer<String> action) {
        List<String> succeeded = new ArrayList<>();
        List<BulkOperationFailure> failures = new ArrayList<>();
        for (String id : ids) {
            try {
                action.accept(id);
                succeeded.add(id);
            } catch (Exception ex) {
                failures.add(new BulkOperationFailure(id, ex.getMessage()));
            }
        }
        return new BulkOperationResponse(succeeded, failures);
    }
}
