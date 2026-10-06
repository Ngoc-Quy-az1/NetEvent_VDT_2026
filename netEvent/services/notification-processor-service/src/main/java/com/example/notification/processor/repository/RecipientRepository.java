package com.example.notification.processor.repository;
import com.example.notification.processor.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface RecipientRepository extends JpaRepository<Recipient, UUID> { List<Recipient> findByAccountIdAndStatus(UUID accountId, String status); }
