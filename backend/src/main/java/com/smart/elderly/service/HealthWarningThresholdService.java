package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.HealthWarningThreshold;
import com.smart.elderly.mapper.HealthWarningThresholdMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HealthWarningThresholdService extends ServiceImpl<HealthWarningThresholdMapper, HealthWarningThreshold> {

    private static final Map<String, BigDecimal> DEFAULT_HIGH_THRESHOLDS = new HashMap<>();
    private static final Map<String, BigDecimal> DEFAULT_LOW_THRESHOLDS = new HashMap<>();

    static {
        DEFAULT_HIGH_THRESHOLDS.put("temperature", new BigDecimal("37.3"));
        DEFAULT_HIGH_THRESHOLDS.put("systolicPressure", new BigDecimal("140"));
        DEFAULT_HIGH_THRESHOLDS.put("diastolicPressure", new BigDecimal("90"));
        DEFAULT_HIGH_THRESHOLDS.put("heartRate", new BigDecimal("100"));
        DEFAULT_HIGH_THRESHOLDS.put("bloodSugar", new BigDecimal("6.1"));
        
        DEFAULT_LOW_THRESHOLDS.put("systolicPressure", new BigDecimal("90"));
        DEFAULT_LOW_THRESHOLDS.put("diastolicPressure", new BigDecimal("60"));
        DEFAULT_LOW_THRESHOLDS.put("heartRate", new BigDecimal("60"));
        DEFAULT_LOW_THRESHOLDS.put("bloodOxygen", new BigDecimal("95"));
        DEFAULT_LOW_THRESHOLDS.put("bloodSugar", new BigDecimal("3.9"));
    }

    public List<HealthWarningThreshold> getByElderlyId(Integer elderlyId) {
        return baseMapper.findByElderlyId(elderlyId);
    }

    public List<HealthWarningThreshold> getSystemDefaults() {
        return baseMapper.findSystemDefaults();
    }

    public HealthWarningThreshold getThreshold(Integer elderlyId, String indicatorType) {
        HealthWarningThreshold threshold = baseMapper.findByElderlyIdAndIndicatorType(elderlyId, indicatorType);
        if (threshold == null) {
            threshold = baseMapper.findSystemDefaultByIndicatorType(indicatorType);
        }
        if (threshold == null) {
            threshold = createDefaultThreshold(indicatorType);
        }
        return threshold;
    }

    private HealthWarningThreshold createDefaultThreshold(String indicatorType) {
        HealthWarningThreshold threshold = new HealthWarningThreshold();
        threshold.setIndicatorType(indicatorType);
        threshold.setHighThreshold(DEFAULT_HIGH_THRESHOLDS.get(indicatorType));
        threshold.setLowThreshold(DEFAULT_LOW_THRESHOLDS.get(indicatorType));
        threshold.setEnabled(true);
        return threshold;
    }

    @Transactional
    public boolean saveOrUpdateThreshold(HealthWarningThreshold threshold) {
        if (threshold.getId() == null) {
            threshold.setCreatedAt(LocalDateTime.now());
            threshold.setUpdatedAt(LocalDateTime.now());
            if (threshold.getEnabled() == null) {
                threshold.setEnabled(true);
            }
            return this.save(threshold);
        } else {
            threshold.setUpdatedAt(LocalDateTime.now());
            return this.updateById(threshold);
        }
    }

    @Transactional
    public boolean deleteThreshold(Integer id) {
        return this.removeById(id);
    }

    @Transactional
    public void initSystemDefaults() {
        List<HealthWarningThreshold> existing = getSystemDefaults();
        if (!existing.isEmpty()) {
            return;
        }
        
        String[] indicators = {"temperature", "systolicPressure", "diastolicPressure", 
                               "heartRate", "bloodOxygen", "bloodSugar"};
        
        for (String indicator : indicators) {
            HealthWarningThreshold threshold = new HealthWarningThreshold();
            threshold.setIndicatorType(indicator);
            threshold.setHighThreshold(DEFAULT_HIGH_THRESHOLDS.get(indicator));
            threshold.setLowThreshold(DEFAULT_LOW_THRESHOLDS.get(indicator));
            threshold.setEnabled(true);
            threshold.setCreatedAt(LocalDateTime.now());
            threshold.setUpdatedAt(LocalDateTime.now());
            this.save(threshold);
        }
    }

    public Map<String, Object> getEffectiveThresholds(Integer elderlyId) {
        Map<String, Object> result = new HashMap<>();
        
        List<HealthWarningThreshold> personal = getByElderlyId(elderlyId);
        List<HealthWarningThreshold> systemDefaults = getSystemDefaults();
        
        Map<String, HealthWarningThreshold> thresholdMap = new HashMap<>();
        
        for (HealthWarningThreshold t : systemDefaults) {
            if (t.getEnabled()) {
                thresholdMap.put(t.getIndicatorType(), t);
            }
        }
        
        for (HealthWarningThreshold t : personal) {
            if (t.getEnabled()) {
                thresholdMap.put(t.getIndicatorType(), t);
            }
        }
        
        for (Map.Entry<String, HealthWarningThreshold> entry : thresholdMap.entrySet()) {
            Map<String, BigDecimal> values = new HashMap<>();
            HealthWarningThreshold t = entry.getValue();
            values.put("high", t.getHighThreshold());
            values.put("low", t.getLowThreshold());
            result.put(entry.getKey(), values);
        }
        
        return result;
    }
}
