package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("health_record_corrections")
public class HealthRecordCorrection {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer healthRecordId;
    
    private Integer elderlyId;
    
    private String beforeBloodPressure;
    private Integer beforeSystolicPressure;
    private Integer beforeDiastolicPressure;
    private BigDecimal beforeTemperature;
    private Integer beforeHeartRate;
    private Integer beforeBloodOxygen;
    private BigDecimal beforeBloodSugar;
    private String beforeAbnormalReason;
    private Boolean beforeIsAbnormal;
    
    private String afterBloodPressure;
    private Integer afterSystolicPressure;
    private Integer afterDiastolicPressure;
    private BigDecimal afterTemperature;
    private Integer afterHeartRate;
    private Integer afterBloodOxygen;
    private BigDecimal afterBloodSugar;
    private String afterAbnormalReason;
    private Boolean afterIsAbnormal;
    
    private String correctionReason;
    private String correctedBy;
    private LocalDateTime correctedAt;
    private String correctionRemark;
    
    private String status;
    private Integer version;

    @TableField(exist = false)
    private String elderlyName;
}
