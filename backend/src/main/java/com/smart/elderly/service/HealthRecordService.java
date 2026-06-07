package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.entity.HealthRecord;
import com.smart.elderly.entity.HealthWarningThreshold;
import com.smart.elderly.mapper.HealthRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HealthRecordService extends ServiceImpl<HealthRecordMapper, HealthRecord> {

    @Autowired
    private ElderlyService elderlyService;

    @Autowired
    private HealthWarningThresholdService thresholdService;

    @Autowired
    private HealthWarningRecordService warningRecordService;

    @Autowired
    private HealthRecordQualityReviewService qualityReviewService;

    @Transactional
    public boolean saveRecord(HealthRecord record) {
        if (record.getElderlyId() == null) {
            throw new IllegalArgumentException("老人ID不能为空");
        }
        
        Elderly elderly = elderlyService.getById(record.getElderlyId());
        if (elderly == null) {
            throw new IllegalArgumentException("老人不存在，ID: " + record.getElderlyId());
        }

        if (record.getSystolicPressure() != null && record.getDiastolicPressure() != null) {
            record.setBloodPressure(record.getSystolicPressure() + "/" + record.getDiastolicPressure());
        } else if (record.getBloodPressure() != null && record.getBloodPressure().contains("/")) {
            String[] parts = record.getBloodPressure().split("/");
            if (parts.length == 2) {
                try {
                    record.setSystolicPressure(Integer.parseInt(parts[0].trim()));
                    record.setDiastolicPressure(Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException e) {
                }
            }
        }

        if (record.getCheckTime() == null) {
            record.setCheckTime(LocalDateTime.now());
        }

        List<WarningInfo> pendingWarnings = new ArrayList<>();
        List<String> abnormalReasons = new ArrayList<>();
        boolean isAbnormal = false;
        Integer elderlyId = record.getElderlyId();

        if (record.getTemperature() != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "temperature");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                if (threshold.getHighThreshold() != null && 
                    record.getTemperature().compareTo(threshold.getHighThreshold()) > 0) {
                    isAbnormal = true;
                    String reason = "体温异常(" + record.getTemperature() + "℃)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new WarningInfo(elderlyId, "temperature", 
                        record.getTemperature(), threshold.getHighThreshold(), 
                        "HIGH", "老人" + elderly.getName() + reason));
                }
            }
        }

        if (record.getSystolicPressure() != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "systolicPressure");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                BigDecimal actualValue = BigDecimal.valueOf(record.getSystolicPressure());
                if (threshold.getHighThreshold() != null && 
                    actualValue.compareTo(threshold.getHighThreshold()) > 0) {
                    isAbnormal = true;
                    String reason = "收缩压异常(" + record.getSystolicPressure() + "mmHg)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new WarningInfo(elderlyId, "systolicPressure", 
                        actualValue, threshold.getHighThreshold(), 
                        "HIGH", "老人" + elderly.getName() + reason));
                } else if (threshold.getLowThreshold() != null && 
                    actualValue.compareTo(threshold.getLowThreshold()) < 0) {
                    isAbnormal = true;
                    String reason = "收缩压异常(" + record.getSystolicPressure() + "mmHg)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new WarningInfo(elderlyId, "systolicPressure", 
                        actualValue, threshold.getLowThreshold(), 
                        "LOW", "老人" + elderly.getName() + reason));
                }
            }
        }

        if (record.getDiastolicPressure() != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "diastolicPressure");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                BigDecimal actualValue = BigDecimal.valueOf(record.getDiastolicPressure());
                if (threshold.getHighThreshold() != null && 
                    actualValue.compareTo(threshold.getHighThreshold()) > 0) {
                    isAbnormal = true;
                    String reason = "舒张压异常(" + record.getDiastolicPressure() + "mmHg)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new WarningInfo(elderlyId, "diastolicPressure", 
                        actualValue, threshold.getHighThreshold(), 
                        "HIGH", "老人" + elderly.getName() + reason));
                } else if (threshold.getLowThreshold() != null && 
                    actualValue.compareTo(threshold.getLowThreshold()) < 0) {
                    isAbnormal = true;
                    String reason = "舒张压异常(" + record.getDiastolicPressure() + "mmHg)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new WarningInfo(elderlyId, "diastolicPressure", 
                        actualValue, threshold.getLowThreshold(), 
                        "LOW", "老人" + elderly.getName() + reason));
                }
            }
        }

        if (record.getHeartRate() != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "heartRate");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                BigDecimal actualValue = BigDecimal.valueOf(record.getHeartRate());
                if (threshold.getHighThreshold() != null && 
                    actualValue.compareTo(threshold.getHighThreshold()) > 0) {
                    isAbnormal = true;
                    String reason = "心率异常(" + record.getHeartRate() + "次/分)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new WarningInfo(elderlyId, "heartRate", 
                        actualValue, threshold.getHighThreshold(), 
                        "HIGH", "老人" + elderly.getName() + reason));
                } else if (threshold.getLowThreshold() != null && 
                    actualValue.compareTo(threshold.getLowThreshold()) < 0) {
                    isAbnormal = true;
                    String reason = "心率异常(" + record.getHeartRate() + "次/分)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new WarningInfo(elderlyId, "heartRate", 
                        actualValue, threshold.getLowThreshold(), 
                        "LOW", "老人" + elderly.getName() + reason));
                }
            }
        }

        if (record.getBloodOxygen() != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "bloodOxygen");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                BigDecimal actualValue = BigDecimal.valueOf(record.getBloodOxygen());
                if (threshold.getLowThreshold() != null && 
                    actualValue.compareTo(threshold.getLowThreshold()) < 0) {
                    isAbnormal = true;
                    String reason = "血氧异常(" + record.getBloodOxygen() + "%)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new WarningInfo(elderlyId, "bloodOxygen", 
                        actualValue, threshold.getLowThreshold(), 
                        "LOW", "老人" + elderly.getName() + reason));
                }
            }
        }

        if (record.getBloodSugar() != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "bloodSugar");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                if (threshold.getHighThreshold() != null && 
                    record.getBloodSugar().compareTo(threshold.getHighThreshold()) > 0) {
                    isAbnormal = true;
                    String reason = "血糖异常(" + record.getBloodSugar() + "mmol/L)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new WarningInfo(elderlyId, "bloodSugar", 
                        record.getBloodSugar(), threshold.getHighThreshold(), 
                        "HIGH", "老人" + elderly.getName() + reason));
                } else if (threshold.getLowThreshold() != null && 
                    record.getBloodSugar().compareTo(threshold.getLowThreshold()) < 0) {
                    isAbnormal = true;
                    String reason = "血糖异常(" + record.getBloodSugar() + "mmol/L)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new WarningInfo(elderlyId, "bloodSugar", 
                        record.getBloodSugar(), threshold.getLowThreshold(), 
                        "LOW", "老人" + elderly.getName() + reason));
                }
            }
        }

        record.setIsAbnormal(isAbnormal);
        if (!abnormalReasons.isEmpty()) {
            record.setAbnormalReason(String.join("; ", abnormalReasons));
        }

        com.smart.elderly.dto.QualityReviewResult qualityReviewResult = qualityReviewService.analyzeRecordQuality(record);
        qualityReviewService.applyQualityReviewResult(record, qualityReviewResult);

        boolean saved = this.save(record);

        if (saved) {
            if (!pendingWarnings.isEmpty()) {
                for (WarningInfo info : pendingWarnings) {
                    warningRecordService.createWarning(
                        info.elderlyId,
                        record.getId(),
                        info.indicatorType,
                        info.actualValue,
                        info.thresholdValue,
                        info.warningLevel,
                        info.warningMessage
                    );
                }
            }

            qualityReviewService.createReviewIfNeeded(record, qualityReviewResult);
        }

        return saved;
    }

    public static class WarningInfo {
        public Integer elderlyId;
        public String indicatorType;
        public BigDecimal actualValue;
        public BigDecimal thresholdValue;
        public String warningLevel;
        public String warningMessage;

        public WarningInfo(Integer elderlyId, String indicatorType, BigDecimal actualValue,
                           BigDecimal thresholdValue, String warningLevel, String warningMessage) {
            this.elderlyId = elderlyId;
            this.indicatorType = indicatorType;
            this.actualValue = actualValue;
            this.thresholdValue = thresholdValue;
            this.warningLevel = warningLevel;
            this.warningMessage = warningMessage;
        }
    }

    public List<HealthRecord> getRecordsWithNames() {
        return baseMapper.selectWithElderlyName();
    }

    public List<HealthRecord> getRecordsWithFilters(Integer elderlyId, Boolean isAbnormal) {
        return baseMapper.selectWithFilters(elderlyId, isAbnormal);
    }

    public List<HealthRecord> getByElderlyId(Integer elderlyId) {
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getElderlyId, elderlyId)
               .orderByAsc(HealthRecord::getCheckTime);
        return this.list(wrapper);
    }

    public Map<String, Object> getTrendData(Integer elderlyId) {
        List<HealthRecord> records = getByElderlyId(elderlyId);
        Map<String, Object> result = new LinkedHashMap<>();
        
        List<String> times = new ArrayList<>();
        List<BigDecimal> temperatures = new ArrayList<>();
        List<Integer> systolicPressures = new ArrayList<>();
        List<Integer> diastolicPressures = new ArrayList<>();
        List<Integer> heartRates = new ArrayList<>();
        List<Integer> bloodOxygens = new ArrayList<>();
        List<BigDecimal> bloodSugars = new ArrayList<>();

        for (HealthRecord record : records) {
            times.add(formatTime(record.getCheckTime()));
            temperatures.add(record.getTemperature());
            systolicPressures.add(record.getSystolicPressure());
            diastolicPressures.add(record.getDiastolicPressure());
            heartRates.add(record.getHeartRate());
            bloodOxygens.add(record.getBloodOxygen());
            bloodSugars.add(record.getBloodSugar());
        }

        result.put("times", times);
        result.put("temperatures", temperatures);
        result.put("systolicPressures", systolicPressures);
        result.put("diastolicPressures", diastolicPressures);
        result.put("heartRates", heartRates);
        result.put("bloodOxygens", bloodOxygens);
        result.put("bloodSugars", bloodSugars);
        result.put("total", records.size());

        return result;
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) return "";
        return time.getMonthValue() + "/" + time.getDayOfMonth() + " " + 
               String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
}
