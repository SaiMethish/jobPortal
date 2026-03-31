package com.example.jobPortal.service;

import com.example.jobPortal.dto.response.NotificationDTO;
import com.example.jobPortal.entity.NotificationType;
import com.example.jobPortal.entity.User;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void createNotification(User user, String message, NotificationType type);

    List<NotificationDTO> getNotifications(User user);

    void markAsRead(User user, UUID notificationId);
    
    long getUnreadCount(User user);
}
