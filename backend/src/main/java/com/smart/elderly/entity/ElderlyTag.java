package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@TableName("elderly_tag")
public class ElderlyTag {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @NotBlank(message = "标签名称不能为空")
    private String name;
    
    private String color;
    
    private String status;
    
    private Integer sort;
    
    private String remark;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
