package com.example.notification.minibusiness.service;

import com.example.notification.minibusiness.dto.BatchSessionDtos.*;

import java.util.List;

public interface EventSessionBatchService {

    BatchSessionPreviewResponse previewBatch(BatchCreateEventSessionRequest request);

    BatchCreateEventSessionResponse createBatch(BatchCreateEventSessionRequest request);

    List<EventTypeResponse> getAllEventTypes();

    List<HolidayOccasionResponse> getAllHolidays();
}
