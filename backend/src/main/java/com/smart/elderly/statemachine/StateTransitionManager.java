package com.smart.elderly.statemachine;

import com.smart.elderly.enums.HealthWarningStatus;
import com.smart.elderly.enums.NotificationStatus;
import com.smart.elderly.enums.UserSessionStatus;
import com.smart.elderly.enums.WarningFollowupTaskStatus;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class StateTransitionManager {

    public boolean canTransitionHealthWarning(HealthWarningStatus current, HealthWarningStatus target) {
        if (current == null || target == null) {
            return false;
        }
        return current.canTransitionTo(target);
    }

    public boolean canTransitionNotification(NotificationStatus current, NotificationStatus target) {
        if (current == null || target == null) {
            return false;
        }
        return current.canTransitionTo(target);
    }

    public boolean canTransitionUserSession(UserSessionStatus current, UserSessionStatus target) {
        if (current == null || target == null) {
            return false;
        }
        return current.canTransitionTo(target);
    }

    public boolean canTransitionFollowupTask(WarningFollowupTaskStatus current, WarningFollowupTaskStatus target) {
        if (current == null || target == null) {
            return false;
        }
        return current.canTransitionTo(target);
    }

    public void validateHealthWarningTransition(HealthWarningStatus current, HealthWarningStatus target) {
        if (!canTransitionHealthWarning(current, target)) {
            throw new IllegalArgumentException(
                String.format("非法状态切换：无法从 %s 切换到 %s。允许的目标状态：%s",
                    current.getDisplayName(),
                    target.getDisplayName(),
                    current.getAllowedTransitions().stream()
                        .map(HealthWarningStatus::getDisplayName)
                        .collect(Collectors.toList()))
            );
        }
    }

    public void validateNotificationTransition(NotificationStatus current, NotificationStatus target) {
        if (!canTransitionNotification(current, target)) {
            throw new IllegalArgumentException(
                String.format("非法状态切换：无法从 %s 切换到 %s",
                    current.getDisplayName(),
                    target.getDisplayName())
            );
        }
    }

    public void validateUserSessionTransition(UserSessionStatus current, UserSessionStatus target) {
        if (!canTransitionUserSession(current, target)) {
            throw new IllegalArgumentException(
                String.format("非法状态切换：无法从 %s 切换到 %s",
                    current.getDisplayName(),
                    target.getDisplayName())
            );
        }
    }

    public void validateFollowupTaskTransition(WarningFollowupTaskStatus current, WarningFollowupTaskStatus target) {
        if (!canTransitionFollowupTask(current, target)) {
            throw new IllegalArgumentException(
                String.format("非法状态切换：无法从 %s 切换到 %s。允许的目标状态：%s",
                    current.getDisplayName(),
                    target.getDisplayName(),
                    current.getAllowedTransitions().stream()
                        .map(WarningFollowupTaskStatus::getDisplayName)
                        .collect(Collectors.toList()))
            );
        }
    }
}
