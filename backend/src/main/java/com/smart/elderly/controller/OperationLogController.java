package com.smart.elderly.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smart.elderly.common.Result;
import com.smart.elderly.entity.OperationLog;
import com.smart.elderly.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/page")
    public Result<Page<OperationLog>> getLogPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Page<OperationLog> page = operationLogService.getLogPage(pageNum, pageSize, username, operation, startTime, endTime);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<OperationLog> getById(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        OperationLog log = operationLogService.getById(id);
        return Result.success(log);
    }
}
