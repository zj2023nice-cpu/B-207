package com.smart.elderly.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HealthRecordExcelVO {
    @ExcelProperty("ID")
    @ColumnWidth(10)
    private Integer id;

    @ExcelProperty("老人姓名")
    @ColumnWidth(15)
    private String elderlyName;

    @ExcelProperty("血压")
    @ColumnWidth(15)
    private String bloodPressure;

    @ExcelProperty("收缩压")
    @ColumnWidth(12)
    private Integer systolicPressure;

    @ExcelProperty("舒张压")
    @ColumnWidth(12)
    private Integer diastolicPressure;

    @ExcelProperty("体温")
    @ColumnWidth(10)
    private BigDecimal temperature;

    @ExcelProperty("心率")
    @ColumnWidth(10)
    private Integer heartRate;

    @ExcelProperty("血氧")
    @ColumnWidth(10)
    private Integer bloodOxygen;

    @ExcelProperty("血糖")
    @ColumnWidth(10)
    private BigDecimal bloodSugar;

    @ExcelProperty("是否异常")
    @ColumnWidth(12)
    private String isAbnormalDesc;

    @ExcelProperty("异常原因")
    @ColumnWidth(25)
    private String abnormalReason;

    @ExcelProperty("检测时间")
    @ColumnWidth(25)
    private LocalDateTime checkTime;
}
