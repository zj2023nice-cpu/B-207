package com.smart.elderly.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResultVO {
    private Integer totalCount;
    private Integer successCount;
    private Integer errorCount;
    private List<ImportErrorVO> errors;
    private Boolean hasError;

    public ImportResultVO() {
        this.totalCount = 0;
        this.successCount = 0;
        this.errorCount = 0;
        this.errors = new ArrayList<>();
        this.hasError = false;
    }

    public void addError(Integer rowNum, String fieldName, String errorMessage) {
        this.errors.add(new ImportErrorVO(rowNum, fieldName, errorMessage));
        this.errorCount++;
        this.hasError = true;
    }

    public void incrementSuccess() {
        this.successCount++;
    }

    public void incrementTotal() {
        this.totalCount++;
    }
}
