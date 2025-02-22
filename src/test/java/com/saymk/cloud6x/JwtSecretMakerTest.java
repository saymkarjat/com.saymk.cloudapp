package com.saymk.cloud6x;

import io.jsonwebtoken.Jwts;
import jakarta.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

public class JwtSecretMakerTest {
    @Test
    public void generateSecretKey() {
        SecretKey key = Jwts.SIG.HS256.key().build();
        String code = DatatypeConverter.printHexBinary(key.getEncoded());
        System.out.printf(code);
    }
}
