package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

/** Thrown when a publish to Kafka/Redpanda fails or times out - mapped to 503 so callers get a
 * clear "streaming is down" response instead of a generic 500, and so the rest of the REST API
 * (which doesn't depend on Kafka) is unaffected. */
public class StreamingUnavailableException extends RuntimeException {
    public StreamingUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
