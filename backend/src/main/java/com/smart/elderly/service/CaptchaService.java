package com.smart.elderly.service;

import com.smart.elderly.util.CaptchaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaService {

    static final String CAPTCHA_PREFIX = "captcha:";
    static final long CAPTCHA_EXPIRE_MINUTES = 5;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Map<String, String> generateCaptcha() {
        String code = generateCode();
        String uuid = generateUuid();
        String base64Image = generateCaptchaImage(code);

        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + uuid, code.toLowerCase(), CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("image", base64Image);
        return result;
    }

    public boolean validateCaptcha(String uuid, String code) {
        if (uuid == null || code == null || code.trim().isEmpty()) {
            return false;
        }
        String storedCode = stringRedisTemplate.opsForValue().get(CAPTCHA_PREFIX + uuid);
        if (storedCode == null) {
            return false;
        }
        stringRedisTemplate.delete(CAPTCHA_PREFIX + uuid);
        return storedCode.equals(code.toLowerCase());
    }

    String generateCode() {
        return CaptchaUtil.generateCode();
    }

    String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    String generateCaptchaImage(String code) {
        return CaptchaUtil.generateCaptchaBase64(code);
    }
}
