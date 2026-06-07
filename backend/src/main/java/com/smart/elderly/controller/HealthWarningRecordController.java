package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.enums.HealthWarningStatus;
import com.smart.elderly.service.HealthWarningRecordService;
import com.smart.elderly.vo.HealthWarningDetailVO;
import com.smart.elderly.vo.HealthWarningTransitionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/warning/record")
public class HealthWarningRecordController {

    @Autowired
    private HealthWarningRecordService warningRecordService;

    @GetMapping("/list")
    public Result<List<HealthWarningRecord>> list(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return Result.success(warningRecordService.getByStatus(status));
        }
        return Result.success(warningRecordService.getAllWithElderlyName());
    }

    @GetMapping("/{id}")
    public Result<HealthWarningDetailVO> getDetail(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        HealthWarningDetailVO detail = warningRecordService.getDetailById(id);
        if (detail == null) {
            return Result.error("记录不存在");
        }
        return Result.success(detail);
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

    @GetMapping("/statuses")
    public Result<List<Map<String, String>>> getAllStatuses() {
        List<Map<String, String>> statuses = new java.util.ArrayList<>();
        for (HealthWarningStatus status : HealthWarningStatus.values()) {
            Map<String, String> item = new HashMap<>();
            item.put("code", status.getCode());
            item.put("name", status.getDisplayName());
            item.put("tagType", status.getTagType());
            statuses.add(item);
        }
        return Result.success(statuses);
    }

    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        try {
            boolean success = warningRecordService.markAsRead(id);
            if (success) {
                return Result.success("已标记为已读");
            } else {
                return Result.error("操作失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
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
        
        try {
            boolean success = warningRecordService.handleWarning(id, handledBy, remark);
            if (success) {
                return Result.success("处理成功");
            } else {
                return Result.error("处理失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/ignore/{id}")
    public Result<String> ignoreWarning(@PathVariable Integer id, 
                                         @RequestBody Map<String, String> params) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        String operator = params.get("operator");
        String remark = params.get("remark");
        
        try {
            boolean success = warningRecordService.ignoreWarning(id, operator, remark);
            if (success) {
                return Result.success("已忽略该预警");
            } else {
                return Result.error("操作失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/reopen/{id}")
    public Result<String> reopenWarning(@PathVariable Integer id, 
                                         @RequestBody Map<String, String> params) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        String operator = params.get("operator");
        String remark = params.get("remark");
        
        try {
            boolean success = warningRecordService.reopenWarning(id, operator, remark);
            if (success) {
                return Result.success("已重新打开");
            } else {
                return Result.error("操作失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/escalate/{id}")
    public Result<String> escalateWarning(@PathVariable Integer id, 
                                           @RequestBody Map<String, String> params) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        String operator = params.get("operator");
        String remark = params.get("remark");
        
        try {
            boolean success = warningRecordService.escalateWarning(id, operator, remark);
            if (success) {
                return Result.success("已升级处理");
            } else {
                return Result.error("操作失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/transition/{id}")
    public Result<String> transitionStatus(@PathVariable Integer id, 
                                            @RequestBody HealthWarningTransitionVO vo) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        if (vo.getTargetStatus() == null) {
            return Result.error("目标状态不能为空");
        }
        
        try {
            HealthWarningStatus targetStatus = HealthWarningStatus.fromCode(vo.getTargetStatus());
            boolean success = warningRecordService.transitionStatus(
                id, targetStatus, vo.getOperator(), vo.getRemark()
            );
            if (success) {
                return Result.success("状态更新成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
