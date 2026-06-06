package com.smart.elderly.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShiftHandoverCreateDTO {
    @NotBlank(message = "交班人不能为空")
    private String handoverPerson;

    @NotBlank(message = "接班人不能为空")
    private String takeoverPerson;

    @NotNull(message = "交接时间不能为空")
    private LocalDateTime handoverTime;

    private String keyElderly;

    private String pendingWarningSummary;

    private String remarks;

    private List<Integer> warningRecordIds;
}
