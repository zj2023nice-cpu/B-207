package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.entity.HealthWarningThreshold;
import com.smart.elderly.service.HealthWarningThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warning/threshold")
public class HealthWarningThresholdController {

    @Autowired
    private HealthWarningThresholdService thresholdService;

    @GetMapping("/init")
    public Result<String> initDefaults() {
        thresholdService.initSystemDefaults();
        return Result.success("默认阈值初始化完成");
    }

    @GetMapping("/system")
    public Result<List<HealthWarningThreshold>> getSystemDefaults() {
        return Result.success(thresholdService.getSystemDefaults());
    }

    @GetMapping("/elderly/{elderlyId}")
    public Result<List<HealthWarningThreshold>> getByElderlyId(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        return Result.success(thresholdService.getByElderlyId(elderlyId));
    }

    @GetMapping("/effective/{elderlyId}")
    public Result<Map<String, Object>> getEffectiveThresholds(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        return Result.success(thresholdService.getEffectiveThresholds(elderlyId));
    }

    @GetMapping("/{elderlyId}/{indicatorType}")
    public Result<HealthWarningThreshold> getThreshold(@PathVariable Integer elderlyId, 
                                                         @PathVariable String indicatorType) {
        if (elderlyId == null || indicatorType == null) {
            return Result.error("参数不能为空");
        }
        return Result.success(thresholdService.getThreshold(elderlyId, indicatorType));
    }

    @PostMapping("/save")
    public Result<String> saveThreshold(@Valid @RequestBody HealthWarningThreshold threshold) {
        boolean success = thresholdService.saveOrUpdateThreshold(threshold);
        if (success) {
            return Result.success("保存成功");
        } else {
            return Result.error("保存失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteThreshold(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        boolean success = thresholdService.deleteThreshold(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}
