package com.smart.elderly.service;

import com.smart.elderly.entity.Elderly;
import com.smart.elderly.entity.HealthRecord;
import com.smart.elderly.entity.HealthWarningThreshold;
import com.smart.elderly.mapper.HealthRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HealthRecordServiceTest {

    @Mock
    private HealthRecordMapper healthRecordMapper;

    @Mock
    private ElderlyService elderlyService;

    @Mock
    private HealthWarningThresholdService thresholdService;

    @Mock
    private HealthWarningRecordService warningRecordService;

    @Mock
    private HealthRecordQualityReviewService qualityReviewService;

    @InjectMocks
    private HealthRecordService healthRecordService;

    private Elderly testElderly;

    @BeforeEach
    void setUp() {
        testElderly = new Elderly();
        testElderly.setId(1);
        testElderly.setName("测试老人");
    }

    private HealthRecord createBasicRecord() {
        HealthRecord record = new HealthRecord();
        record.setElderlyId(1);
        record.setCheckTime(LocalDateTime.now());
        return record;
    }

    private HealthWarningThreshold createThreshold(String indicatorType, 
            BigDecimal highThreshold, BigDecimal lowThreshold, boolean enabled) {
        HealthWarningThreshold threshold = new HealthWarningThreshold();
        threshold.setIndicatorType(indicatorType);
        threshold.setHighThreshold(highThreshold);
        threshold.setLowThreshold(lowThreshold);
        threshold.setEnabled(enabled);
        return threshold;
    }

    @Test
    @DisplayName("测试老人ID为空时抛出异常")
    void testSaveRecord_WithNullElderlyId_ShouldThrowException() {
        HealthRecord record = new HealthRecord();
        record.setElderlyId(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> healthRecordService.saveRecord(record));
        
        assertEquals("老人ID不能为空", exception.getMessage());
        verify(elderlyService, never()).getById(any());
    }

    @Test
    @DisplayName("测试老人不存在时抛出异常")
    void testSaveRecord_WithNonExistentElderly_ShouldThrowException() {
        HealthRecord record = createBasicRecord();
        record.setElderlyId(999);

        when(elderlyService.getById(999)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> healthRecordService.saveRecord(record));
        
        assertEquals("老人不存在，ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("测试体温正常 - 不触发预警")
    void testSaveRecord_WithNormalTemperature_ShouldNotTriggerWarning() {
        HealthRecord record = createBasicRecord();
        record.setTemperature(new BigDecimal("36.5"));

        HealthWarningThreshold threshold = createThreshold("temperature", 
                new BigDecimal("37.3"), null, true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "temperature")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertFalse(record.getIsAbnormal());
        assertNull(record.getAbnormalReason());
        verify(warningRecordService, never()).createWarning(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("测试体温等于阈值 - 边界值测试（不触发预警）")
    void testSaveRecord_WithTemperatureAtThreshold_ShouldNotTriggerWarning() {
        HealthRecord record = createBasicRecord();
        record.setTemperature(new BigDecimal("37.3"));

        HealthWarningThreshold threshold = createThreshold("temperature", 
                new BigDecimal("37.3"), null, true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "temperature")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertFalse(record.getIsAbnormal());
    }

    @Test
    @DisplayName("测试体温略高于阈值 - 边界值测试（触发预警）")
    void testSaveRecord_WithTemperatureSlightlyAboveThreshold_ShouldTriggerWarning() {
        HealthRecord record = createBasicRecord();
        record.setTemperature(new BigDecimal("37.4"));

        HealthWarningThreshold threshold = createThreshold("temperature", 
                new BigDecimal("37.3"), null, true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "temperature")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertTrue(record.getIsAbnormal());
        assertNotNull(record.getAbnormalReason());
        assertTrue(record.getAbnormalReason().contains("体温异常"));
        verify(warningRecordService, times(1)).createWarning(
                eq(1), any(), eq("temperature"), any(), any(), eq("HIGH"), anyString());
    }

    @Test
    @DisplayName("测试体温预警禁用时 - 不触发预警")
    void testSaveRecord_WithHighTemperatureButThresholdDisabled_ShouldNotTriggerWarning() {
        HealthRecord record = createBasicRecord();
        record.setTemperature(new BigDecimal("38.5"));

        HealthWarningThreshold threshold = createThreshold("temperature", 
                new BigDecimal("37.3"), null, false);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "temperature")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertFalse(record.getIsAbnormal());
        verify(warningRecordService, never()).createWarning(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("测试收缩压高于阈值 - 触发高压预警")
    void testSaveRecord_WithHighSystolicPressure_ShouldTriggerHighWarning() {
        HealthRecord record = createBasicRecord();
        record.setSystolicPressure(150);

        HealthWarningThreshold threshold = createThreshold("systolicPressure", 
                new BigDecimal("140"), new BigDecimal("90"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "systolicPressure")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertTrue(record.getIsAbnormal());
        assertTrue(record.getAbnormalReason().contains("收缩压异常"));
        verify(warningRecordService, times(1)).createWarning(
                eq(1), any(), eq("systolicPressure"), any(), any(), eq("HIGH"), anyString());
    }

    @Test
    @DisplayName("测试收缩压低于阈值 - 触发低压预警")
    void testSaveRecord_WithLowSystolicPressure_ShouldTriggerLowWarning() {
        HealthRecord record = createBasicRecord();
        record.setSystolicPressure(85);

        HealthWarningThreshold threshold = createThreshold("systolicPressure", 
                new BigDecimal("140"), new BigDecimal("90"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "systolicPressure")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertTrue(record.getIsAbnormal());
        verify(warningRecordService, times(1)).createWarning(
                eq(1), any(), eq("systolicPressure"), any(), any(), eq("LOW"), anyString());
    }

    @Test
    @DisplayName("测试收缩压边界值 - 等于高压阈值不触发")
    void testSaveRecord_WithSystolicPressureAtHighThreshold_ShouldNotTriggerWarning() {
        HealthRecord record = createBasicRecord();
        record.setSystolicPressure(140);

        HealthWarningThreshold threshold = createThreshold("systolicPressure", 
                new BigDecimal("140"), new BigDecimal("90"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "systolicPressure")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertFalse(record.getIsAbnormal());
    }

    @Test
    @DisplayName("测试收缩压边界值 - 等于低压阈值不触发")
    void testSaveRecord_WithSystolicPressureAtLowThreshold_ShouldNotTriggerWarning() {
        HealthRecord record = createBasicRecord();
        record.setSystolicPressure(90);

        HealthWarningThreshold threshold = createThreshold("systolicPressure", 
                new BigDecimal("140"), new BigDecimal("90"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "systolicPressure")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertFalse(record.getIsAbnormal());
    }

    @Test
    @DisplayName("测试舒张压高于阈值 - 触发高压预警")
    void testSaveRecord_WithHighDiastolicPressure_ShouldTriggerHighWarning() {
        HealthRecord record = createBasicRecord();
        record.setDiastolicPressure(95);

        HealthWarningThreshold threshold = createThreshold("diastolicPressure", 
                new BigDecimal("90"), new BigDecimal("60"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "diastolicPressure")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertTrue(record.getIsAbnormal());
        verify(warningRecordService, times(1)).createWarning(
                eq(1), any(), eq("diastolicPressure"), any(), any(), eq("HIGH"), anyString());
    }

    @Test
    @DisplayName("测试舒张压低于阈值 - 触发低压预警")
    void testSaveRecord_WithLowDiastolicPressure_ShouldTriggerLowWarning() {
        HealthRecord record = createBasicRecord();
        record.setDiastolicPressure(55);

        HealthWarningThreshold threshold = createThreshold("diastolicPressure", 
                new BigDecimal("90"), new BigDecimal("60"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "diastolicPressure")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertTrue(record.getIsAbnormal());
        verify(warningRecordService, times(1)).createWarning(
                eq(1), any(), eq("diastolicPressure"), any(), any(), eq("LOW"), anyString());
    }

    @Test
    @DisplayName("测试心率高于阈值 - 触发高压预警")
    void testSaveRecord_WithHighHeartRate_ShouldTriggerHighWarning() {
        HealthRecord record = createBasicRecord();
        record.setHeartRate(110);

        HealthWarningThreshold threshold = createThreshold("heartRate", 
                new BigDecimal("100"), new BigDecimal("60"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "heartRate")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertTrue(record.getIsAbnormal());
        verify(warningRecordService, times(1)).createWarning(
                eq(1), any(), eq("heartRate"), any(), any(), eq("HIGH"), anyString());
    }

    @Test
    @DisplayName("测试心率低于阈值 - 触发低压预警")
    void testSaveRecord_WithLowHeartRate_ShouldTriggerLowWarning() {
        HealthRecord record = createBasicRecord();
        record.setHeartRate(55);

        HealthWarningThreshold threshold = createThreshold("heartRate", 
                new BigDecimal("100"), new BigDecimal("60"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "heartRate")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertTrue(record.getIsAbnormal());
        verify(warningRecordService, times(1)).createWarning(
                eq(1), any(), eq("heartRate"), any(), any(), eq("LOW"), anyString());
    }

    @Test
    @DisplayName("测试血氧低于阈值 - 触发低压预警（血氧只检查低阈值）")
    void testSaveRecord_WithLowBloodOxygen_ShouldTriggerLowWarning() {
        HealthRecord record = createBasicRecord();
        record.setBloodOxygen(93);

        HealthWarningThreshold threshold = createThreshold("bloodOxygen", 
                null, new BigDecimal("95"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "bloodOxygen")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertTrue(record.getIsAbnormal());
        verify(warningRecordService, times(1)).createWarning(
                eq(1), any(), eq("bloodOxygen"), any(), any(), eq("LOW"), anyString());
    }

    @Test
    @DisplayName("测试血氧等于阈值 - 边界值测试（不触发预警）")
    void testSaveRecord_WithBloodOxygenAtThreshold_ShouldNotTriggerWarning() {
        HealthRecord record = createBasicRecord();
        record.setBloodOxygen(95);

        HealthWarningThreshold threshold = createThreshold("bloodOxygen", 
                null, new BigDecimal("95"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "bloodOxygen")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertFalse(record.getIsAbnormal());
    }

    @Test
    @DisplayName("测试血糖高于阈值 - 触发高压预警")
    void testSaveRecord_WithHighBloodSugar_ShouldTriggerHighWarning() {
        HealthRecord record = createBasicRecord();
        record.setBloodSugar(new BigDecimal("7.0"));

        HealthWarningThreshold threshold = createThreshold("bloodSugar", 
                new BigDecimal("6.1"), new BigDecimal("3.9"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "bloodSugar")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertTrue(record.getIsAbnormal());
        verify(warningRecordService, times(1)).createWarning(
                eq(1), any(), eq("bloodSugar"), any(), any(), eq("HIGH"), anyString());
    }

    @Test
    @DisplayName("测试血糖低于阈值 - 触发低压预警")
    void testSaveRecord_WithLowBloodSugar_ShouldTriggerLowWarning() {
        HealthRecord record = createBasicRecord();
        record.setBloodSugar(new BigDecimal("3.5"));

        HealthWarningThreshold threshold = createThreshold("bloodSugar", 
                new BigDecimal("6.1"), new BigDecimal("3.9"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "bloodSugar")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertTrue(record.getIsAbnormal());
        verify(warningRecordService, times(1)).createWarning(
                eq(1), any(), eq("bloodSugar"), any(), any(), eq("LOW"), anyString());
    }

    @Test
    @DisplayName("测试多项指标同时异常 - 组合场景")
    void testSaveRecord_WithMultipleAbnormalIndicators_ShouldTriggerMultipleWarnings() {
        HealthRecord record = createBasicRecord();
        record.setTemperature(new BigDecimal("38.5"));
        record.setSystolicPressure(160);
        record.setHeartRate(120);

        HealthWarningThreshold tempThreshold = createThreshold("temperature", 
                new BigDecimal("37.3"), null, true);
        HealthWarningThreshold sysThreshold = createThreshold("systolicPressure", 
                new BigDecimal("140"), new BigDecimal("90"), true);
        HealthWarningThreshold heartThreshold = createThreshold("heartRate", 
                new BigDecimal("100"), new BigDecimal("60"), true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "temperature")).thenReturn(tempThreshold);
        when(thresholdService.getThreshold(1, "systolicPressure")).thenReturn(sysThreshold);
        when(thresholdService.getThreshold(1, "diastolicPressure")).thenReturn(
                createThreshold("diastolicPressure", new BigDecimal("90"), new BigDecimal("60"), true));
        when(thresholdService.getThreshold(1, "heartRate")).thenReturn(heartThreshold);
        when(thresholdService.getThreshold(1, "bloodOxygen")).thenReturn(
                createThreshold("bloodOxygen", null, new BigDecimal("95"), true));
        when(thresholdService.getThreshold(1, "bloodSugar")).thenReturn(
                createThreshold("bloodSugar", new BigDecimal("6.1"), new BigDecimal("3.9"), true));
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertTrue(record.getIsAbnormal());
        assertNotNull(record.getAbnormalReason());
        assertTrue(record.getAbnormalReason().contains("体温异常"));
        assertTrue(record.getAbnormalReason().contains("收缩压异常"));
        assertTrue(record.getAbnormalReason().contains("心率异常"));
        
        verify(warningRecordService, times(3)).createWarning(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("测试所有指标正常 - 不触发任何预警")
    void testSaveRecord_WithAllNormalIndicators_ShouldNotTriggerAnyWarning() {
        HealthRecord record = createBasicRecord();
        record.setTemperature(new BigDecimal("36.5"));
        record.setSystolicPressure(120);
        record.setDiastolicPressure(80);
        record.setHeartRate(75);
        record.setBloodOxygen(98);
        record.setBloodSugar(new BigDecimal("5.0"));

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "temperature")).thenReturn(
                createThreshold("temperature", new BigDecimal("37.3"), null, true));
        when(thresholdService.getThreshold(1, "systolicPressure")).thenReturn(
                createThreshold("systolicPressure", new BigDecimal("140"), new BigDecimal("90"), true));
        when(thresholdService.getThreshold(1, "diastolicPressure")).thenReturn(
                createThreshold("diastolicPressure", new BigDecimal("90"), new BigDecimal("60"), true));
        when(thresholdService.getThreshold(1, "heartRate")).thenReturn(
                createThreshold("heartRate", new BigDecimal("100"), new BigDecimal("60"), true));
        when(thresholdService.getThreshold(1, "bloodOxygen")).thenReturn(
                createThreshold("bloodOxygen", null, new BigDecimal("95"), true));
        when(thresholdService.getThreshold(1, "bloodSugar")).thenReturn(
                createThreshold("bloodSugar", new BigDecimal("6.1"), new BigDecimal("3.9"), true));
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertFalse(record.getIsAbnormal());
        assertNull(record.getAbnormalReason());
        verify(warningRecordService, never()).createWarning(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("测试血压字符串解析 - 从字符串设置收缩压和舒张压")
    void testSaveRecord_WithBloodPressureString_ShouldParseToSystolicAndDiastolic() {
        HealthRecord record = createBasicRecord();
        record.setBloodPressure("120/80");

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "temperature")).thenReturn(
                createThreshold("temperature", new BigDecimal("37.3"), null, true));
        when(thresholdService.getThreshold(1, "systolicPressure")).thenReturn(
                createThreshold("systolicPressure", new BigDecimal("140"), new BigDecimal("90"), true));
        when(thresholdService.getThreshold(1, "diastolicPressure")).thenReturn(
                createThreshold("diastolicPressure", new BigDecimal("90"), new BigDecimal("60"), true));
        when(thresholdService.getThreshold(1, "heartRate")).thenReturn(
                createThreshold("heartRate", new BigDecimal("100"), new BigDecimal("60"), true));
        when(thresholdService.getThreshold(1, "bloodOxygen")).thenReturn(
                createThreshold("bloodOxygen", null, new BigDecimal("95"), true));
        when(thresholdService.getThreshold(1, "bloodSugar")).thenReturn(
                createThreshold("bloodSugar", new BigDecimal("6.1"), new BigDecimal("3.9"), true));
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertEquals(120, record.getSystolicPressure());
        assertEquals(80, record.getDiastolicPressure());
    }

    @Test
    @DisplayName("测试收缩压和舒张压设置 - 自动拼接血压字符串")
    void testSaveRecord_WithSystolicAndDiastolic_ShouldBuildBloodPressureString() {
        HealthRecord record = createBasicRecord();
        record.setSystolicPressure(130);
        record.setDiastolicPressure(85);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "temperature")).thenReturn(
                createThreshold("temperature", new BigDecimal("37.3"), null, true));
        when(thresholdService.getThreshold(1, "systolicPressure")).thenReturn(
                createThreshold("systolicPressure", new BigDecimal("140"), new BigDecimal("90"), true));
        when(thresholdService.getThreshold(1, "diastolicPressure")).thenReturn(
                createThreshold("diastolicPressure", new BigDecimal("90"), new BigDecimal("60"), true));
        when(thresholdService.getThreshold(1, "heartRate")).thenReturn(
                createThreshold("heartRate", new BigDecimal("100"), new BigDecimal("60"), true));
        when(thresholdService.getThreshold(1, "bloodOxygen")).thenReturn(
                createThreshold("bloodOxygen", null, new BigDecimal("95"), true));
        when(thresholdService.getThreshold(1, "bloodSugar")).thenReturn(
                createThreshold("bloodSugar", new BigDecimal("6.1"), new BigDecimal("3.9"), true));
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertEquals("130/85", record.getBloodPressure());
    }

    @Test
    @DisplayName("测试检查时间为空时自动设置当前时间")
    void testSaveRecord_WithNullCheckTime_ShouldSetCurrentTime() {
        HealthRecord record = createBasicRecord();
        record.setCheckTime(null);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "temperature")).thenReturn(
                createThreshold("temperature", new BigDecimal("37.3"), null, true));
        when(thresholdService.getThreshold(1, "systolicPressure")).thenReturn(
                createThreshold("systolicPressure", new BigDecimal("140"), new BigDecimal("90"), true));
        when(thresholdService.getThreshold(1, "diastolicPressure")).thenReturn(
                createThreshold("diastolicPressure", new BigDecimal("90"), new BigDecimal("60"), true));
        when(thresholdService.getThreshold(1, "heartRate")).thenReturn(
                createThreshold("heartRate", new BigDecimal("100"), new BigDecimal("60"), true));
        when(thresholdService.getThreshold(1, "bloodOxygen")).thenReturn(
                createThreshold("bloodOxygen", null, new BigDecimal("95"), true));
        when(thresholdService.getThreshold(1, "bloodSugar")).thenReturn(
                createThreshold("bloodSugar", new BigDecimal("6.1"), new BigDecimal("3.9"), true));
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertNotNull(record.getCheckTime());
    }

    @Test
    @DisplayName("测试阈值为null时不触发预警")
    void testSaveRecord_WithNullThreshold_ShouldNotTriggerWarning() {
        HealthRecord record = createBasicRecord();
        record.setTemperature(new BigDecimal("38.5"));

        HealthWarningThreshold threshold = createThreshold("temperature", 
                null, null, true);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "temperature")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertFalse(record.getIsAbnormal());
        verify(warningRecordService, never()).createWarning(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("测试预警禁用状态为null时不触发预警")
    void testSaveRecord_WithNullEnabledFlag_ShouldNotTriggerWarning() {
        HealthRecord record = createBasicRecord();
        record.setTemperature(new BigDecimal("38.5"));

        HealthWarningThreshold threshold = createThreshold("temperature", 
                new BigDecimal("37.3"), null, true);
        threshold.setEnabled(null);

        when(elderlyService.getById(1)).thenReturn(testElderly);
        when(thresholdService.getThreshold(1, "temperature")).thenReturn(threshold);
        when(healthRecordMapper.insert(any(HealthRecord.class))).thenReturn(1);

        boolean result = healthRecordService.saveRecord(record);

        assertTrue(result);
        assertFalse(record.getIsAbnormal());
    }
}
