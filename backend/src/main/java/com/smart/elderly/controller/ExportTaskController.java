package com.smart.elderly.controller;

import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.dto.ExportTaskCreateDTO;
import com.smart.elderly.entity.ExportTask;
import com.smart.elderly.service.ExportTaskService;
import com.smart.elderly.vo.ExportTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/export-task")
public class ExportTaskController {

    @Autowired
    private ExportTaskService exportTaskService;

    @PostMapping("/create")
    @OperationLog(operation = "创建导出任务", description = "创建数据导出任务")
    public Result<ExportTask> createTask(@RequestBody ExportTaskCreateDTO dto) {
        ExportTask task = exportTaskService.createTask(dto);
        exportTaskService.processTaskAsync(task.getId());
        return Result.success(task);
    }

    @GetMapping("/list")
    public Result<List<ExportTaskVO>> listMyTasks() {
        return Result.success(exportTaskService.listMyTasks());
    }

    @GetMapping("/{id}")
    public Result<ExportTask> getTaskById(@PathVariable Integer id) {
        return Result.success(exportTaskService.getTaskById(id));
    }

    @PostMapping("/cancel/{id}")
    @OperationLog(operation = "取消导出任务", description = "取消数据导出任务")
    public Result<String> cancelTask(@PathVariable Integer id) {
        boolean success = exportTaskService.cancelTask(id);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    @PostMapping("/retry/{id}")
    @OperationLog(operation = "重试导出任务", description = "重试失败的导出任务")
    public Result<String> retryTask(@PathVariable Integer id) {
        boolean success = exportTaskService.retryTask(id);
        return success ? Result.success("重试成功") : Result.error("重试失败");
    }

    @GetMapping("/download/{id}")
    @OperationLog(operation = "下载导出文件", description = "下载导出任务生成的文件")
    public ResponseEntity<Resource> downloadFile(@PathVariable Integer id) {
        File file = exportTaskService.getExportFile(id);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Resource resource = new FileSystemResource(file);
            String fileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.name());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
