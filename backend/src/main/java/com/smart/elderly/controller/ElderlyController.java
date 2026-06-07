package com.smart.elderly.controller;

import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.dto.DuplicateElderlyGroupDTO;
import com.smart.elderly.dto.ElderlyMergeDTO;
import com.smart.elderly.dto.MergePreviewDTO;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.service.ElderlyMergeService;
import com.smart.elderly.service.ElderlyService;
import com.smart.elderly.vo.ImportResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/elderly")
public class ElderlyController {

    @Autowired
    private ElderlyService elderlyService;

    @Autowired
    private ElderlyMergeService elderlyMergeService;

    @GetMapping("/list")
    public Result<List<Elderly>> list() {
        return Result.success(elderlyService.list());
    }

    @OperationLog(operation = "添加老人信息", description = "添加新的老人信息")
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody Elderly elderly) {
        elderlyService.save(elderly);
        return Result.success("添加成功");
    }

    @OperationLog(operation = "修改老人信息", description = "修改已存在的老人信息")
    @PutMapping("/update")
    public Result<String> update(@RequestBody Elderly elderly) {
        if (elderly.getId() == null) {
            return Result.error("ID不能为空");
        }
        elderlyService.updateById(elderly);
        return Result.success("修改成功");
    }

    @OperationLog(operation = "删除老人信息", description = "根据ID删除老人信息")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        elderlyService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/import/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        elderlyService.downloadTemplate(response);
    }

    @OperationLog(operation = "批量导入老人信息", description = "批量导入老人信息")
    @PostMapping("/import")
    public Result<ImportResultVO> importData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择上传文件");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return Result.error("文件格式不正确，请上传Excel文件(.xlsx或.xls)");
        }
        try {
            ImportResultVO result = elderlyService.importData(file);
            if (result.getHasError()) {
                return Result.success(result);
            }
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("文件解析失败：" + e.getMessage());
        }
    }

    @OperationLog(operation = "检测重复档案", description = "检测系统中潜在的重复老人档案")
    @GetMapping("/duplicates/detect")
    public Result<List<DuplicateElderlyGroupDTO>> detectDuplicates() {
        return Result.success(elderlyMergeService.detectDuplicates());
    }

    @GetMapping("/merge/preview")
    public Result<MergePreviewDTO> getMergePreview(
            @RequestParam Integer primaryId,
            @RequestParam Integer mergedId) {
        return Result.success(elderlyMergeService.getMergePreview(primaryId, mergedId));
    }

    @OperationLog(operation = "合并老人档案", description = "合并两个重复的老人档案")
    @PostMapping("/merge")
    public Result<String> mergeElderly(@Valid @RequestBody ElderlyMergeDTO mergeDTO) {
        try {
            elderlyMergeService.mergeElderly(mergeDTO, "system");
            return Result.success("合并成功");
        } catch (Exception e) {
            return Result.error("合并失败：" + e.getMessage());
        }
    }
}
