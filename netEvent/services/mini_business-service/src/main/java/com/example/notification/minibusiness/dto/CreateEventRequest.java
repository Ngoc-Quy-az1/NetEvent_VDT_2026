package com.example.notification.minibusiness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Payload used by the event creation screen.  A catalogue item may be selected
 * by ID, or supplied inline and created together with the event.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank
    private String eventCode;

    @NotBlank
    private String eventName;

    private UUID eventTypeId;

    @Valid
    private EventTypeInput eventType;

    private UUID holidayId;

    @Valid
    private HolidayInput holiday;

    private String eventLevel;
    private Boolean annual;
    private Boolean isLunar;
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventTypeInput {
        @NotBlank
        private String eventTypeCode;

        @NotBlank
        private String eventTypeName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HolidayInput {
        @NotBlank
        private String holidayCode;

        @NotBlank
        private String holidayName;
    }
}
