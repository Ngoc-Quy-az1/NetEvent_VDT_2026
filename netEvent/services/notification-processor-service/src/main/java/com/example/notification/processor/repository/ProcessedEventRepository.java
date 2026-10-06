package com.example.notification.processor.repository;
import com.example.notification.processor.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> { }
