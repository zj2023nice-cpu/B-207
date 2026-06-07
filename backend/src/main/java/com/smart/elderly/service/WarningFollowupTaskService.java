package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.dto.WarningFollowupTaskCreateDTO;
import com.smart.elderly.dto.WarningFollowupTaskUpdateDTO;
import com.smart.elderly.entity.Elderly;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.entity.Notification;
import com.smart.elderly.entity.WarningFollowupTask;
import com.smart.elderly.enums.WarningFollowupTaskStatus;
import com.smart.elderly.mapper.ElderlyMapper;
import com.smart.elderly.mapper.HealthWarningRecordMapper;
import com.smart.elderly.mapper.WarningFollowupTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WarningFollowupTaskService extends ServiceImpl<WarningFollowupTaskMapper, WarningFollowupTask> {

    @Autowired
    private HealthWarningRecordMapper warningRecordMapper;

    @Autowired
    private ElderlyMapper elderlyMapper;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public WarningFollowupTask createTask(WarningFollowupTaskCreateDTO dto) {
        if (dto.getWarningRecordId() == null) {
            throw new IllegalArgumentException("预警记录ID不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("任务标题不能为空");
        }
        if (dto.getDeadline() == null) {
            throw new IllegalArgumentException("截止时间不能为空");
        }

        HealthWarningRecord warningRecord = warningRecordMapper.selectById(dto.getWarningRecordId());
        if (warningRecord == null) {
            throw new IllegalArgumentException("预警记录不存在");
        }

        WarningFollowupTask task = new WarningFollowupTask();
        task.setWarningRecordId(dto.getWarningRecordId());
        task.setElderlyId(warningRecord.getElderlyId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setAssigneeId(dto.getAssigneeId());
        task.setAssigneeName(dto.getAssigneeName());
        task.setDeadline(dto.getDeadline());
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM");
        task.setStatus(WarningFollowupTaskStatus.PENDING.getCode());
        task.setCreatedBy(UserContextHolder.getUserId());
        task.setReminder24hSent(false);
        task.setReminder1hSent(false);
        task.setOverdueReminderSent(false);

        this.save(task);

        if (task.getAssigneeId() != null) {
            Notification notification = new Notification();
            notification.setUserId(task.getAssigneeId());
            notification.setElderlyId(task.getElderlyId());
            notification.setWarningRecordId(task.getWarningRecordId());
            notification.setTitle("新的跟进任务分配");
            notification.setContent("您有一个新的跟进任务：" + task.getTitle() + "，截止时间：" + task.getDeadline());
            notification.setNotificationType("FOLLOWUP_TASK");
            notification.setCreatedAt(LocalDateTime.now());
            notificationService.save(notification);
        }

        return task;
    }

    @Transactional
    public WarningFollowupTask updateTask(Integer id, WarningFollowupTaskUpdateDTO dto) {
        WarningFollowupTask task = this.getById(id);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        if (dto.getTitle() != null) {
            task.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }
        if (dto.getAssigneeId() != null) {
            task.setAssigneeId(dto.getAssigneeId());
        }
        if (dto.getAssigneeName() != null) {
            task.setAssigneeName(dto.getAssigneeName());
        }
        if (dto.getDeadline() != null) {
            task.setDeadline(dto.getDeadline());
            task.setReminder24hSent(false);
            task.setReminder1hSent(false);
            task.setOverdueReminderSent(false);
        }
        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        }
        if (dto.getRemark() != null) {
            task.setRemark(dto.getRemark());
        }
        if (dto.getStatus() != null) {
            WarningFollowupTaskStatus newStatus = WarningFollowupTaskStatus.requireValidCode(dto.getStatus());
            WarningFollowupTaskStatus currentStatus = WarningFollowupTaskStatus.fromCode(task.getStatus());
            if (!currentStatus.canTransitionTo(newStatus)) {
                throw new IllegalArgumentException("无法从状态 " + currentStatus.getDisplayName() + " 转换到 " + newStatus.getDisplayName());
            }
            task.setStatus(newStatus.getCode());
            if (WarningFollowupTaskStatus.COMPLETED.getCode().equals(newStatus.getCode())) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }

        this.updateById(task);
        return task;
    }

    public List<WarningFollowupTask> getByWarningRecordId(Integer warningRecordId) {
        return baseMapper.selectByWarningRecordIdWithDetails(warningRecordId);
    }

    public List<WarningFollowupTask> getMyTasks() {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        return baseMapper.selectByAssigneeIdWithDetails(userId);
    }

    public List<WarningFollowupTask> getAllTasks() {
        return baseMapper.selectAllWithDetails();
    }

    public WarningFollowupTask getDetailById(Integer id) {
        WarningFollowupTask task = baseMapper.selectByIdWithDetails(id);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        return task;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processTaskReminders() {
        LocalDateTime now = LocalDateTime.now();

        List<WarningFollowupTask> tasksFor24h = baseMapper.selectTasksFor24hReminder(now, now.plusHours(24));
        for (WarningFollowupTask task : tasksFor24h) {
            sendTaskReminder(task, "任务即将到期提醒（24小时内）",
                    "您的跟进任务 \"" + task.getTitle() + "\" 将在24小时内到期，请及时处理。");
            task.setReminder24hSent(true);
            this.updateById(task);
        }

        List<WarningFollowupTask> tasksFor1h = baseMapper.selectTasksFor1hReminder(now, now.plusHours(1));
        for (WarningFollowupTask task : tasksFor1h) {
            sendTaskReminder(task, "任务即将到期提醒（1小时内）",
                    "您的跟进任务 \"" + task.getTitle() + "\" 将在1小时内到期，请尽快处理！");
            task.setReminder1hSent(true);
            this.updateById(task);
        }

        List<WarningFollowupTask> overdueTasks = baseMapper.selectOverdueTasks(now);
        for (WarningFollowupTask task : overdueTasks) {
            task.setStatus(WarningFollowupTaskStatus.OVERDUE.getCode());
            sendTaskReminder(task, "任务已逾期提醒",
                    "您的跟进任务 \"" + task.getTitle() + "\" 已逾期，请尽快处理！");
            task.setOverdueReminderSent(true);
            this.updateById(task);
        }
    }

    private void sendTaskReminder(WarningFollowupTask task, String title, String content) {
        if (task.getAssigneeId() == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(task.getAssigneeId());
        notification.setElderlyId(task.getElderlyId());
        notification.setWarningRecordId(task.getWarningRecordId());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType("FOLLOWUP_TASK_REMINDER");
        notification.setCreatedAt(LocalDateTime.now());
        notificationService.save(notification);
    }

    public List<WarningFollowupTask> getUpcomingDeadlineTasks(Integer hours) {
        Integer userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusHours(hours);

        LambdaQueryWrapper<WarningFollowupTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningFollowupTask::getAssigneeId, userId);
        wrapper.in(WarningFollowupTask::getStatus,
                WarningFollowupTaskStatus.PENDING.getCode(),
                WarningFollowupTaskStatus.IN_PROGRESS.getCode());
        wrapper.between(WarningFollowupTask::getDeadline, now, deadline);
        wrapper.orderByAsc(WarningFollowupTask::getDeadline);

        return this.list(wrapper);
    }
}
