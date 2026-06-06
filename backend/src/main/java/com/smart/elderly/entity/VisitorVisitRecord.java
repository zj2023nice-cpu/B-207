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
@TableName("visitor_visit_records")
public class VisitorVisitRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @NotBlank(message = "来访人姓名不能为空")
    private String visitorName;

    @NotBlank(message = "联系电话不能为空")
    private String visitorPhone;

    @NotBlank(message = "与老人关系不能为空")
    private String relationWithElderly;

    @NotNull(message = "探访对象ID不能为空")
    private Integer elderlyId;

    @NotNull(message = "到访时间不能为空")
    private LocalDateTime visitTime;

    private LocalDateTime leaveTime;

    private String status;

    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String elderlyName;
}
