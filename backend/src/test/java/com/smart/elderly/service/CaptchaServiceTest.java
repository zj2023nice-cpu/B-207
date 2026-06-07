package com.smart.elderly.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CaptchaService 验证码服务测试")
class CaptchaServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    @InjectMocks
    private CaptchaService captchaService;

    private static final String TEST_CODE = "abcd";
    private static final String TEST_UUID = "test-uuid-12345";
    private static final String TEST_IMAGE = "data:image/png;base64,testimage";
    private static final String CAPTCHA_KEY = CaptchaService.CAPTCHA_PREFIX + TEST_UUID;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("generateCaptcha - 生成验证码并存储到Redis")
    void testGenerateCaptcha_ShouldStoreInRedis() {
        doReturn(TEST_CODE).when(captchaService).generateCode();
        doReturn(TEST_UUID).when(captchaService).generateUuid();
        doReturn(TEST_IMAGE).when(captchaService).generateCaptchaImage(TEST_CODE);

        Map<String, String> result = captchaService.generateCaptcha();

        assertNotNull(result);
        assertEquals(TEST_UUID, result.get("uuid"));
        assertEquals(TEST_IMAGE, result.get("image"));
        verify(valueOperations).set(eq(CAPTCHA_KEY), eq(TEST_CODE.toLowerCase()),
                eq(CaptchaService.CAPTCHA_EXPIRE_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("generateCaptcha - 返回结果包含uuid和image字段")
    void testGenerateCaptcha_ResultContainsRequiredFields() {
        doReturn(TEST_CODE).when(captchaService).generateCode();
        doReturn(TEST_UUID).when(captchaService).generateUuid();
        doReturn(TEST_IMAGE).when(captchaService).generateCaptchaImage(TEST_CODE);

        Map<String, String> result = captchaService.generateCaptcha();

        assertTrue(result.containsKey("uuid"), "结果应包含uuid字段");
        assertTrue(result.containsKey("image"), "结果应包含image字段");
        assertNotNull(result.get("uuid"), "uuid不应为null");
        assertNotNull(result.get("image"), "image不应为null");
    }

    @Test
    @DisplayName("generateCaptcha - 验证码以小写形式存储")
    void testGenerateCaptcha_ShouldStoreCodeInLowerCase() {
        String mixedCaseCode = "AbCd123";
        doReturn(mixedCaseCode).when(captchaService).generateCode();
        doReturn(TEST_UUID).when(captchaService).generateUuid();
        doReturn(TEST_IMAGE).when(captchaService).generateCaptchaImage(mixedCaseCode);

        captchaService.generateCaptcha();

        verify(valueOperations).set(eq(CAPTCHA_KEY), eq(mixedCaseCode.toLowerCase()),
                anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("generateCaptcha - 设置正确的过期时间")
    void testGenerateCaptcha_ShouldSetCorrectExpireTime() {
        doReturn(TEST_CODE).when(captchaService).generateCode();
        doReturn(TEST_UUID).when(captchaService).generateUuid();
        doReturn(TEST_IMAGE).when(captchaService).generateCaptchaImage(TEST_CODE);

        captchaService.generateCaptcha();

        verify(valueOperations).set(anyString(), anyString(),
                eq(CaptchaService.CAPTCHA_EXPIRE_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("validateCaptcha - 正确验证码返回true")
    void testValidateCaptcha_CorrectCode_ShouldReturnTrue() {
        when(valueOperations.get(CAPTCHA_KEY)).thenReturn(TEST_CODE.toLowerCase());

        boolean result = captchaService.validateCaptcha(TEST_UUID, TEST_CODE);

        assertTrue(result);
    }

    @Test
    @DisplayName("validateCaptcha - 验证后删除验证码（一次性使用）")
    void testValidateCaptcha_ShouldDeleteAfterValidation() {
        when(valueOperations.get(CAPTCHA_KEY)).thenReturn(TEST_CODE.toLowerCase());

        captchaService.validateCaptcha(TEST_UUID, TEST_CODE);

        verify(stringRedisTemplate).delete(CAPTCHA_KEY);
    }

    @Test
    @DisplayName("validateCaptcha - 验证正确也会删除，防止重放攻击")
    void testValidateCaptcha_CorrectCode_ShouldDeleteCaptcha() {
        when(valueOperations.get(CAPTCHA_KEY)).thenReturn(TEST_CODE.toLowerCase());

        boolean firstResult = captchaService.validateCaptcha(TEST_UUID, TEST_CODE);
        assertTrue(firstResult);

        when(valueOperations.get(CAPTCHA_KEY)).thenReturn(null);
        boolean secondResult = captchaService.validateCaptcha(TEST_UUID, TEST_CODE);
        assertFalse(secondResult, "验证码应只能使用一次");
    }

    @Test
    @DisplayName("validateCaptcha - 错误验证码返回false")
    void testValidateCaptcha_WrongCode_ShouldReturnFalse() {
        when(valueOperations.get(CAPTCHA_KEY)).thenReturn(TEST_CODE.toLowerCase());

        boolean result = captchaService.validateCaptcha(TEST_UUID, "wrong");

        assertFalse(result);
        verify(stringRedisTemplate).delete(CAPTCHA_KEY);
    }

    @Test
    @DisplayName("validateCaptcha - 错误验证码也删除，防止暴力破解")
    void testValidateCaptcha_WrongCode_ShouldDeleteCaptcha() {
        when(valueOperations.get(CAPTCHA_KEY)).thenReturn(TEST_CODE.toLowerCase());

        captchaService.validateCaptcha(TEST_UUID, "wrong");

        verify(stringRedisTemplate).delete(CAPTCHA_KEY);
    }

    @Test
    @DisplayName("validateCaptcha - 验证码不区分大小写")
    void testValidateCaptcha_CaseInsensitive_ShouldReturnTrue() {
        when(valueOperations.get(CAPTCHA_KEY)).thenReturn(TEST_CODE.toLowerCase());

        assertTrue(captchaService.validateCaptcha(TEST_UUID, TEST_CODE.toUpperCase()));
        assertTrue(captchaService.validateCaptcha(TEST_UUID, "AbCd"));
    }

    @Test
    @DisplayName("validateCaptcha - uuid为null时返回false")
    void testValidateCaptcha_NullUuid_ShouldReturnFalse() {
        boolean result = captchaService.validateCaptcha(null, TEST_CODE);

        assertFalse(result);
        verify(valueOperations, never()).get(anyString());
        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("validateCaptcha - code为null时返回false")
    void testValidateCaptcha_NullCode_ShouldReturnFalse() {
        boolean result = captchaService.validateCaptcha(TEST_UUID, null);

        assertFalse(result);
        verify(valueOperations, never()).get(anyString());
        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("validateCaptcha - code为空字符串时返回false")
    void testValidateCaptcha_EmptyCode_ShouldReturnFalse() {
        assertFalse(captchaService.validateCaptcha(TEST_UUID, ""));
        assertFalse(captchaService.validateCaptcha(TEST_UUID, "   "));
        verify(valueOperations, never()).get(anyString());
        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("validateCaptcha - 验证码不存在（过期或已使用）时返回false")
    void testValidateCaptcha_CodeNotExists_ShouldReturnFalse() {
        when(valueOperations.get(CAPTCHA_KEY)).thenReturn(null);

        boolean result = captchaService.validateCaptcha(TEST_UUID, TEST_CODE);

        assertFalse(result);
        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("一次性校验行为 - 连续两次校验第二次失败")
    void testOneTimeValidation_SecondValidation_ShouldFail() {
        when(valueOperations.get(CAPTCHA_KEY))
                .thenReturn(TEST_CODE.toLowerCase())
                .thenReturn(null);

        boolean firstResult = captchaService.validateCaptcha(TEST_UUID, TEST_CODE);
        boolean secondResult = captchaService.validateCaptcha(TEST_UUID, TEST_CODE);

        assertTrue(firstResult, "第一次校验应通过");
        assertFalse(secondResult, "第二次校验应失败（验证码已被删除）");
    }

    @Test
    @DisplayName("组合场景 - 生成验证码后正确校验成功")
    void testFullScenario_GenerateThenValidateCorrectly() {
        doReturn(TEST_CODE).when(captchaService).generateCode();
        doReturn(TEST_UUID).when(captchaService).generateUuid();
        doReturn(TEST_IMAGE).when(captchaService).generateCaptchaImage(TEST_CODE);
        when(valueOperations.get(CAPTCHA_KEY)).thenReturn(TEST_CODE.toLowerCase());

        Map<String, String> generated = captchaService.generateCaptcha();
        boolean valid = captchaService.validateCaptcha(generated.get("uuid"), TEST_CODE);

        assertTrue(valid, "生成后使用正确验证码应校验通过");
    }

    @Test
    @DisplayName("组合场景 - 生成验证码后用错误验证码校验失败")
    void testFullScenario_GenerateThenValidateWithWrongCode() {
        doReturn(TEST_CODE).when(captchaService).generateCode();
        doReturn(TEST_UUID).when(captchaService).generateUuid();
        doReturn(TEST_IMAGE).when(captchaService).generateCaptchaImage(TEST_CODE);
        when(valueOperations.get(CAPTCHA_KEY)).thenReturn(TEST_CODE.toLowerCase());

        Map<String, String> generated = captchaService.generateCaptcha();
        boolean valid = captchaService.validateCaptcha(generated.get("uuid"), "wrong");

        assertFalse(valid, "使用错误验证码应校验失败");
    }
}
