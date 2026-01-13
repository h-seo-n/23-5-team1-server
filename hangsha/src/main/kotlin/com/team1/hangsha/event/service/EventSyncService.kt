package com.team1.hangsha.event.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.team1.hangsha.category.repository.CategoryGroupRepository
import com.team1.hangsha.category.repository.CategoryRepository
import com.team1.hangsha.event.model.Event
import com.team1.hangsha.event.repository.EventRepository
import com.team1.hangsha.event.dto.core.CrawledDetailSession
import com.team1.hangsha.event.dto.core.CrawledProgramEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class EventSyncService(
    private val objectMapper: ObjectMapper,
    private val eventRepository: EventRepository,
    private val categoryGroupRepository: CategoryGroupRepository,
    private val categoryRepository: CategoryRepository,
    @Value("\${crawler.events-json-path}") private val eventsJsonPath: String,
) {

    data class SyncResult(val total: Int, val upserted: Int, val skipped: Int)

    @Transactional
    fun syncFromFile(): SyncResult = syncFromFile(Path.of(eventsJsonPath))

    @Transactional
    fun syncFromFile(path: Path): SyncResult {
        require(Files.exists(path)) { "events.json not found: $path" }

        val events: List<CrawledProgramEvent> =
            objectMapper.readValue(
                Files.readString(path),
                objectMapper.typeFactory.constructCollectionType(List::class.java, CrawledProgramEvent::class.java)
            )

        val statusGroupId = requireGroupId("모집현황")
        val typeGroupId = requireGroupId("프로그램 유형")

        // 혹시 몰라서, sync 결과 추적을 위한 변수들
        var upserted = 0
        var skipped = 0

        for (e in events) {
            val applyLink = "https://extra.snu.ac.kr/ptfol/pgm/view.do?dataSeq=${e.dataSeq}"

            val orgName = e.majorTypes.getOrNull(0)?.trim()?.takeIf { it.isNotBlank() }
            val typeName = e.majorTypes.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }

            val statusId = e.status?.trim()?.takeIf { it.isNotBlank() }?.let { findCategoryId(statusGroupId, it) }
            val eventTypeId = typeName?.let { findCategoryId(typeGroupId, it) }

            val applyStart = e.applyStart?.let { dateStart(it) }
            val applyEnd = e.applyEnd?.let { dateEnd(it) }

            // 상세 디테일에서 와야 하니까, 따로 함수 설정
            val (eventStart, eventEnd, location) = deriveEventPeriodAndLocation(e)

            // 중복 여부 판정: 신청 링크
            val existing = eventRepository.findByApplyLink(applyLink)

            val model = Event(
                id = existing?.id,
                title = e.title!!.trim(),
                imageUrl = e.imageUrl?.trim(),
                operationMode = e.operationMode?.trim(),

                statusId = statusId,
                eventTypeId = eventTypeId,
                orgId = null,                 // ✅ org seed 없으니 일단 NULL
                // @TODO: 기관 ID 없으면 여기서 등록해야 할수도..?
                applyStart = applyStart,
                applyEnd = applyEnd,
                eventStart = eventStart,
                eventEnd = eventEnd,

                capacity = e.capacity ?: 0,
                applyCount = e.applyCount ?: 0,

                organization = orgName,        // ✅ org는 문자열로만 저장
                location = location,
                applyLink = applyLink
            )

            // 값이 같아도 save() → DB updated_at 갱신
            eventRepository.save(model)
            upserted++
        }

        return SyncResult(total = events.size, upserted = upserted, skipped = skipped)
    }

    private fun requireGroupId(name: String): Long =
        categoryGroupRepository.findByName(name)?.id ?: error("Missing category_group: $name")

    private fun findCategoryId(groupId: Long, name: String): Long? =
        categoryRepository.findByGroupIdAndName(groupId, name)?.id

    private fun deriveEventPeriodAndLocation(e: CrawledProgramEvent): Triple<LocalDateTime?, LocalDateTime?, String?> {
        val sessions = e.detailSessions
        if (sessions.isNotEmpty()) {
            val starts = sessions.mapNotNull { parseSessionStart(it) }
            val ends = sessions.mapNotNull { parseSessionEnd(it) }
            val start = starts.minOrNull()
            val end = ends.maxOrNull()
            val location = sessions.firstNotNullOfOrNull { it.location?.trim()?.takeIf { s -> s.isNotBlank() } }
            return Triple(start, end, location)
        }

        // fallback: activityStart/End (날짜만)
        val start = e.activityStart?.let { LocalDate.parse(it).atStartOfDay() }
        val end = e.activityEnd?.let { LocalDate.parse(it).atTime(23, 59, 59) }
        return Triple(start, end, null)
    }

    private fun parseSessionStart(s: CrawledDetailSession): LocalDateTime? {
        val d = s.startDate ?: return null
        val date = LocalDate.parse(d)
        val time = s.startTime?.let { LocalTime.parse(it) } ?: LocalTime.MIDNIGHT
        return date.atTime(time)
    }

    private fun parseSessionEnd(s: CrawledDetailSession): LocalDateTime? {
        val d = s.endDate ?: s.startDate ?: return null
        val date = LocalDate.parse(d)
        val time = s.endTime?.let { LocalTime.parse(it) } ?: LocalTime.of(23, 59, 59)
        return date.atTime(time)
    }

    private fun dateStart(ymd: String): LocalDateTime =
        LocalDate.parse(ymd).atStartOfDay()

    private fun dateEnd(ymd: String): LocalDateTime =
        LocalDate.parse(ymd).atTime(23, 59, 59)
}
