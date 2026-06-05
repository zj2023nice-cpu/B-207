package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@TableName("elderly")
public class Elderly {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @NotBlank(message = "姓名不能为空")
    private String name;
    private Integer age;
    private String gender;
    private String phone;
    private String address;
    
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    
    private String status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
