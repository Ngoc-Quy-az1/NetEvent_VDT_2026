package com.example.notification.adapter;

import com.example.notification.adapter.application.dto.request.ProfileTriggerRequest;
import com.example.notification.adapter.application.service.IngestionService;
import com.example.notification.adapter.domain.EventEntity;
import com.example.notification.adapter.infrastructure.EventRepository;
import com.example.notification.adapter.infrastructure.KafkaEventPublisher;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationAdapterApplicationTests {
    @Test
    void publishesOneProfileEventForANewEvent() {
        EventRepository repository = mock(EventRepository.class);
        KafkaEventPublisher publisher = mock(KafkaEventPublisher.class);
        IngestionService service = new IngestionService(repository, publisher);

        service.processKpiReport(ProfileTriggerRequest.builder().eventCode("KPI-1")
                .profileName("COVERAGE_ALERT").payload(Collections.emptyMap()).build());

        verify(repository).save(any(EventEntity.class));
        verify(publisher).publishProfileTriggeredEvent(any());
    }
}
