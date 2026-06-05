package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.service.HealthWarningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warning/record")
public class HealthWarningRecordController {

    @Autowired
    private HealthWarningRecordService warningRecordService;

    @GetMapping("/list")
    public Result<List<HealthWarningRecord>> list() {
        return Result.success(warningRecordService.getAllWithElderlyName());
    }

    @GetMapping("/elderly/{elderlyId}")
    public Result<List<HealthWarningRecord>> getByElderlyId(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        return Result.success(warningRecordService.getByElderlyId(elderlyId));
    }

    @GetMapping("/pending")
    public Result<List<HealthWarningRecord>> getPending() {
        return Result.success(warningRecordService.getPending());
    }

    @GetMapping("/count")
    public Result<Map<String, Object>> countPending() {
        Map<String, Object> result = new HashMap<>();
        result.put("pendingCount", warningRecordService.countPending());
        return Result.success(result);
    }

    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        boolean success = warningRecordService.markAsRead(id);
        if (success) {
            return Result.success("已标记为已读");
        } else {
            return Result.error("操作失败");
        }
    }

    @PutMapping("/handle/{id}")
    public Result<String> handleWarning(@PathVariable Integer id, 
                                          @RequestBody Map<String, String> params) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        String handledBy = params.get("handledBy");
        String remark = params.get("remark");
        
        boolean success = warningRecordService.handleWarning(id, handledBy, remark);
        if (success) {
            return Result.success("处理成功");
        } else {
            return Result.error("处理失败");
        }
    }
}
