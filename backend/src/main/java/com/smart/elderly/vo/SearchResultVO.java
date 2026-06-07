package com.smart.elderly.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SearchResultVO {
    private String keyword;
    private Long total;
    private Map<String, Long> moduleCounts;
    private List<SearchResultItemVO> items;
}
