package com.smart.elderly.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ElderlyImportVO {
    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("年龄")
    private String ageStr;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("居住地址")
    private String address;

    @ExcelProperty("紧急联系人")
    private String emergencyContactName;

    @ExcelProperty("紧急联系人电话")
    private String emergencyContactPhone;

    @ExcelProperty("紧急联系人关系")
    private String emergencyContactRelation;

    @ExcelProperty("状态")
    private String status;
}
