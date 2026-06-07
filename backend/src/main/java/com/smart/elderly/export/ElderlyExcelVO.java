package com.smart.elderly.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ElderlyExcelVO {
    @ExcelProperty("ID")
    @ColumnWidth(10)
    private Integer id;

    @ExcelProperty("姓名")
    @ColumnWidth(15)
    private String name;

    @ExcelProperty("年龄")
    @ColumnWidth(10)
    private Integer age;

    @ExcelProperty("性别")
    @ColumnWidth(10)
    private String gender;

    @ExcelProperty("电话")
    @ColumnWidth(20)
    private String phone;

    @ExcelProperty("地址")
    @ColumnWidth(30)
    private String address;

    @ExcelProperty("紧急联系人")
    @ColumnWidth(15)
    private String emergencyContactName;

    @ExcelProperty("紧急联系人电话")
    @ColumnWidth(20)
    private String emergencyContactPhone;

    @ExcelProperty("紧急联系人关系")
    @ColumnWidth(15)
    private String emergencyContactRelation;

    @ExcelProperty("状态")
    @ColumnWidth(12)
    private String status;

    @ExcelProperty("创建时间")
    @ColumnWidth(25)
    private LocalDateTime createdAt;
}
