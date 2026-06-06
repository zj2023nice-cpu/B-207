package com.smart.elderly.dto;

import lombok.Data;
import java.util.List;

@Data
public class TagBindRequest {
    private List<Integer> elderlyIds;
    private List<Integer> tagIds;
}
