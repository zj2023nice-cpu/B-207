package com.smart.elderly.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smart.elderly.common.Result;
import com.smart.elderly.dto.ShiftHandoverCreateDTO;
import com.smart.elderly.entity.ShiftHandoverRecord;
import com.smart.elderly.service.ShiftHandoverRecordService;
import com.smart.elderly.vo.ShiftHandoverDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shift-handover")
public class ShiftHandoverRecordController {

    @Autowired
    private ShiftHandoverRecordService handoverRecordService;

    @GetMapping("/list")
    public Result<List<ShiftHandoverRecord>> list() {
        return Result.success(handoverRecordService.getAll());
    }

    @GetMapping("/page")
    public Result<IPage<ShiftHandoverRecord>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(handoverRecordService.getByPage(pageNum, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Result<ShiftHandoverDetailVO> detail(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        ShiftHandoverDetailVO detail = handoverRecordService.getDetailById(id);
        if (detail == null) {
            return Result.error("记录不存在");
        }
        return Result.success(detail);
    }

    @PostMapping("/add")
    public Result<ShiftHandoverRecord> add(@RequestBody ShiftHandoverCreateDTO dto) {
        ShiftHandoverRecord record = handoverRecordService.create(dto);
        return Result.success(record);
    }

    @PutMapping("/update/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody ShiftHandoverCreateDTO dto) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        boolean success = handoverRecordService.update(id, dto);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        boolean success = handoverRecordService.delete(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    @GetMapping("/warning-ids/{id}")
    public Result<List<Integer>> getWarningIds(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        return Result.success(handoverRecordService.getWarningRecordIdsByHandoverId(id));
    }

    @PostMapping("/generate-summary")
    public Result<String> generateSummary(@RequestBody Map<String, List<Integer>> params) {
        List<Integer> warningIds = params.get("warningIds");
        String summary = handoverRecordService.generateWarningSummary(warningIds);
        return Result.success(summary);
    }
}
