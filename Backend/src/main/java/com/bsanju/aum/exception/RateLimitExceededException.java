package com.bsanju.aum.exception;

/**
 * Exception thrown when rate limit is exceeded.
 * Used for API rate limiting and abuse prevention.
 */
public class RateLimitExceededException extends RuntimeException {

    private final String userIdentifier;
    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, String userIdentifier, long retryAfterSeconds) {
        super(message);
        this.userIdentifier = userIdentifier;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public RateLimitExceededException(String userIdentifier, long retryAfterSeconds) {
        super("Rate limit exceeded. Please try again after " + retryAfterSeconds + " seconds.");
        this.userIdentifier = userIdentifier;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
