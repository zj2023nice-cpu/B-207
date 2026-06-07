package com.smart.elderly.dto;

import com.smart.elderly.entity.Elderly;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class MergePreviewDTO {
    private Elderly primaryElderly;
    private Elderly mergedElderly;
    private List<ConflictField> conflictFields;
    private Map<String, Integer> relatedDataCounts;
    
    @Data
    public static class ConflictField {
        private String fieldName;
        private String fieldLabel;
        private Object primaryValue;
        private Object mergedValue;
        private Object recommendedValue;
    }
}
