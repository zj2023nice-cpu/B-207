package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.dto.HealthRecordCorrectionDTO;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.entity.HealthRecord;
import com.smart.elderly.entity.HealthRecordCorrection;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.entity.HealthWarningThreshold;
import com.smart.elderly.mapper.HealthRecordCorrectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HealthRecordCorrectionService extends ServiceImpl<HealthRecordCorrectionMapper, HealthRecordCorrection> {

    @Autowired
    private HealthRecordService healthRecordService;

    @Autowired
    private ElderlyService elderlyService;

    @Autowired
    private HealthWarningThresholdService thresholdService;

    @Autowired
    private HealthWarningRecordService warningRecordService;

    public List<HealthRecordCorrection> getByHealthRecordId(Integer healthRecordId) {
        return baseMapper.findByHealthRecordIdWithName(healthRecordId);
    }

    public List<HealthRecordCorrection> getByElderlyId(Integer elderlyId) {
        return baseMapper.findByElderlyIdWithName(elderlyId);
    }

    @Transactional
    public HealthRecordCorrection createCorrection(HealthRecordCorrectionDTO dto, String operator) {
        HealthRecord originalRecord = healthRecordService.getById(dto.getHealthRecordId());
        if (originalRecord == null) {
            throw new IllegalArgumentException("健康记录不存在，ID: " + dto.getHealthRecordId());
        }

        Elderly elderly = elderlyService.getById(originalRecord.getElderlyId());
        if (elderly == null) {
            throw new IllegalArgumentException("老人不存在，ID: " + originalRecord.getElderlyId());
        }

        Integer version = baseMapper.countByHealthRecordId(dto.getHealthRecordId()) + 1;

        HealthRecordCorrection correction = new HealthRecordCorrection();
        correction.setHealthRecordId(dto.getHealthRecordId());
        correction.setElderlyId(originalRecord.getElderlyId());

        correction.setBeforeBloodPressure(originalRecord.getBloodPressure());
        correction.setBeforeSystolicPressure(originalRecord.getSystolicPressure());
        correction.setBeforeDiastolicPressure(originalRecord.getDiastolicPressure());
        correction.setBeforeTemperature(originalRecord.getTemperature());
        correction.setBeforeHeartRate(originalRecord.getHeartRate());
        correction.setBeforeBloodOxygen(originalRecord.getBloodOxygen());
        correction.setBeforeBloodSugar(originalRecord.getBloodSugar());
        correction.setBeforeAbnormalReason(originalRecord.getAbnormalReason());
        correction.setBeforeIsAbnormal(originalRecord.getIsAbnormal());

        Integer systolicPressure = dto.getSystolicPressure() != null ? dto.getSystolicPressure() : originalRecord.getSystolicPressure();
        Integer diastolicPressure = dto.getDiastolicPressure() != null ? dto.getDiastolicPressure() : originalRecord.getDiastolicPressure();
        String bloodPressure = dto.getBloodPressure();
        if (systolicPressure != null && diastolicPressure != null) {
            bloodPressure = systolicPressure + "/" + diastolicPressure;
        } else if (bloodPressure == null) {
            bloodPressure = originalRecord.getBloodPressure();
        }

        correction.setAfterBloodPressure(bloodPressure);
        correction.setAfterSystolicPressure(systolicPressure);
        correction.setAfterDiastolicPressure(diastolicPressure);
        correction.setAfterTemperature(dto.getTemperature() != null ? dto.getTemperature() : originalRecord.getTemperature());
        correction.setAfterHeartRate(dto.getHeartRate() != null ? dto.getHeartRate() : originalRecord.getHeartRate());
        correction.setAfterBloodOxygen(dto.getBloodOxygen() != null ? dto.getBloodOxygen() : originalRecord.getBloodOxygen());
        correction.setAfterBloodSugar(dto.getBloodSugar() != null ? dto.getBloodSugar() : originalRecord.getBloodSugar());

        correction.setCorrectionReason(dto.getCorrectionReason());
        correction.setCorrectionRemark(dto.getCorrectionRemark());
        correction.setCorrectedBy(operator);
        correction.setCorrectedAt(LocalDateTime.now());
        correction.setStatus("EFFECTIVE");
        correction.setVersion(version);

        List<HealthRecordService.WarningInfo> pendingWarnings = new ArrayList<>();
        List<String> abnormalReasons = new ArrayList<>();
        boolean isAbnormal = calculateAbnormalStatus(
            originalRecord.getElderlyId(),
            correction.getAfterTemperature(),
            correction.getAfterSystolicPressure(),
            correction.getAfterDiastolicPressure(),
            correction.getAfterHeartRate(),
            correction.getAfterBloodOxygen(),
            correction.getAfterBloodSugar(),
            elderly.getName(),
            pendingWarnings,
            abnormalReasons
        );

        correction.setAfterIsAbnormal(isAbnormal);
        if (!abnormalReasons.isEmpty()) {
            correction.setAfterAbnormalReason(String.join("; ", abnormalReasons));
        }

        this.save(correction);

        originalRecord.setBloodPressure(correction.getAfterBloodPressure());
        originalRecord.setSystolicPressure(correction.getAfterSystolicPressure());
        originalRecord.setDiastolicPressure(correction.getAfterDiastolicPressure());
        originalRecord.setTemperature(correction.getAfterTemperature());
        originalRecord.setHeartRate(correction.getAfterHeartRate());
        originalRecord.setBloodOxygen(correction.getAfterBloodOxygen());
        originalRecord.setBloodSugar(correction.getAfterBloodSugar());
        originalRecord.setIsAbnormal(isAbnormal);
        originalRecord.setAbnormalReason(correction.getAfterAbnormalReason());
        originalRecord.setCorrected(true);
        originalRecord.setCorrectedAt(correction.getCorrectedAt());
        originalRecord.setCorrectedBy(operator);
        originalRecord.setLatestCorrectionId(correction.getId());
        healthRecordService.updateById(originalRecord);

        invalidateOldWarnings(dto.getHealthRecordId());

        if (!pendingWarnings.isEmpty()) {
            for (HealthRecordService.WarningInfo info : pendingWarnings) {
                warningRecordService.createWarning(
                    info.elderlyId,
                    dto.getHealthRecordId(),
                    info.indicatorType,
                    info.actualValue,
                    info.thresholdValue,
                    info.warningLevel,
                    "[更正后]" + info.warningMessage
                );
            }
        }

        return correction;
    }

    private boolean calculateAbnormalStatus(Integer elderlyId, 
                                            BigDecimal temperature,
                                            Integer systolicPressure,
                                            Integer diastolicPressure,
                                            Integer heartRate,
                                            Integer bloodOxygen,
                                            BigDecimal bloodSugar,
                                            String elderlyName,
                                            List<HealthRecordService.WarningInfo> pendingWarnings,
                                            List<String> abnormalReasons) {
        boolean isAbnormal = false;

        if (temperature != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "temperature");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                if (threshold.getHighThreshold() != null && 
                    temperature.compareTo(threshold.getHighThreshold()) > 0) {
                    isAbnormal = true;
                    String reason = "体温异常(" + temperature + "℃)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new HealthRecordService.WarningInfo(elderlyId, "temperature", 
                        temperature, threshold.getHighThreshold(), 
                        "HIGH", "老人" + elderlyName + reason));
                }
            }
        }

        if (systolicPressure != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "systolicPressure");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                BigDecimal actualValue = BigDecimal.valueOf(systolicPressure);
                if (threshold.getHighThreshold() != null && 
                    actualValue.compareTo(threshold.getHighThreshold()) > 0) {
                    isAbnormal = true;
                    String reason = "收缩压异常(" + systolicPressure + "mmHg)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new HealthRecordService.WarningInfo(elderlyId, "systolicPressure", 
                        actualValue, threshold.getHighThreshold(), 
                        "HIGH", "老人" + elderlyName + reason));
                } else if (threshold.getLowThreshold() != null && 
                    actualValue.compareTo(threshold.getLowThreshold()) < 0) {
                    isAbnormal = true;
                    String reason = "收缩压异常(" + systolicPressure + "mmHg)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new HealthRecordService.WarningInfo(elderlyId, "systolicPressure", 
                        actualValue, threshold.getLowThreshold(), 
                        "LOW", "老人" + elderlyName + reason));
                }
            }
        }

        if (diastolicPressure != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "diastolicPressure");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                BigDecimal actualValue = BigDecimal.valueOf(diastolicPressure);
                if (threshold.getHighThreshold() != null && 
                    actualValue.compareTo(threshold.getHighThreshold()) > 0) {
                    isAbnormal = true;
                    String reason = "舒张压异常(" + diastolicPressure + "mmHg)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new HealthRecordService.WarningInfo(elderlyId, "diastolicPressure", 
                        actualValue, threshold.getHighThreshold(), 
                        "HIGH", "老人" + elderlyName + reason));
                } else if (threshold.getLowThreshold() != null && 
                    actualValue.compareTo(threshold.getLowThreshold()) < 0) {
                    isAbnormal = true;
                    String reason = "舒张压异常(" + diastolicPressure + "mmHg)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new HealthRecordService.WarningInfo(elderlyId, "diastolicPressure", 
                        actualValue, threshold.getLowThreshold(), 
                        "LOW", "老人" + elderlyName + reason));
                }
            }
        }

        if (heartRate != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "heartRate");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                BigDecimal actualValue = BigDecimal.valueOf(heartRate);
                if (threshold.getHighThreshold() != null && 
                    actualValue.compareTo(threshold.getHighThreshold()) > 0) {
                    isAbnormal = true;
                    String reason = "心率异常(" + heartRate + "次/分)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new HealthRecordService.WarningInfo(elderlyId, "heartRate", 
                        actualValue, threshold.getHighThreshold(), 
                        "HIGH", "老人" + elderlyName + reason));
                } else if (threshold.getLowThreshold() != null && 
                    actualValue.compareTo(threshold.getLowThreshold()) < 0) {
                    isAbnormal = true;
                    String reason = "心率异常(" + heartRate + "次/分)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new HealthRecordService.WarningInfo(elderlyId, "heartRate", 
                        actualValue, threshold.getLowThreshold(), 
                        "LOW", "老人" + elderlyName + reason));
                }
            }
        }

        if (bloodOxygen != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "bloodOxygen");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                BigDecimal actualValue = BigDecimal.valueOf(bloodOxygen);
                if (threshold.getLowThreshold() != null && 
                    actualValue.compareTo(threshold.getLowThreshold()) < 0) {
                    isAbnormal = true;
                    String reason = "血氧异常(" + bloodOxygen + "%)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new HealthRecordService.WarningInfo(elderlyId, "bloodOxygen", 
                        actualValue, threshold.getLowThreshold(), 
                        "LOW", "老人" + elderlyName + reason));
                }
            }
        }

        if (bloodSugar != null) {
            HealthWarningThreshold threshold = thresholdService.getThreshold(elderlyId, "bloodSugar");
            if (threshold.getEnabled() != null && threshold.getEnabled()) {
                if (threshold.getHighThreshold() != null && 
                    bloodSugar.compareTo(threshold.getHighThreshold()) > 0) {
                    isAbnormal = true;
                    String reason = "血糖异常(" + bloodSugar + "mmol/L)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new HealthRecordService.WarningInfo(elderlyId, "bloodSugar", 
                        bloodSugar, threshold.getHighThreshold(), 
                        "HIGH", "老人" + elderlyName + reason));
                } else if (threshold.getLowThreshold() != null && 
                    bloodSugar.compareTo(threshold.getLowThreshold()) < 0) {
                    isAbnormal = true;
                    String reason = "血糖异常(" + bloodSugar + "mmol/L)";
                    abnormalReasons.add(reason);
                    pendingWarnings.add(new HealthRecordService.WarningInfo(elderlyId, "bloodSugar", 
                        bloodSugar, threshold.getLowThreshold(), 
                        "LOW", "老人" + elderlyName + reason));
                }
            }
        }

        return isAbnormal;
    }

    private void invalidateOldWarnings(Integer healthRecordId) {
        LambdaQueryWrapper<HealthWarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthWarningRecord::getHealthRecordId, healthRecordId);
        List<HealthWarningRecord> oldWarnings = warningRecordService.list(wrapper);
        
        for (HealthWarningRecord oldWarning : oldWarnings) {
            if (!"HANDLED".equals(oldWarning.getStatus())) {
                oldWarning.setStatus("INVALIDATED");
                oldWarning.setHandleRemark("因健康记录更正而失效");
                oldWarning.setHandledAt(LocalDateTime.now());
                oldWarning.setHandledBy("system");
                warningRecordService.updateById(oldWarning);
            }
        }
    }
}
