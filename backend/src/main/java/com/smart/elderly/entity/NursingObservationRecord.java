package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@TableName("nursing_observation_records")
public class NursingObservationRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @NotNull(message = "老人ID不能为空")
    private Integer elderlyId;

    @NotNull(message = "观察时间不能为空")
    private LocalDateTime observationTime;

    @NotBlank(message = "观察人不能为空")
    private String observer;

    @NotBlank(message = "观察类型不能为空")
    private String observationType;

    @NotBlank(message = "备注内容不能为空")
    private String remark;

    @NotNull(message = "是否需要后续跟进不能为空")
    private Boolean needFollowUp;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String elderlyName;
}
