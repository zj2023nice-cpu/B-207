package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.dto.WarningFollowupTaskCreateDTO;
import com.smart.elderly.dto.WarningFollowupTaskUpdateDTO;
import com.smart.elderly.entity.WarningFollowupTask;
import com.smart.elderly.enums.WarningFollowupTaskStatus;
import com.smart.elderly.service.WarningFollowupTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warning/followup-task")
public class WarningFollowupTaskController {

    @Autowired
    private WarningFollowupTaskService followupTaskService;

    @PostMapping("/create")
    public Result<WarningFollowupTask> createTask(@RequestBody WarningFollowupTaskCreateDTO dto) {
        try {
            WarningFollowupTask task = followupTaskService.createTask(dto);
            return Result.success(task);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<WarningFollowupTask> updateTask(@PathVariable Integer id,
                                                   @RequestBody WarningFollowupTaskUpdateDTO dto) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        try {
            WarningFollowupTask task = followupTaskService.updateTask(id, dto);
            return Result.success(task);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<WarningFollowupTask>> list(
            @RequestParam(required = false) Integer warningRecordId,
            @RequestParam(required = false) Boolean myTasks) {
        if (Boolean.TRUE.equals(myTasks)) {
            return Result.success(followupTaskService.getMyTasks());
        }
        if (warningRecordId != null) {
            return Result.success(followupTaskService.getByWarningRecordId(warningRecordId));
        }
        return Result.success(followupTaskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public Result<WarningFollowupTask> getDetail(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        try {
            WarningFollowupTask task = followupTaskService.getDetailById(id);
            return Result.success(task);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statuses")
    public Result<List<Map<String, String>>> getAllStatuses() {
        List<Map<String, String>> statuses = new ArrayList<>();
        for (WarningFollowupTaskStatus status : WarningFollowupTaskStatus.values()) {
            Map<String, String> item = new HashMap<>();
            item.put("code", status.getCode());
            item.put("name", status.getDisplayName());
            item.put("tagType", status.getTagType());
            statuses.add(item);
        }
        return Result.success(statuses);
    }

    @GetMapping("/upcoming")
    public Result<List<WarningFollowupTask>> getUpcomingDeadlineTasks(
            @RequestParam(defaultValue = "24") Integer hours) {
        try {
            return Result.success(followupTaskService.getUpcomingDeadlineTasks(hours));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/start/{id}")
    public Result<String> startTask(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        try {
            WarningFollowupTaskUpdateDTO dto = new WarningFollowupTaskUpdateDTO();
            dto.setStatus(WarningFollowupTaskStatus.IN_PROGRESS.getCode());
            followupTaskService.updateTask(id, dto);
            return Result.success("已开始处理");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/complete/{id}")
    public Result<String> completeTask(@PathVariable Integer id,
                                        @RequestBody Map<String, String> params) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        try {
            WarningFollowupTaskUpdateDTO dto = new WarningFollowupTaskUpdateDTO();
            dto.setStatus(WarningFollowupTaskStatus.COMPLETED.getCode());
            if (params != null) {
                dto.setRemark(params.get("remark"));
            }
            followupTaskService.updateTask(id, dto);
            return Result.success("任务已完成");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/cancel/{id}")
    public Result<String> cancelTask(@PathVariable Integer id,
                                      @RequestBody Map<String, String> params) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        try {
            WarningFollowupTaskUpdateDTO dto = new WarningFollowupTaskUpdateDTO();
            dto.setStatus(WarningFollowupTaskStatus.CANCELLED.getCode());
            if (params != null) {
                dto.setRemark(params.get("remark"));
            }
            followupTaskService.updateTask(id, dto);
            return Result.success("任务已取消");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
