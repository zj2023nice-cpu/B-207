package com.smart.elderly.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.smart.elderly.entity.HealthWarningRecord;
import com.smart.elderly.entity.HealthWarningTimeline;
import com.smart.elderly.enums.HealthWarningStatus;
import com.smart.elderly.mapper.HealthWarningRecordMapper;
import com.smart.elderly.mapper.HealthWarningTimelineMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HealthWarningRecordService 预警记录服务测试")
class HealthWarningRecordServiceTest {

    @Mock
    private HealthWarningRecordMapper baseMapper;

    @Mock
    private HealthWarningThresholdService thresholdService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HealthWarningTimelineMapper timelineMapper;

    @Spy
    @InjectMocks
    private HealthWarningRecordService warningRecordService;

    private static final Integer WARNING_ID = 1;
    private static final Integer ELDERLY_ID = 100;
    private static final String OPERATOR = "admin";
    private static final String REMARK = "测试备注";

    private HealthWarningRecord createWarningRecord(HealthWarningStatus status) {
        HealthWarningRecord record = new HealthWarningRecord();
        record.setId(WARNING_ID);
        record.setElderlyId(ELDERLY_ID);
        record.setHealthRecordId(200);
        record.setIndicatorType("temperature");
        record.setActualValue(new BigDecimal("38.5"));
        record.setThresholdValue(new BigDecimal("37.3"));
        record.setWarningLevel("HIGH");
        record.setWarningMessage("体温异常");
        record.setStatus(status.getCode());
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }

    @BeforeEach
    void setUp() {
        doReturn(true).when(warningRecordService).updateById(any(HealthWarningRecord.class));
        doReturn(true).when(warningRecordService).save(any(HealthWarningRecord.class));
    }

    @Test
    @DisplayName("createWarning - 创建预警初始状态为PENDING")
    void testCreateWarning_InitialStatus_ShouldBePending() {
        HealthWarningRecord result = warningRecordService.createWarning(
                ELDERLY_ID, 200, "temperature",
                new BigDecimal("38.5"), new BigDecimal("37.3"),
                "HIGH", "体温异常");

        assertNotNull(result);
        assertEquals(HealthWarningStatus.PENDING.getCode(), result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    @DisplayName("createWarning - 创建预警后记录时间线")
    void testCreateWarning_ShouldAddTimeline() {
        warningRecordService.createWarning(
                ELDERLY_ID, 200, "temperature",
                new BigDecimal("38.5"), new BigDecimal("37.3"),
                "HIGH", "体温异常");

        verify(timelineMapper).insert(argThat(timeline ->
                "CREATE".equals(timeline.getActionType()) &&
                HealthWarningStatus.PENDING.getCode().equals(timeline.getToStatus())
        ));
    }

    @Test
    @DisplayName("createWarning - 创建预警后发送通知")
    void testCreateWarning_ShouldSendNotification() {
        warningRecordService.createWarning(
                ELDERLY_ID, 200, "temperature",
                new BigDecimal("38.5"), new BigDecimal("37.3"),
                "HIGH", "体温异常");

        verify(notificationService).createWarningNotification(any(HealthWarningRecord.class));
    }

    @Test
    @DisplayName("markAsRead - PENDING状态可以标记为已读")
    void testMarkAsRead_FromPending_ShouldSucceed() {
        HealthWarningRecord pendingRecord = createWarningRecord(HealthWarningStatus.PENDING);
        doReturn(pendingRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.markAsRead(WARNING_ID);

        assertTrue(result);
        assertEquals(HealthWarningStatus.READ.getCode(), pendingRecord.getStatus());
    }

    @Test
    @DisplayName("markAsRead - READ状态再次标记为已读返回true（幂等）")
    void testMarkAsRead_AlreadyRead_ShouldReturnTrue() {
        HealthWarningRecord readRecord = createWarningRecord(HealthWarningStatus.READ);
        doReturn(readRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.markAsRead(WARNING_ID);

        assertTrue(result);
        verify(warningRecordService, never()).updateById(any());
    }

    @Test
    @DisplayName("handleWarning - PENDING状态可以处理")
    void testHandleWarning_FromPending_ShouldSucceed() {
        HealthWarningRecord pendingRecord = createWarningRecord(HealthWarningStatus.PENDING);
        doReturn(pendingRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.handleWarning(WARNING_ID, OPERATOR, REMARK);

        assertTrue(result);
        assertEquals(HealthWarningStatus.HANDLED.getCode(), pendingRecord.getStatus());
        assertEquals(OPERATOR, pendingRecord.getHandledBy());
        assertEquals(REMARK, pendingRecord.getHandleRemark());
        assertNotNull(pendingRecord.getHandledAt());
    }

    @Test
    @DisplayName("handleWarning - READ状态可以处理")
    void testHandleWarning_FromRead_ShouldSucceed() {
        HealthWarningRecord readRecord = createWarningRecord(HealthWarningStatus.READ);
        doReturn(readRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.handleWarning(WARNING_ID, OPERATOR, REMARK);

        assertTrue(result);
        assertEquals(HealthWarningStatus.HANDLED.getCode(), readRecord.getStatus());
    }

    @Test
    @DisplayName("handleWarning - HANDLED状态重复处理返回true（幂等）")
    void testHandleWarning_AlreadyHandled_ShouldReturnTrue() {
        HealthWarningRecord handledRecord = createWarningRecord(HealthWarningStatus.HANDLED);
        doReturn(handledRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.handleWarning(WARNING_ID, OPERATOR, REMARK);

        assertTrue(result);
        verify(warningRecordService, never()).updateById(any());
    }

    @Test
    @DisplayName("handleWarning - 非法重复处理：HANDLED不能直接到READ")
    void testHandleWarning_IllegalTransition_FromHandledToRead_ShouldThrow() {
        HealthWarningRecord handledRecord = createWarningRecord(HealthWarningStatus.HANDLED);
        doReturn(handledRecord).when(warningRecordService).getById(WARNING_ID);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> warningRecordService.transitionStatus(WARNING_ID, HealthWarningStatus.READ, OPERATOR, REMARK));

        assertTrue(exception.getMessage().contains("非法状态切换"));
    }

    @Test
    @DisplayName("ignoreWarning - PENDING状态可以忽略")
    void testIgnoreWarning_FromPending_ShouldSucceed() {
        HealthWarningRecord pendingRecord = createWarningRecord(HealthWarningStatus.PENDING);
        doReturn(pendingRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.ignoreWarning(WARNING_ID, OPERATOR, REMARK);

        assertTrue(result);
        assertEquals(HealthWarningStatus.IGNORED.getCode(), pendingRecord.getStatus());
    }

    @Test
    @DisplayName("ignoreWarning - 操作人不能为空")
    void testIgnoreWarning_NullOperator_ShouldThrow() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> warningRecordService.ignoreWarning(WARNING_ID, null, REMARK));

        assertEquals("操作人不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("ignoreWarning - 操作人空白字符串应抛出异常")
    void testIgnoreWarning_EmptyOperator_ShouldThrow() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> warningRecordService.ignoreWarning(WARNING_ID, "   ", REMARK));

        assertEquals("操作人不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("reopenWarning - HANDLED状态可以重新打开")
    void testReopenWarning_FromHandled_ShouldSucceed() {
        HealthWarningRecord handledRecord = createWarningRecord(HealthWarningStatus.HANDLED);
        doReturn(handledRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.reopenWarning(WARNING_ID, OPERATOR, REMARK);

        assertTrue(result);
        assertEquals(HealthWarningStatus.REOPENED.getCode(), handledRecord.getStatus());
    }

    @Test
    @DisplayName("reopenWarning - IGNORED状态可以重新打开")
    void testReopenWarning_FromIgnored_ShouldSucceed() {
        HealthWarningRecord ignoredRecord = createWarningRecord(HealthWarningStatus.IGNORED);
        doReturn(ignoredRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.reopenWarning(WARNING_ID, OPERATOR, REMARK);

        assertTrue(result);
        assertEquals(HealthWarningStatus.REOPENED.getCode(), ignoredRecord.getStatus());
    }

    @Test
    @DisplayName("escalateWarning - PENDING状态可以升级")
    void testEscalateWarning_FromPending_ShouldSucceed() {
        HealthWarningRecord pendingRecord = createWarningRecord(HealthWarningStatus.PENDING);
        doReturn(pendingRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.escalateWarning(WARNING_ID, OPERATOR, REMARK);

        assertTrue(result);
        assertEquals(HealthWarningStatus.ESCALATED.getCode(), pendingRecord.getStatus());
    }

    @Test
    @DisplayName("escalateWarning - READ状态可以升级")
    void testEscalateWarning_FromRead_ShouldSucceed() {
        HealthWarningRecord readRecord = createWarningRecord(HealthWarningStatus.READ);
        doReturn(readRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.escalateWarning(WARNING_ID, OPERATOR, REMARK);

        assertTrue(result);
        assertEquals(HealthWarningStatus.ESCALATED.getCode(), readRecord.getStatus());
    }

    @Test
    @DisplayName("transitionStatus - 预警ID为null时抛出异常")
    void testTransitionStatus_NullId_ShouldThrow() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> warningRecordService.transitionStatus(null, HealthWarningStatus.READ, OPERATOR, REMARK));

        assertEquals("预警记录ID不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("transitionStatus - 目标状态为null时抛出异常")
    void testTransitionStatus_NullTargetStatus_ShouldThrow() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> warningRecordService.transitionStatus(WARNING_ID, null, OPERATOR, REMARK));

        assertEquals("目标状态不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("transitionStatus - 预警记录不存在时抛出异常")
    void testTransitionStatus_RecordNotFound_ShouldThrow() {
        doReturn(null).when(warningRecordService).getById(WARNING_ID);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> warningRecordService.transitionStatus(WARNING_ID, HealthWarningStatus.READ, OPERATOR, REMARK));

        assertTrue(exception.getMessage().contains("预警记录不存在"));
    }

    @Test
    @DisplayName("transitionStatus - 非法状态转换抛出异常")
    void testTransitionStatus_IllegalTransition_ShouldThrow() {
        HealthWarningRecord handledRecord = createWarningRecord(HealthWarningStatus.HANDLED);
        doReturn(handledRecord).when(warningRecordService).getById(WARNING_ID);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> warningRecordService.transitionStatus(WARNING_ID, HealthWarningStatus.READ, OPERATOR, REMARK));

        assertTrue(exception.getMessage().contains("非法状态切换"));
        assertTrue(exception.getMessage().contains(HealthWarningStatus.HANDLED.getDisplayName()));
        assertTrue(exception.getMessage().contains(HealthWarningStatus.READ.getDisplayName()));
    }

    @Test
    @DisplayName("状态流转完整路径 - PENDING -> READ -> HANDLED -> REOPENED -> HANDLED")
    void testFullTransitionPath_ShouldSucceed() {
        HealthWarningRecord record = createWarningRecord(HealthWarningStatus.PENDING);
        doReturn(record).when(warningRecordService).getById(WARNING_ID);

        assertTrue(warningRecordService.markAsRead(WARNING_ID));
        assertEquals(HealthWarningStatus.READ.getCode(), record.getStatus());

        assertTrue(warningRecordService.handleWarning(WARNING_ID, OPERATOR, REMARK));
        assertEquals(HealthWarningStatus.HANDLED.getCode(), record.getStatus());

        assertTrue(warningRecordService.reopenWarning(WARNING_ID, OPERATOR, REMARK));
        assertEquals(HealthWarningStatus.REOPENED.getCode(), record.getStatus());

        assertTrue(warningRecordService.handleWarning(WARNING_ID, OPERATOR, REMARK));
        assertEquals(HealthWarningStatus.HANDLED.getCode(), record.getStatus());

        verify(timelineMapper, times(4)).insert(any(HealthWarningTimeline.class));
    }

    @Test
    @DisplayName("非法流转 - HANDLED不能直接到PENDING")
    void testIllegalTransition_HandledToPending_ShouldThrow() {
        HealthWarningRecord handledRecord = createWarningRecord(HealthWarningStatus.HANDLED);
        doReturn(handledRecord).when(warningRecordService).getById(WARNING_ID);

        assertThrows(IllegalArgumentException.class,
                () -> warningRecordService.transitionStatus(WARNING_ID, HealthWarningStatus.PENDING, OPERATOR, REMARK));
    }

    @Test
    @DisplayName("非法流转 - IGNORED不能直接到HANDLED（必须先REOPENED）")
    void testIllegalTransition_IgnoredToHandled_ShouldThrow() {
        HealthWarningRecord ignoredRecord = createWarningRecord(HealthWarningStatus.IGNORED);
        doReturn(ignoredRecord).when(warningRecordService).getById(WARNING_ID);

        assertThrows(IllegalArgumentException.class,
                () -> warningRecordService.transitionStatus(WARNING_ID, HealthWarningStatus.HANDLED, OPERATOR, REMARK));
    }

    @Test
    @DisplayName("状态流转 - 每次转换都记录时间线")
    void testTransitionStatus_ShouldRecordTimeline() {
        HealthWarningRecord pendingRecord = createWarningRecord(HealthWarningStatus.PENDING);
        doReturn(pendingRecord).when(warningRecordService).getById(WARNING_ID);

        warningRecordService.markAsRead(WARNING_ID);

        verify(timelineMapper).insert(argThat(timeline ->
                "READ".equals(timeline.getActionType()) &&
                HealthWarningStatus.PENDING.getCode().equals(timeline.getFromStatus()) &&
                HealthWarningStatus.READ.getCode().equals(timeline.getToStatus())
        ));
    }

    @Test
    @DisplayName("handleWarning - 处理后设置处理时间和处理人")
    void testHandleWarning_ShouldSetHandledFields() {
        HealthWarningRecord pendingRecord = createWarningRecord(HealthWarningStatus.PENDING);
        doReturn(pendingRecord).when(warningRecordService).getById(WARNING_ID);

        assertNull(pendingRecord.getHandledAt());
        assertNull(pendingRecord.getHandledBy());
        assertNull(pendingRecord.getHandleRemark());

        warningRecordService.handleWarning(WARNING_ID, OPERATOR, REMARK);

        assertNotNull(pendingRecord.getHandledAt());
        assertEquals(OPERATOR, pendingRecord.getHandledBy());
        assertEquals(REMARK, pendingRecord.getHandleRemark());
    }

    @Test
    @DisplayName("markAsRead - 标记已读不需要操作人")
    void testMarkAsRead_NoOperatorRequired_ShouldSucceed() {
        HealthWarningRecord pendingRecord = createWarningRecord(HealthWarningStatus.PENDING);
        doReturn(pendingRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.markAsRead(WARNING_ID);

        assertTrue(result);
        assertEquals(HealthWarningStatus.READ.getCode(), pendingRecord.getStatus());
    }

    @Test
    @DisplayName("REOPENED状态可以继续流转到READ")
    void testTransition_ReopenedToRead_ShouldSucceed() {
        HealthWarningRecord reopenedRecord = createWarningRecord(HealthWarningStatus.REOPENED);
        doReturn(reopenedRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.transitionStatus(WARNING_ID, HealthWarningStatus.READ, "system", "标记已读");

        assertTrue(result);
        assertEquals(HealthWarningStatus.READ.getCode(), reopenedRecord.getStatus());
    }

    @Test
    @DisplayName("ESCALATED状态可以流转到HANDLED")
    void testTransition_EscalatedToHandled_ShouldSucceed() {
        HealthWarningRecord escalatedRecord = createWarningRecord(HealthWarningStatus.ESCALATED);
        doReturn(escalatedRecord).when(warningRecordService).getById(WARNING_ID);

        boolean result = warningRecordService.handleWarning(WARNING_ID, OPERATOR, REMARK);

        assertTrue(result);
        assertEquals(HealthWarningStatus.HANDLED.getCode(), escalatedRecord.getStatus());
    }
}
