package com.bank.transaction.util;

import com.bank.transaction.exception.InvalidTokenException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token机制：防止“重复提交操作”或者“重放攻击”
 */
@Component
public class TokenUtil {
    private final Map<String, Long> usedTokens = new ConcurrentHashMap<>();
    private static final long TOKEN_VALIDITY_SECONDS = 300;//Token过期时间，5分钟

    // 生成一个一次性的Token，有效期是5分钟，用于实现幂等性操作，防止“重复提交操作”或者“重放攻击”
    public String generateToken() {
        String token = UUID.randomUUID().toString();
        usedTokens.put(token, Instant.now().plusSeconds(TOKEN_VALIDITY_SECONDS).getEpochSecond());
        return token;
    }

    // 校验Token的合法性
    public void validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new InvalidTokenException("无效token");
        }
        // 用一次就删除Token
        Long expiry = usedTokens.remove(token);
        if (expiry == null) {
            throw new InvalidTokenException("无效或已经使用过的token");
        }
        // 判断Token是否在有效期内
        if (Instant.now().getEpochSecond() > expiry) {
            throw new InvalidTokenException("token有效期为5分钟，当前token已经过期，请重新提交");
        }
    }
}
