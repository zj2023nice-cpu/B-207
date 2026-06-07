package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.dto.ExportTaskCreateDTO;
import com.smart.elderly.entity.ExportTask;
import com.smart.elderly.export.ExportDataProvider;
import com.smart.elderly.mapper.ExportTaskMapper;
import com.smart.elderly.vo.ExportTaskVO;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExportTaskService extends ServiceImpl<ExportTaskMapper, ExportTask> implements ApplicationContextAware {

    @Autowired
    private ExportTaskMapper exportTaskMapper;

    private ApplicationContext applicationContext;
    private Map<String, ExportDataProvider> providerMap = new HashMap<>();

    private static final String EXPORT_BASE_PATH = System.getProperty("user.dir") + File.separator + "export_files";
    private static final Map<String, String> STATUS_DESC_MAP = new HashMap<>();
    private static final Map<String, String> EXPORT_TYPE_DESC_MAP = new HashMap<>();

    static {
        STATUS_DESC_MAP.put("PENDING", "待处理");
        STATUS_DESC_MAP.put("PROCESSING", "处理中");
        STATUS_DESC_MAP.put("COMPLETED", "已完成");
        STATUS_DESC_MAP.put("FAILED", "失败");
        STATUS_DESC_MAP.put("CANCELLED", "已取消");

        EXPORT_TYPE_DESC_MAP.put("ELDERLY", "老人管理");
        EXPORT_TYPE_DESC_MAP.put("HEALTH_RECORD", "健康记录");
        EXPORT_TYPE_DESC_MAP.put("WARNING_RECORD", "预警记录");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        Map<String, ExportDataProvider> beans = applicationContext.getBeansOfType(ExportDataProvider.class);
        for (ExportDataProvider provider : beans.values()) {
            providerMap.put(provider.getExportType(), provider);
        }
    }

    public ExportTask createTask(ExportTaskCreateDTO dto) {
        Integer userId = UserContextHolder.getUserId();
        String username = UserContextHolder.getUsername();
        ExportTask task = new ExportTask();
        task.setTaskName(dto.getTaskName());
        task.setExportType(dto.getExportType());
        task.setExportParams(dto.getExportParams());
        task.setExportRangeDesc(dto.getExportRangeDesc());
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setMaxRetry(3);
        task.setCreatedBy(userId);
        task.setCreatedByName(username != null ? username : "系统");
        task.setCreatedAt(LocalDateTime.now());
        exportTaskMapper.insert(task);
        return task;
    }

    @Async("exportTaskExecutor")
    public void processTaskAsync(Integer taskId) {
        ExportTask task = exportTaskMapper.selectById(taskId);
        if (task == null || !"PENDING".equals(task.getStatus())) {
            return;
        }
        try {
            task.setStatus("PROCESSING");
            task.setStartedAt(LocalDateTime.now());
            exportTaskMapper.updateById(task);

            ExportDataProvider provider = providerMap.get(task.getExportType());
            if (provider == null) {
                throw new RuntimeException("不支持的导出类型: " + task.getExportType());
            }

            List<?> dataList = provider.fetchData(task.getExportParams());

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String dirPath = EXPORT_BASE_PATH + File.separator + dateStr;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = provider.generateFileName() + "_" + taskId + ".xlsx";
            String filePath = dirPath + File.separator + fileName;

            com.alibaba.excel.EasyExcel.write(filePath)
                    .head(provider.getHeaders())
                    .sheet("数据")
                    .doWrite(dataList);

            File file = new File(filePath);
            task.setStatus("COMPLETED");
            task.setFileName(fileName);
            task.setFilePath(filePath);
            task.setFileSize(file.length());
            task.setTotalCount(dataList.size());
            task.setCompletedAt(LocalDateTime.now());
            exportTaskMapper.updateById(task);

        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            exportTaskMapper.updateById(task);
        }
    }

    public List<ExportTaskVO> listMyTasks() {
        Integer userId = UserContextHolder.getUserId();
        QueryWrapper<ExportTask> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("created_by", userId);
        }
        wrapper.orderByDesc("created_at");
        List<ExportTask> tasks = exportTaskMapper.selectList(wrapper);
        List<ExportTaskVO> voList = new ArrayList<>();
        for (ExportTask task : tasks) {
            voList.add(convertToVO(task));
        }
        return voList;
    }

    private ExportTaskVO convertToVO(ExportTask task) {
        ExportTaskVO vo = new ExportTaskVO();
        org.springframework.beans.BeanUtils.copyProperties(task, vo);
        vo.setStatusDesc(STATUS_DESC_MAP.getOrDefault(task.getStatus(), task.getStatus()));
        vo.setExportTypeDesc(EXPORT_TYPE_DESC_MAP.getOrDefault(task.getExportType(), task.getExportType()));
        vo.setCanDownload("COMPLETED".equals(task.getStatus()) && task.getFilePath() != null);
        vo.setCanRetry("FAILED".equals(task.getStatus()) && task.getRetryCount() < task.getMaxRetry());
        return vo;
    }

    public ExportTask getTaskById(Integer id) {
        return exportTaskMapper.selectById(id);
    }

    public boolean cancelTask(Integer id) {
        ExportTask task = exportTaskMapper.selectById(id);
        if (task == null) {
            return false;
        }
        if ("PENDING".equals(task.getStatus())) {
            task.setStatus("CANCELLED");
            task.setCompletedAt(LocalDateTime.now());
            exportTaskMapper.updateById(task);
            return true;
        }
        return false;
    }

    public boolean retryTask(Integer id) {
        ExportTask task = exportTaskMapper.selectById(id);
        if (task == null) {
            return false;
        }
        if ("FAILED".equals(task.getStatus()) && task.getRetryCount() < task.getMaxRetry()) {
            task.setStatus("PENDING");
            task.setRetryCount(task.getRetryCount() + 1);
            task.setErrorMessage(null);
            task.setStartedAt(null);
            task.setCompletedAt(null);
            exportTaskMapper.updateById(task);
            processTaskAsync(id);
            return true;
        }
        return false;
    }

    public File getExportFile(Integer id) {
        ExportTask task = exportTaskMapper.selectById(id);
        if (task == null || !"COMPLETED".equals(task.getStatus()) || task.getFilePath() == null) {
            return null;
        }
        File file = new File(task.getFilePath());
        return file.exists() ? file : null;
    }
}
