package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("health_record_quality_reviews")
public class HealthRecordQualityReview {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer healthRecordId;
    private Integer elderlyId;
    private String reviewStatus;
    private String triggeredRules;
    private String issuesSummary;
    private Integer qualityScore;
    private Integer reviewerId;
    private String reviewerName;
    private LocalDateTime reviewTime;
    private String reviewConclusion;
    private String ignoreReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String elderlyName;
    
    @TableField(exist = false)
    private HealthRecord healthRecord;
}
