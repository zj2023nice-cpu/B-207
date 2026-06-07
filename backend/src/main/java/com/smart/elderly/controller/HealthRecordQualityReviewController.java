package com.smart.elderly.controller;

import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.dto.QualityReviewSubmitDTO;
import com.smart.elderly.entity.HealthRecordQualityReview;
import com.smart.elderly.service.HealthRecordQualityReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quality-review")
public class HealthRecordQualityReviewController {

    @Autowired
    private HealthRecordQualityReviewService reviewService;

    @GetMapping("/list")
    public Result<List<HealthRecordQualityReview>> list(
            @RequestParam(required = false) Integer elderlyId,
            @RequestParam(required = false) String reviewStatus) {
        return Result.success(reviewService.getReviewsWithFilters(elderlyId, reviewStatus));
    }

    @GetMapping("/detail/{id}")
    public Result<HealthRecordQualityReview> detail(@PathVariable Integer id) {
        return Result.success(reviewService.getReviewDetail(id));
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> stats() {
        return Result.success(reviewService.getReviewStats());
    }

    @OperationLog(operation = "提交质量复核", description = "提交健康记录质量复核结论")
    @PostMapping("/submit")
    public Result<String> submit(@Valid @RequestBody QualityReviewSubmitDTO dto) {
        reviewService.submitReview(dto);
        return Result.success("复核提交成功");
    }

    @GetMapping("/by-record/{healthRecordId}")
    public Result<HealthRecordQualityReview> getByRecordId(@PathVariable Integer healthRecordId) {
        return Result.success(reviewService.getByHealthRecordId(healthRecordId));
    }
}
