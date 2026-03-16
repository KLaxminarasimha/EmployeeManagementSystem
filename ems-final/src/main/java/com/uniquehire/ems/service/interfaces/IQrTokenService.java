package com.uniquehire.ems.service.interfaces;

public interface IQrTokenService {

    /** Generate a new token and store in Redis. Runs every 30s via @Scheduled. */
    void rotateToken();

    /** Get current active token. Auto-rotates if none found. */
    String getCurrentToken();

    /** Generate QR code PNG bytes embedding the current token. */
    byte[] generateQrCodeImage() throws Exception;

    /** Returns true if token exists in Redis and has not expired. */
    boolean isTokenValid(String token);

    /** Delete token from Redis after use (one-time use enforcement). */
    void invalidateToken(String token);

    /** Remaining TTL in milliseconds for the current token (for countdown timer). */
    long getCurrentTokenTtlMs();
}
