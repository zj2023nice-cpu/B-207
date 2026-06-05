package com.smart.elderly.controller;

import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.service.ElderlyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/elderly")
public class ElderlyController {

    @Autowired
    private ElderlyService elderlyService;

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
}
