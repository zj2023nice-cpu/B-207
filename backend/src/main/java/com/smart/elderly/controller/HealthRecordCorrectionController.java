package com.smart.elderly.controller;

import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.dto.HealthRecordCorrectionDTO;
import com.smart.elderly.entity.HealthRecordCorrection;
import com.smart.elderly.service.HealthRecordCorrectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/health/correction")
public class HealthRecordCorrectionController {

    @Autowired
    private HealthRecordCorrectionService correctionService;

    @GetMapping("/list/{healthRecordId}")
    public Result<List<HealthRecordCorrection>> listByHealthRecord(@PathVariable Integer healthRecordId) {
        if (healthRecordId == null) {
            return Result.error("健康记录ID不能为空");
        }
        List<HealthRecordCorrection> corrections = correctionService.getByHealthRecordId(healthRecordId);
        return Result.success(corrections);
    }

    @GetMapping("/elderly/{elderlyId}")
    public Result<List<HealthRecordCorrection>> listByElderly(@PathVariable Integer elderlyId) {
        if (elderlyId == null) {
            return Result.error("老人ID不能为空");
        }
        List<HealthRecordCorrection> corrections = correctionService.getByElderlyId(elderlyId);
        return Result.success(corrections);
    }

    @OperationLog(operation = "更正健康记录", description = "创建健康记录数据更正")
    @PostMapping("/create")
    public Result<HealthRecordCorrection> create(@Valid @RequestBody HealthRecordCorrectionDTO dto) {
        String currentUser = UserContextHolder.getUsername();
        if (currentUser == null) {
            currentUser = "system";
        }
        HealthRecordCorrection correction = correctionService.createCorrection(dto, currentUser);
        return Result.success(correction);
    }
}
