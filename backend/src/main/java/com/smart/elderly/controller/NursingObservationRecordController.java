package com.smart.elderly.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.entity.NursingObservationRecord;
import com.smart.elderly.service.NursingObservationRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/nursing-observation")
public class NursingObservationRecordController {

    @Autowired
    private NursingObservationRecordService nursingObservationRecordService;

    @GetMapping("/list")
    public Result<List<NursingObservationRecord>> list() {
        return Result.success(nursingObservationRecordService.getRecordsWithNames());
    }

    @GetMapping("/page")
    public Result<IPage<NursingObservationRecord>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(nursingObservationRecordService.getRecordsWithPage(pageNum, pageSize));
    }

    @GetMapping("/list/{elderlyId}")
    public Result<List<NursingObservationRecord>> listByElderly(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        List<NursingObservationRecord> records = nursingObservationRecordService.getByElderlyId(elderlyId);
        return Result.success(records);
    }

    @GetMapping("/{id}")
    public Result<NursingObservationRecord> getById(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("记录ID不能为空");
        }
        NursingObservationRecord record = nursingObservationRecordService.getRecordById(id);
        return Result.success(record);
    }

    @OperationLog(operation = "添加护理观察记录", description = "添加新的护理观察记录")
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody NursingObservationRecord record) {
        nursingObservationRecordService.saveRecord(record);
        return Result.success("记录保存成功");
    }

    @OperationLog(operation = "编辑护理观察记录", description = "编辑护理观察记录")
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody NursingObservationRecord record) {
        if (record.getId() == null) {
            return Result.error("记录ID不能为空");
        }
        nursingObservationRecordService.saveRecord(record);
        return Result.success("记录更新成功");
    }

    @OperationLog(operation = "删除护理观察记录", description = "删除护理观察记录")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("记录ID不能为空");
        }
        nursingObservationRecordService.deleteRecord(id);
        return Result.success("记录删除成功");
    }
}
