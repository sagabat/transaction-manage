package com.bank.transaction.service;

import com.bank.transaction.exception.InvalidTokenException;
import com.bank.transaction.util.TokenUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TokenUtilTest {
    private final TokenUtil tokenUtil = new TokenUtil();

    @Test
    void testGenerateToken() {
        String token = tokenUtil.generateToken();
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testValidateToken_Success() {
        String token = tokenUtil.generateToken();
        tokenUtil.validateToken(token);
    }

    @Test
    void testValidateToken_Invalid() {
        assertThrows(InvalidTokenException.class, () -> tokenUtil.validateToken(null));
        assertThrows(InvalidTokenException.class, () -> tokenUtil.validateToken(""));
    }
}
