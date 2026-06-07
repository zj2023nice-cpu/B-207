package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.elderly.dto.QualityReviewResult;
import com.smart.elderly.entity.HealthRecord;
import com.smart.elderly.entity.QualityReviewRule;
import com.smart.elderly.enums.Severity;
import com.smart.elderly.mapper.HealthRecordMapper;
import com.smart.elderly.mapper.QualityReviewRuleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
@Component
public class QualityReviewEngine {

    @Autowired
    private QualityReviewRuleMapper ruleMapper;

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, BiFunction<HealthRecord, Map<String, Object>, String>> ruleHandlers = new HashMap<>();

    public QualityReviewEngine() {
        initRuleHandlers();
    }

    private void initRuleHandlers() {
        ruleHandlers.put("MISSING_VITAL_SIGNS", this::checkMissingVitalSigns);
        ruleHandlers.put("EXTREME_BLOOD_PRESSURE", this::checkExtremeBloodPressure);
        ruleHandlers.put("EXTREME_TEMPERATURE", this::checkExtremeTemperature);
        ruleHandlers.put("EXTREME_HEART_RATE", this::checkExtremeHeartRate);
        ruleHandlers.put("EXTREME_BLOOD_OXYGEN", this::checkExtremeBloodOxygen);
        ruleHandlers.put("EXTREME_BLOOD_SUGAR", this::checkExtremeBloodSugar);
        ruleHandlers.put("QUICK_DUPLICATE", this::checkQuickDuplicate);
        ruleHandlers.put("BLOOD_PRESSURE_CONTRADICTION", this::checkBloodPressureContradiction);
        ruleHandlers.put("ABNORMAL_NO_REASON", this::checkAbnormalNoReason);
    }

    public QualityReviewResult analyze(HealthRecord record) {
        QualityReviewResult result = new QualityReviewResult();
        
        List<QualityReviewRule> enabledRules = ruleMapper.selectList(
            new LambdaQueryWrapper<QualityReviewRule>()
                .eq(QualityReviewRule::getEnabled, true)
                .orderByAsc(QualityReviewRule::getSortOrder)
        );

        for (QualityReviewRule rule : enabledRules) {
            try {
                BiFunction<HealthRecord, Map<String, Object>, String> handler = ruleHandlers.get(rule.getRuleCode());
                if (handler == null) {
                    continue;
                }
                
                Map<String, Object> config = parseConfig(rule.getConfigJson());
                
                String issue = handler.apply(record, config);
                if (issue != null) {
                    Severity severity = Severity.valueOf(rule.getSeverity());
                    result.addIssue(rule.getRuleCode(), issue, severity.getScoreDeduction());
                }
            } catch (Exception e) {
                log.error("执行规则{}异常", rule.getRuleCode(), e);
            }
        }

        result.setIssuesSummary(result.buildIssuesSummary());
        return result;
    }

    private Map<String, Object> parseConfig(String configJson) {
        if (configJson == null || configJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析规则配置失败", e);
            return new HashMap<>();
        }
    }

    public void registerRuleHandler(String ruleCode, BiFunction<HealthRecord, Map<String, Object>, String> handler) {
        ruleHandlers.put(ruleCode, handler);
    }

    private String checkMissingVitalSigns(HealthRecord record, Map<String, Object> config) {
        Object requiredFieldsObj = config.get("requiredFields");
        if (requiredFieldsObj == null || !(requiredFieldsObj instanceof List)) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        List<String> requiredFields = (List<String>) requiredFieldsObj;
        List<String> missing = new ArrayList<>();
        
        for (String field : requiredFields) {
            Object value = getFieldValue(record, field);
            if (value == null) {
                missing.add(getFieldDisplayName(field));
            }
        }
        
        if (!missing.isEmpty()) {
            return "缺少关键指标：" + String.join("、", missing);
        }
        return null;
    }

    private String checkExtremeBloodPressure(HealthRecord record, Map<String, Object> config) {
        if (record.getSystolicPressure() == null && record.getDiastolicPressure() == null) {
            return null;
        }
        
        int sysMin = getIntConfig(config, "systolicMin", 50);
        int sysMax = getIntConfig(config, "systolicMax", 220);
        int diaMin = getIntConfig(config, "diastolicMin", 30);
        int diaMax = getIntConfig(config, "diastolicMax", 130);

        List<String> issues = new ArrayList<>();
        if (record.getSystolicPressure() != null) {
            if (record.getSystolicPressure() < sysMin || record.getSystolicPressure() > sysMax) {
                issues.add("收缩压" + record.getSystolicPressure() + "超出合理范围(" + sysMin + "-" + sysMax + ")");
            }
        }
        if (record.getDiastolicPressure() != null) {
            if (record.getDiastolicPressure() < diaMin || record.getDiastolicPressure() > diaMax) {
                issues.add("舒张压" + record.getDiastolicPressure() + "超出合理范围(" + diaMin + "-" + diaMax + ")");
            }
        }
        
        return issues.isEmpty() ? null : String.join("；", issues);
    }

    private String checkExtremeTemperature(HealthRecord record, Map<String, Object> config) {
        if (record.getTemperature() == null) return null;
        
        BigDecimal tempMin = getBigDecimalConfig(config, "tempMin", new BigDecimal("34"));
        BigDecimal tempMax = getBigDecimalConfig(config, "tempMax", new BigDecimal("43"));

        if (record.getTemperature().compareTo(tempMin) < 0 || record.getTemperature().compareTo(tempMax) > 0) {
            return "体温" + record.getTemperature() + "℃超出合理范围(" + tempMin + "-" + tempMax + ")";
        }
        return null;
    }

    private String checkExtremeHeartRate(HealthRecord record, Map<String, Object> config) {
        if (record.getHeartRate() == null) return null;
        
        int hrMin = getIntConfig(config, "hrMin", 30);
        int hrMax = getIntConfig(config, "hrMax", 220);

        if (record.getHeartRate() < hrMin || record.getHeartRate() > hrMax) {
            return "心率" + record.getHeartRate() + "次/分超出合理范围(" + hrMin + "-" + hrMax + ")";
        }
        return null;
    }

    private String checkExtremeBloodOxygen(HealthRecord record, Map<String, Object> config) {
        if (record.getBloodOxygen() == null) return null;
        
        int spo2Min = getIntConfig(config, "spo2Min", 70);
        int spo2Max = getIntConfig(config, "spo2Max", 100);

        if (record.getBloodOxygen() < spo2Min || record.getBloodOxygen() > spo2Max) {
            return "血氧" + record.getBloodOxygen() + "%超出合理范围(" + spo2Min + "-" + spo2Max + ")";
        }
        return null;
    }

    private String checkExtremeBloodSugar(HealthRecord record, Map<String, Object> config) {
        if (record.getBloodSugar() == null) return null;
        
        BigDecimal glucoseMin = getBigDecimalConfig(config, "glucoseMin", new BigDecimal("1"));
        BigDecimal glucoseMax = getBigDecimalConfig(config, "glucoseMax", new BigDecimal("30"));

        if (record.getBloodSugar().compareTo(glucoseMin) < 0 || record.getBloodSugar().compareTo(glucoseMax) > 0) {
            return "血糖" + record.getBloodSugar() + "mmol/L超出合理范围(" + glucoseMin + "-" + glucoseMax + ")";
        }
        return null;
    }

    private String checkQuickDuplicate(HealthRecord record, Map<String, Object> config) {
        if (record.getElderlyId() == null || record.getCheckTime() == null) return null;

        int timeWindowMinutes = getIntConfig(config, "timeWindowMinutes", 5);
        LocalDateTime startTime = record.getCheckTime().minusMinutes(timeWindowMinutes);
        LocalDateTime endTime = record.getCheckTime().plusMinutes(timeWindowMinutes);

        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<HealthRecord>()
            .eq(HealthRecord::getElderlyId, record.getElderlyId())
            .between(HealthRecord::getCheckTime, startTime, endTime);

        if (record.getId() != null) {
            wrapper.ne(HealthRecord::getId, record.getId());
        }

        Long count = healthRecordMapper.selectCount(wrapper);

        if (count != null && count > 0) {
            return "该老人在" + timeWindowMinutes + "分钟内已有健康记录，存在重复录入可能";
        }
        return null;
    }

    private String checkBloodPressureContradiction(HealthRecord record, Map<String, Object> config) {
        if (record.getSystolicPressure() == null || record.getDiastolicPressure() == null) {
            return null;
        }
        
        if (record.getSystolicPressure() <= record.getDiastolicPressure()) {
            return "收缩压(" + record.getSystolicPressure() + ")应大于舒张压(" + record.getDiastolicPressure() + ")，存在逻辑矛盾";
        }
        return null;
    }

    private String checkAbnormalNoReason(HealthRecord record, Map<String, Object> config) {
        if (Boolean.TRUE.equals(record.getIsAbnormal()) && 
            (record.getAbnormalReason() == null || record.getAbnormalReason().trim().isEmpty())) {
            return "记录标记为异常但未填写异常原因说明";
        }
        return null;
    }

    private int getIntConfig(Map<String, Object> config, String key, int defaultValue) {
        Object value = config.get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private BigDecimal getBigDecimalConfig(Map<String, Object> config, String key, BigDecimal defaultValue) {
        Object value = config.get(key);
        if (value == null) return defaultValue;
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Object getFieldValue(HealthRecord record, String field) {
        switch (field) {
            case "systolicPressure":
                return record.getSystolicPressure();
            case "diastolicPressure":
                return record.getDiastolicPressure();
            case "heartRate":
                return record.getHeartRate();
            case "temperature":
                return record.getTemperature();
            case "bloodOxygen":
                return record.getBloodOxygen();
            case "bloodSugar":
                return record.getBloodSugar();
            default:
                return null;
        }
    }

    private String getFieldDisplayName(String field) {
        switch (field) {
            case "systolicPressure":
                return "收缩压";
            case "diastolicPressure":
                return "舒张压";
            case "heartRate":
                return "心率";
            case "temperature":
                return "体温";
            case "bloodOxygen":
                return "血氧";
            case "bloodSugar":
                return "血糖";
            default:
                return field;
        }
    }
}
