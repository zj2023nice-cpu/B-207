package com.smart.elderly.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WarningRecordExcelVO {
    @ExcelProperty("ID")
    @ColumnWidth(10)
    private Integer id;

    @ExcelProperty("老人姓名")
    @ColumnWidth(15)
    private String elderlyName;

    @ExcelProperty("指标类型")
    @ColumnWidth(15)
    private String indicatorTypeDesc;

    @ExcelProperty("实际值")
    @ColumnWidth(12)
    private BigDecimal actualValue;

    @ExcelProperty("阈值")
    @ColumnWidth(12)
    private BigDecimal thresholdValue;

    @ExcelProperty("预警级别")
    @ColumnWidth(12)
    private String warningLevel;

    @ExcelProperty("预警消息")
    @ColumnWidth(30)
    private String warningMessage;

    @ExcelProperty("状态")
    @ColumnWidth(12)
    private String statusDesc;

    @ExcelProperty("创建时间")
    @ColumnWidth(25)
    private LocalDateTime createdAt;

    @ExcelProperty("处理时间")
    @ColumnWidth(25)
    private LocalDateTime handledAt;

    @ExcelProperty("处理人")
    @ColumnWidth(15)
    private String handledBy;

    @ExcelProperty("处理备注")
    @ColumnWidth(30)
    private String handleRemark;
}
