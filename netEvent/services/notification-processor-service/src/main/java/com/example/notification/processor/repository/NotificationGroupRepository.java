package com.example.notification.processor.repository;
import com.example.notification.processor.entity.NotificationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface NotificationGroupRepository extends JpaRepository<NotificationGroup, UUID> { }
