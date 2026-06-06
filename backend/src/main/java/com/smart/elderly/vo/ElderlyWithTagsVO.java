package com.smart.elderly.vo;

import com.smart.elderly.entity.Elderly;
import com.smart.elderly.entity.ElderlyTag;
import lombok.Data;
import java.util.List;

@Data
public class ElderlyWithTagsVO {
    private Elderly elderly;
    private List<ElderlyTag> tags;
}
