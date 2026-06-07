package com.smart.elderly.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ElderlyImportVO {
    @ExcelProperty(index = 0)
    private String name;

    @ExcelProperty(index = 1)
    private Object ageStr;

    @ExcelProperty(index = 2)
    private String gender;

    @ExcelProperty(index = 3)
    private String phone;

    @ExcelProperty(index = 4)
    private String address;

    @ExcelProperty(index = 5)
    private String emergencyContactName;

    @ExcelProperty(index = 6)
    private String emergencyContactPhone;

    @ExcelProperty(index = 7)
    private String emergencyContactRelation;

    @ExcelProperty(index = 8)
    private String status;
}
