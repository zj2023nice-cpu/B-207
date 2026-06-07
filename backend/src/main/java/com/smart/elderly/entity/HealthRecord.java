package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("health_records")
public class HealthRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @NotNull(message = "老人ID不能为空")
    private Integer elderlyId;
    
    private String bloodPressure;
    private Integer systolicPressure;
    private Integer diastolicPressure;
    private BigDecimal temperature;
    private Integer heartRate;
    private Integer bloodOxygen;
    private BigDecimal bloodSugar;
    private String abnormalReason;
    private Boolean isAbnormal;
    private LocalDateTime checkTime;
    
    private Boolean corrected;
    private LocalDateTime correctedAt;
    private String correctedBy;
    private Integer latestCorrectionId;

    @TableField(exist = false)
    private String elderlyName;
}
