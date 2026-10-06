package com.example.notification.minibusiness.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BatchSessionDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionBatchItemRequest {
        @JsonProperty("event_id")
        @JsonAlias({"eventId"})
        private UUID eventId;

        @JsonProperty("start_date")
        @JsonAlias({"startDate"})
        private String startDate;

        @JsonProperty("end_date")
        @JsonAlias({"endDate"})
        private String endDate;

        @JsonProperty("lunar_start_date")
        @JsonAlias({"lunarStartDate"})
        private String lunarStartDate;

        @JsonProperty("lunar_end_date")
        @JsonAlias({"lunarEndDate"})
        private String lunarEndDate;

        @JsonProperty("expected_participants")
        @JsonAlias({"expectedParticipants"})
        private Integer expectedParticipants;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchCreateEventSessionRequest {
        @Builder.Default
        private List<SessionBatchItemRequest> sessions = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatedSessionInfo {
        private UUID sessionId;
        private String sessionEventCode;
        private UUID eventId;
        private String eventCode;
        private String eventName;
        private String siteCode;
        private int cellsMapped;
        private String warning;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchSessionErrorItem {
        private UUID eventId;
        private String eventCode;
        private String errorCode;
        private String errorMessage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchCreateEventSessionResponse {
        private int totalRequested;
        private int totalCreated;
        private int totalCellsMapped;
        @Builder.Default
        private List<CreatedSessionInfo> createdSessions = new ArrayList<>();
        @Builder.Default
        private List<BatchSessionErrorItem> errors = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreviewSessionItem {
        private UUID eventId;
        private String eventCode;
        private String eventName;
        private String siteCode;
        private String sessionEventCode;
        private String startDate;
        private String endDate;
        private String lunarStartDate;
        private String lunarEndDate;
        private Integer expectedParticipants;
        private int estimatedCells;
        private boolean isExisting;
        private String warning;
        private boolean isValid;
        private String error;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchSessionPreviewResponse {
        private int totalRequested;
        private int totalValid;
        private int totalEstimatedCells;
        private int totalExistingConflicts;
        @Builder.Default
        private List<PreviewSessionItem> previewItems = new ArrayList<>();
        @Builder.Default
        private List<BatchSessionErrorItem> errors = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventTypeResponse {
        private UUID eventTypeId;
        private String eventTypeCode;
        private String eventTypeName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HolidayOccasionResponse {
        private UUID holidayId;
        private String holidayCode;
        private String holidayName;
    }
}
