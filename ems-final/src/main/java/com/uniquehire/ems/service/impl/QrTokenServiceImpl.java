package com.uniquehire.ems.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.uniquehire.ems.service.interfaces.IQrTokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class QrTokenServiceImpl implements IQrTokenService {

    private static final Logger log           = LoggerFactory.getLogger(QrTokenServiceImpl.class);
    private static final String CURRENT_KEY   = "qr:current_token";
    private static final String TOKEN_PREFIX  = "qr:token:";
    private static final int    QR_SIZE       = 300;

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.qr.token-ttl-seconds:30}")
    private long tokenTtlSeconds;

    // ── Rotate every 30 s ───────────────────────────────────
    @Override
    @Scheduled(fixedDelayString = "${app.qr.token-ttl-seconds:30}000", initialDelay = 0)
    public void rotateToken() {
        String token     = generateSecureToken();
        long   expiresAt = System.currentTimeMillis() + (tokenTtlSeconds * 1000);
        long   ttl       = tokenTtlSeconds + 5; // 5-s grace period

        redisTemplate.opsForValue().set(
            TOKEN_PREFIX + token, String.valueOf(expiresAt), ttl, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(
            CURRENT_KEY, token, ttl, TimeUnit.SECONDS);

        log.debug("QR token rotated (TTL={}s)", tokenTtlSeconds);
    }

    // ── Current token ────────────────────────────────────────
    @Override
    public String getCurrentToken() {
        String token = redisTemplate.opsForValue().get(CURRENT_KEY);
        if (token == null || token.isBlank()) {
            rotateToken();
            token = redisTemplate.opsForValue().get(CURRENT_KEY);
        }
        return token;
    }

    // ── Generate QR PNG ──────────────────────────────────────
    @Override
    public byte[] generateQrCodeImage() throws Exception {
        String token   = getCurrentToken();
        String payload = "uniquehire://attendance/checkin"
            + "?token=" + token
            + "&ts="    + System.currentTimeMillis()
            + "&ttl="   + getCurrentTokenTtlMs();

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix    matrix = writer.encode(payload, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

    // ── Validate ─────────────────────────────────────────────
    @Override
    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) return false;
        String expiryStr = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (expiryStr == null) {
            log.warn("QR token not found in Redis: {}", token);
            return false;
        }
        boolean valid = System.currentTimeMillis() <= Long.parseLong(expiryStr);
        if (!valid) log.warn("QR token expired: {}", token);
        return valid;
    }

    // ── Invalidate (one-time use) ────────────────────────────
    @Override
    public void invalidateToken(String token) {
        redisTemplate.delete(TOKEN_PREFIX + token);
        log.debug("QR token invalidated: {}", token);
    }

    // ── TTL remaining ────────────────────────────────────────
    @Override
    public long getCurrentTokenTtlMs() {
        Long ttl = redisTemplate.getExpire(CURRENT_KEY, TimeUnit.MILLISECONDS);
        return (ttl != null && ttl > 0) ? ttl : 0L;
    }

    // ── Internal ─────────────────────────────────────────────
    private String generateSecureToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
