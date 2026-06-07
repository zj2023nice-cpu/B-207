package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.dto.WorkbenchQueryDTO;
import com.smart.elderly.service.WorkbenchService;
import com.smart.elderly.vo.WorkbenchItemVO;
import com.smart.elderly.vo.WorkbenchStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workbench")
public class WorkbenchController {

    @Autowired
    private WorkbenchService workbenchService;

    @GetMapping("/stats")
    public Result<WorkbenchStatsVO> getStats(@RequestParam(required = false) Integer userId) {
        if (userId == null) {
            userId = UserContextHolder.getUserId();
        }
        return Result.success(workbenchService.getStats(userId));
    }

    @PostMapping("/items")
    public Result<List<WorkbenchItemVO>> getItems(@RequestBody WorkbenchQueryDTO queryDTO) {
        if (queryDTO.getUserId() == null) {
            queryDTO.setUserId(UserContextHolder.getUserId());
        }
        if (queryDTO.getPageNum() == null) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null) {
            queryDTO.setPageSize(20);
        }
        return Result.success(workbenchService.getWorkbenchItems(queryDTO));
    }

    @GetMapping("/items")
    public Result<List<WorkbenchItemVO>> getItemsGet(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) List<Integer> elderlyIds,
            @RequestParam(required = false) List<String> priorities,
            @RequestParam(required = false) List<String> itemTypes,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        WorkbenchQueryDTO queryDTO = new WorkbenchQueryDTO();
        queryDTO.setUserId(userId != null ? userId : UserContextHolder.getUserId());
        queryDTO.setElderlyIds(elderlyIds);
        queryDTO.setPriorities(priorities);
        queryDTO.setItemTypes(itemTypes);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        return Result.success(workbenchService.getWorkbenchItems(queryDTO));
    }

    @GetMapping("/filter-options")
    public Result<Map<String, List<Map<String, String>>>> getFilterOptions() {
        Map<String, List<Map<String, String>>> options = new java.util.HashMap<>();
        options.put("itemTypes", workbenchService.getFilterOptions());
        options.put("priorities", workbenchService.getPriorityOptions());
        return Result.success(options);
    }
}
