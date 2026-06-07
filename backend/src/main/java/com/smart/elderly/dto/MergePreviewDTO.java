package com.smart.elderly.dto;

import com.smart.elderly.entity.Elderly;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MergePreviewDTO {
    private Elderly primaryElderly;
    private List<Elderly> mergedElderlyList;
    private List<ConflictField> conflictFields;
    private Map<String, Integer> relatedDataCounts;

    @Data
    public static class ConflictField {
        private String fieldName;
        private String fieldLabel;
        private Object primaryValue;
        private Object recommendedValue;
        private List<ValueOption> options;
    }

    @Data
    public static class ValueOption {
        private String optionKey;
        private Object value;
        private List<String> sourceLabels;
        private boolean recommended;
    }
}
