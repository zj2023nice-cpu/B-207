package com.smart.elderly.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.entity.VisitorVisitRecord;
import com.smart.elderly.service.VisitorVisitRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/visitor-visit")
public class VisitorVisitRecordController {

    @Autowired
    private VisitorVisitRecordService visitorVisitRecordService;

    @GetMapping("/list")
    public Result<List<VisitorVisitRecord>> list() {
        return Result.success(visitorVisitRecordService.getRecordsWithNames());
    }

    @GetMapping("/page")
    public Result<IPage<VisitorVisitRecord>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer elderlyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(visitorVisitRecordService.getRecordsWithFilters(
                pageNum, pageSize, elderlyId, status, startTime, endTime
        ));
    }

    @GetMapping("/list/{elderlyId}")
    public Result<List<VisitorVisitRecord>> listByElderly(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        List<VisitorVisitRecord> records = visitorVisitRecordService.getByElderlyId(elderlyId);
        return Result.success(records);
    }

    @GetMapping("/{id}")
    public Result<VisitorVisitRecord> getById(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("记录ID不能为空");
        }
        VisitorVisitRecord record = visitorVisitRecordService.getRecordById(id);
        return Result.success(record);
    }

    @OperationLog(operation = "添加访客来访记录", description = "添加新的访客来访记录")
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody VisitorVisitRecord record) {
        visitorVisitRecordService.saveRecord(record);
        return Result.success("记录保存成功");
    }

    @OperationLog(operation = "编辑访客来访记录", description = "编辑访客来访记录")
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody VisitorVisitRecord record) {
        if (record.getId() == null) {
            return Result.error("记录ID不能为空");
        }
        visitorVisitRecordService.saveRecord(record);
        return Result.success("记录更新成功");
    }

    @OperationLog(operation = "访客离开登记", description = "登记访客离开")
    @PutMapping("/leave/{id}")
    public Result<String> registerLeave(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("记录ID不能为空");
        }
        visitorVisitRecordService.registerLeave(id);
        return Result.success("离开登记成功");
    }

    @OperationLog(operation = "删除访客来访记录", description = "删除访客来访记录")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("记录ID不能为空");
        }
        visitorVisitRecordService.deleteRecord(id);
        return Result.success("记录删除成功");
    }
}
