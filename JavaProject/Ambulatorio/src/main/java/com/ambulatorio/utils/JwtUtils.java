package com.ambulatorio.utils;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtils {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String SECRET_STRING = dotenv.get("JWT_SECRET_KEY");

    private static final long EXPIRATION_TIME_MS = 3600000; // 1 ora

    /**
     * Metodo di supporto privato per convertire la stringa in una chiave crittografica sicura
     */
    private static SecretKey getSigningKey() {
        if (SECRET_STRING == null || SECRET_STRING.length() < 32) {
            throw new IllegalStateException("JWT_SECRET_KEY mancante o troppo corta nel file .env (min 32 caratteri)");
        }
        return Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    }

    public static String generaToken(String id, String email, String ruolo) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiration = new Date(nowMillis + EXPIRATION_TIME_MS);

        return Jwts.builder()
                // Claims standard e custom
                .subject(id)
                .claim("email", email)
                .claim("ruolo", ruolo)

                // Date di emissione e scadenza
                .issuedAt(now)
                .expiration(expiration)

                // Firma digitale con la chiave segreta
                .signWith(getSigningKey())

                // Compattazione in una stringa URL-safe
                .compact();
    }
}
