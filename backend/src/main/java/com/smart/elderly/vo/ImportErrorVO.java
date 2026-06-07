package com.smart.elderly.vo;

import lombok.Data;

@Data
public class ImportErrorVO {
    private Integer rowNum;
    private String fieldName;
    private String errorMessage;

    public ImportErrorVO(Integer rowNum, String fieldName, String errorMessage) {
        this.rowNum = rowNum;
        this.fieldName = fieldName;
        this.errorMessage = errorMessage;
    }
}
