package com.smart.elderly.controller;

import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.entity.HealthRecord;
import com.smart.elderly.service.HealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    @GetMapping("/history")
    public Result<List<HealthRecord>> history() {
        return Result.success(healthRecordService.getRecordsWithNames());
    }

    @OperationLog(operation = "添加健康记录", description = "添加新的健康记录")
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody HealthRecord record) {
        healthRecordService.saveRecord(record);
        return Result.success("记录保存成功");
    }

    @GetMapping("/trend/{elderlyId}")
    public Result<Map<String, Object>> trend(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        Map<String, Object> trendData = healthRecordService.getTrendData(elderlyId);
        return Result.success(trendData);
    }

    @GetMapping("/list/{elderlyId}")
    public Result<List<HealthRecord>> listByElderly(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        List<HealthRecord> records = healthRecordService.getByElderlyId(elderlyId);
        return Result.success(records);
    }
}
