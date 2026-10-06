package com.example.notification.minibusiness.service.impl;

import com.example.notification.minibusiness.dto.BatchSessionDtos.*;
import com.example.notification.minibusiness.repository.EventSessionBatchQueryRepository;
import com.example.notification.minibusiness.service.EventSessionBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSessionBatchServiceImpl implements EventSessionBatchService {

    private final EventSessionBatchQueryRepository batchQueryRepository;

    private static final DateTimeFormatter TIMESTAMP_SUFFIX_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATE_SUFFIX_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(readOnly = true)
    public BatchSessionPreviewResponse previewBatch(BatchCreateEventSessionRequest request) {
        log.info("Previewing batch event session creation for {} items", 
                request != null && request.getSessions() != null ? request.getSessions().size() : 0);

        if (request == null || request.getSessions() == null || request.getSessions().isEmpty()) {
            return BatchSessionPreviewResponse.builder()
                    .totalRequested(0)
                    .totalValid(0)
                    .totalEstimatedCells(0)
                    .totalExistingConflicts(0)
                    .previewItems(Collections.emptyList())
                    .errors(Collections.emptyList())
                    .build();
        }

        List<PreviewSessionItem> previewItems = new ArrayList<>();
        List<BatchSessionErrorItem> errors = new ArrayList<>();
        int totalCells = 0;
        int conflicts = 0;

        for (SessionBatchItemRequest item : request.getSessions()) {
            if (item.getEventId() == null) {
                errors.add(BatchSessionErrorItem.builder()
                        .errorCode("MISSING_EVENT_ID")
                        .errorMessage("Thiếu mã định danh sự kiện (event_id)")
                        .build());
                continue;
            }

            // 1. Query Event details
            List<Map<String, Object>> eventRows = batchQueryRepository.findEventById(item.getEventId());

            if (eventRows.isEmpty()) {
                errors.add(BatchSessionErrorItem.builder()
                        .eventId(item.getEventId())
                        .errorCode("EVENT_NOT_FOUND")
                        .errorMessage("Sự kiện không tồn tại trong hệ thống (ID: " + item.getEventId() + ")")
                        .build());
                continue;
            }

            Map<String, Object> eventRow = eventRows.get(0);
            String eventCode = (String) eventRow.get("event_code");
            String eventName = (String) eventRow.get("event_name");
            String status = (String) eventRow.get("status");

            if (!"ACTIVE".equalsIgnoreCase(status)) {
                errors.add(BatchSessionErrorItem.builder()
                        .eventId(item.getEventId())
                        .eventCode(eventCode)
                        .errorCode("EVENT_NOT_ACTIVE")
                        .errorMessage("Sự kiện " + eventCode + " đang ở trạng thái " + status + ", không thể tạo phiên")
                        .build());
                continue;
            }

            // 2. Generate session_event_code
            String dateSuffix = extractDateSuffix(item.getStartDate());
            String sessionEventCode = eventCode + "_" + dateSuffix;

            // 3. Check duplicate session_event_code
            boolean isExisting = batchQueryRepository.sessionCodeExists(sessionEventCode);
            if (isExisting) {
                conflicts++;
            }

            // 4. Parse Site & count matching cells
            String siteCode = extractSiteCode(eventCode);
            List<UUID> matchingCellIds = findCellIdsBySiteCode(siteCode);
            int cellCount = matchingCellIds.size();
            totalCells += cellCount;

            String warning = null;
            if (cellCount == 0) {
                warning = "Không tìm thấy cell nào thuộc trạm " + siteCode + " (0 cell được gán)";
            }

            previewItems.add(PreviewSessionItem.builder()
                    .eventId(item.getEventId())
                    .eventCode(eventCode)
                    .eventName(eventName)
                    .siteCode(siteCode)
                    .sessionEventCode(sessionEventCode)
                    .startDate(item.getStartDate())
                    .endDate(item.getEndDate())
                    .lunarStartDate(item.getLunarStartDate())
                    .lunarEndDate(item.getLunarEndDate())
                    .expectedParticipants(item.getExpectedParticipants())
                    .estimatedCells(cellCount)
                    .isExisting(isExisting)
                    .warning(warning)
                    .isValid(!isExisting)
                    .error(isExisting ? "Mã phiên " + sessionEventCode + " đã tồn tại trong hệ thống" : null)
                    .build());
        }

        return BatchSessionPreviewResponse.builder()
                .totalRequested(request.getSessions().size())
                .totalValid(previewItems.size() - conflicts)
                .totalEstimatedCells(totalCells)
                .totalExistingConflicts(conflicts)
                .previewItems(previewItems)
                .errors(errors)
                .build();
    }

    @Override
    @Transactional
    public BatchCreateEventSessionResponse createBatch(BatchCreateEventSessionRequest request) {
        log.info("Processing batch event session creation for {} items", 
                request != null && request.getSessions() != null ? request.getSessions().size() : 0);

        if (request == null || request.getSessions() == null || request.getSessions().isEmpty()) {
            return BatchCreateEventSessionResponse.builder()
                    .totalRequested(0)
                    .totalCreated(0)
                    .totalCellsMapped(0)
                    .createdSessions(Collections.emptyList())
                    .errors(Collections.emptyList())
                    .build();
        }

        List<CreatedSessionInfo> createdSessions = new ArrayList<>();
        List<BatchSessionErrorItem> errors = new ArrayList<>();
        int totalCellsMapped = 0;

        List<Object[]> sessionInsertBatch = new ArrayList<>();
        List<Object[]> cellSessionInsertBatch = new ArrayList<>();

        for (SessionBatchItemRequest item : request.getSessions()) {
            if (item.getEventId() == null) {
                errors.add(BatchSessionErrorItem.builder()
                        .errorCode("MISSING_EVENT_ID")
                        .errorMessage("Thiếu mã định danh sự kiện (event_id)")
                        .build());
                continue;
            }

            // 1. Query Event details
            List<Map<String, Object>> eventRows = batchQueryRepository.findEventById(item.getEventId());

            if (eventRows.isEmpty()) {
                errors.add(BatchSessionErrorItem.builder()
                        .eventId(item.getEventId())
                        .errorCode("EVENT_NOT_FOUND")
                        .errorMessage("Sự kiện không tồn tại trong hệ thống (ID: " + item.getEventId() + ")")
                        .build());
                continue;
            }

            Map<String, Object> eventRow = eventRows.get(0);
            String eventCode = (String) eventRow.get("event_code");
            String eventName = (String) eventRow.get("event_name");
            String status = (String) eventRow.get("status");

            if (!"ACTIVE".equalsIgnoreCase(status)) {
                errors.add(BatchSessionErrorItem.builder()
                        .eventId(item.getEventId())
                        .eventCode(eventCode)
                        .errorCode("EVENT_NOT_ACTIVE")
                        .errorMessage("Sự kiện " + eventCode + " không ở trạng thái ACTIVE (hiện tại: " + status + ")")
                        .build());
                continue;
            }

            // 2. Generate session_event_code
            String dateSuffix = extractDateSuffix(item.getStartDate());
            String sessionEventCode = eventCode + "_" + dateSuffix;

            // 3. Check duplicate session_event_code
            if (batchQueryRepository.sessionCodeExists(sessionEventCode)) {
                errors.add(BatchSessionErrorItem.builder()
                        .eventId(item.getEventId())
                        .eventCode(eventCode)
                        .errorCode("DUPLICATE_SESSION_CODE")
                        .errorMessage("Mã phiên " + sessionEventCode + " đã tồn tại, không thể tạo đè")
                        .build());
                continue;
            }

            // 4. Parse Site & query matching cell IDs
            String siteCode = extractSiteCode(eventCode);
            List<UUID> matchingCellIds = findCellIdsBySiteCode(siteCode);
            int cellCount = matchingCellIds.size();

            String warning = null;
            if (cellCount == 0) {
                warning = "0 cell được gán (không tìm thấy cell thuộc trạm " + siteCode + ")";
            }

            UUID sessionId = UUID.randomUUID();
            Timestamp startTs = parseTimestamp(item.getStartDate(), false);
            Timestamp endTs = parseTimestamp(item.getEndDate(), true);
            Timestamp lunarStartTs = parseTimestamp(item.getLunarStartDate(), false);
            Timestamp lunarEndTs = parseTimestamp(item.getLunarEndDate(), true);

            sessionInsertBatch.add(new Object[]{
                    sessionId,
                    sessionEventCode,
                    item.getEventId(),
                    startTs,
                    endTs,
                    lunarStartTs,
                    lunarEndTs,
                    item.getExpectedParticipants() != null ? item.getExpectedParticipants() : 0
            });

            for (UUID cellId : matchingCellIds) {
                cellSessionInsertBatch.add(new Object[]{
                        UUID.randomUUID(),
                        cellId,
                        sessionId
                });
            }

            totalCellsMapped += cellCount;
            createdSessions.add(CreatedSessionInfo.builder()
                    .sessionId(sessionId)
                    .sessionEventCode(sessionEventCode)
                    .eventId(item.getEventId())
                    .eventCode(eventCode)
                    .eventName(eventName)
                    .siteCode(siteCode)
                    .cellsMapped(cellCount)
                    .warning(warning)
                    .build());
        }

        // 5. Bulk insert into event_session
        if (!sessionInsertBatch.isEmpty()) {
            batchQueryRepository.insertSessions(sessionInsertBatch);
            log.info("Successfully bulk inserted {} event_session records", sessionInsertBatch.size());
        }

        // 6. Bulk insert into cell_session
        if (!cellSessionInsertBatch.isEmpty()) {
            batchQueryRepository.insertCellSessions(cellSessionInsertBatch);
            log.info("Successfully bulk inserted {} cell_session records", cellSessionInsertBatch.size());
        }

        return BatchCreateEventSessionResponse.builder()
                .totalRequested(request.getSessions().size())
                .totalCreated(createdSessions.size())
                .totalCellsMapped(totalCellsMapped)
                .createdSessions(createdSessions)
                .errors(errors)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTypeResponse> getAllEventTypes() {
        return batchQueryRepository.findEventTypes().stream()
                .map(row -> EventTypeResponse.builder()
                        .eventTypeId(UUID.fromString(row.get("event_type_id").toString()))
                        .eventTypeCode((String) row.get("event_type_code"))
                        .eventTypeName((String) row.get("event_type_name"))
                        .build()
                ).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayOccasionResponse> getAllHolidays() {
        return batchQueryRepository.findHolidays().stream()
                .map(row -> HolidayOccasionResponse.builder()
                        .holidayId(UUID.fromString(row.get("holiday_id").toString()))
                        .holidayCode((String) row.get("holiday_code"))
                        .holidayName((String) row.get("holiday_name"))
                        .build()
                ).collect(java.util.stream.Collectors.toList());
    }

    // --- Helper Methods ---

    /**
     * Parse mã site từ event_code theo quy ước <khu_vực>_<mã_site>_<số_thứ_tự>
     * Ví dụ:
     *   KV3_HCM_HCM138_002 -> HCM138
     *   KV3_B650_B650001_003 -> B650001
     *   KV3_T008_T00801_019 -> T00801
     *   KV3_VLG_000000_005 -> 000000
     */
    private String extractSiteCode(String eventCode) {
        if (eventCode == null || eventCode.trim().isEmpty()) return "";
        String[] parts = eventCode.trim().split("_");
        if (parts.length >= 3) {
            // Phần tử kế cuối là mã site
            return parts[parts.length - 2];
        } else if (parts.length == 2) {
            return parts[0];
        }
        return eventCode.trim();
    }

    private List<UUID> findCellIdsBySiteCode(String siteCode) {
        return batchQueryRepository.findCellIdsBySiteCode(siteCode);
    }

    private String extractDateSuffix(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDateTime.now(ZoneOffset.ofHours(7)).format(TIMESTAMP_SUFFIX_FORMAT);
        }

        try {
            String clean = dateStr.trim().replace(" ", "T");
            if (clean.contains("T")) {
                String[] parts = clean.split("T");
                String datePart = parts[0].replace("-", "");
                String timePart = parts.length > 1 ? parts[1].replace(":", "") : "000000";
                if (timePart.contains("+")) timePart = timePart.substring(0, timePart.indexOf("+"));
                if (timePart.contains("-")) timePart = timePart.substring(0, timePart.indexOf("-"));
                if (timePart.contains(".")) timePart = timePart.substring(0, timePart.indexOf("."));
                if (timePart.contains("Z")) timePart = timePart.replace("Z", "");

                while (timePart.length() < 6) {
                    timePart += "0";
                }
                if (timePart.length() > 6) {
                    timePart = timePart.substring(0, 6);
                }
                return datePart + timePart;
            } else if (clean.length() >= 10) {
                String ymd = clean.substring(0, 10).replace("-", "");
                return ymd + "000000";
            }
            return LocalDateTime.now(ZoneOffset.ofHours(7)).format(TIMESTAMP_SUFFIX_FORMAT);
        } catch (Exception e) {
            log.warn("Could not parse date string '{}', fallback to now: {}", dateStr, e.getMessage());
            return LocalDateTime.now(ZoneOffset.ofHours(7)).format(TIMESTAMP_SUFFIX_FORMAT);
        }
    }

    private Timestamp parseTimestamp(String dateStr, boolean isEndOfDay) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            String clean = dateStr.trim();
            if (clean.contains("T") || clean.contains(" ")) {
                String formatted = clean.replace(" ", "T");
                if (formatted.endsWith("Z")) {
                    return Timestamp.from(Instant.parse(formatted));
                }
                if (formatted.contains("+") || (formatted.lastIndexOf('-') > formatted.indexOf('T'))) {
                    return Timestamp.from(OffsetDateTime.parse(formatted).toInstant());
                }
                if (formatted.length() == 16) {
                    formatted += ":00";
                }
                LocalDateTime ldt = LocalDateTime.parse(formatted);
                return Timestamp.from(ldt.atZone(ZoneOffset.ofHours(7)).toInstant());
            } else {
                LocalDate date = LocalDate.parse(clean);
                if (isEndOfDay) {
                    return Timestamp.from(date.atTime(23, 59, 59).atOffset(ZoneOffset.ofHours(7)).toInstant());
                } else {
                    return Timestamp.from(date.atStartOfDay().atOffset(ZoneOffset.ofHours(7)).toInstant());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse date '{}': {}", dateStr, e.getMessage());
            return Timestamp.from(Instant.now());
        }
    }
}
