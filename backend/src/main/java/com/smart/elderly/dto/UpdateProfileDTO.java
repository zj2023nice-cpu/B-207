package com.smart.elderly.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;

@Data
public class UpdateProfileDTO {
    @Length(max = 100, message = "展示名称长度不能超过100个字符")
    private String displayName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
