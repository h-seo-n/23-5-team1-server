package com.team1.hangsha.timetable.controller

import com.fasterxml.jackson.databind.JsonNode
import com.team1.hangsha.timetable.dto.AddCourseRequest
import com.team1.hangsha.timetable.dto.AddCourseResponse
import com.team1.hangsha.timetable.dto.CreateCustomCourseRequest
import com.team1.hangsha.timetable.dto.UpdateCustomCourseRequest
import com.team1.hangsha.timetable.dto.EnrollResponse
import com.team1.hangsha.timetable.dto.ListEnrollsResponse
import com.team1.hangsha.timetable.service.EnrollService
import com.team1.hangsha.user.LoggedInUser
import com.team1.hangsha.user.model.User
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema

@RestController
@RequestMapping("/api/v1/timetables/{timetableId}/enrolls")
class EnrollController(
    private val enrollService: EnrollService,
) {
    @GetMapping
    fun listEnrolls(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @PathVariable timetableId: Long,
    ): ResponseEntity<ListEnrollsResponse> {
        val res = enrollService.listEnrolls(user.id!!, timetableId)
        return ResponseEntity.ok(res)
    }

    @GetMapping("/{enrollId}")
    fun getEnroll(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @PathVariable timetableId: Long,
        @PathVariable enrollId: Long,
    ): ResponseEntity<EnrollResponse> {
        val res = enrollService.getEnroll(user.id!!, timetableId, enrollId)
        return ResponseEntity.ok(res)
    }

    @PostMapping("/custom")
    fun createCustomCourseAndEnroll(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @PathVariable timetableId: Long,
        @Valid @RequestBody req: CreateCustomCourseRequest,
    ): ResponseEntity<EnrollResponse> {
        val res = enrollService.createCustomCourseAndEnroll(user.id!!, timetableId, req)
        return ResponseEntity.status(HttpStatus.CREATED).body(res)
    }

//    // (추후) POST /api/v1/timetables/{timetableId}/enrolls  (크롤링 강의 추가)
//    @PostMapping
//    fun addCrawledCourse(
//        @Parameter(hidden = true) @LoggedInUser user: User,
//        @PathVariable timetableId: Long,
//        @RequestBody req: AddCourseRequest,
//    ): ResponseEntity<AddCourseResponse> {
//        val res = enrollService.addCrawledCourse(user.id!!, timetableId, req.courseId)
//        return ResponseEntity.status(HttpStatus.CREATED).body(res)
//    }


    @PatchMapping("/{enrollId}")
    @Operation(
        summary = "Update custom enroll (PATCH)",
        description = """
        커스텀 과목(enroll.source=CUSTOM)만 부분 수정합니다.

        ### PATCH 규칙
        - 필드 미포함: 해당 필드는 변경되지 않습니다.
        - 요청 바디에 아래 6개 중 **최소 1개 필드**는 포함되어야 합니다.
          - courseTitle, timeSlots, courseNumber, lectureNumber, credit, instructor
          - 아무것도 없으면 ENROLL_PATCH_EMPTY

        ### 필드별 정책
        - courseTitle
          - **null 금지** → COURSE_TITLE_CANNOT_BE_NULL
          - **blank 금지**(trim 후 빈 문자열) → COURSE_TITLE_CANNOT_BE_BLANK
        - timeSlots
          - **null 금지** → TIME_SLOTS_CANNOT_BE_NULL
          - **empty 배열 금지** → TIME_SLOTS_CANNOT_BE_EMPTY
          - 배열을 주면 **replace-all**(기존 슬롯 삭제 후 새 슬롯 저장)
          - 시간표 내 다른 과목과 겹치면(시간 충돌) 에러
        - courseNumber / lectureNumber / credit / instructor (optional)
          - 미포함: 변경 없음
          - null: 값 삭제(null로 저장)
          - 값 존재: 업데이트
    """
    )
    fun updateCustomEnroll(
        @Parameter(hidden = true) @LoggedInUser user: User,

        @Parameter(description = "시간표 ID", example = "12", required = true)
        @PathVariable timetableId: Long,

        @Parameter(description = "수정할 enrollId", example = "70", required = true)
        @PathVariable enrollId: Long,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "커스텀 과목 PATCH 바디(부분 수정)",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UpdateCustomCourseRequest::class),
                    examples = [
                        ExampleObject(
                            name = "updateTitleOnly",
                            summary = "courseTitle만 수정",
                            value = """{"courseTitle":"Operating Systems"}"""
                        ),
                        ExampleObject(
                            name = "replaceTimeSlots",
                            summary = "timeSlots 전체 교체(replace-all)",
                            value = """{"timeSlots":[{"dayOfWeek":"MON","startAt":"10:30","endAt":"11:45"}]}"""
                        ),
                        ExampleObject(
                            name = "clearOptionalFields",
                            summary = "optional 필드 삭제(null)",
                            value = """{"courseNumber":null,"lectureNumber":null,"instructor":null}"""
                        ),
                        ExampleObject(
                            name = "updateMixed",
                            summary = "여러 필드 동시 수정",
                            value = """{"courseTitle":"OS","credit":3,"instructor":"Kim"}"""
                        )
                    ]
                )
            ]
        )
        @RequestBody body: JsonNode,
    ): ResponseEntity<EnrollResponse> {
        val res = enrollService.updateCustomEnroll(user.id!!, timetableId, enrollId, body)
        return ResponseEntity.ok(res)
    }

    @DeleteMapping("/{enrollId}")
    fun deleteEnroll(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @PathVariable timetableId: Long,
        @PathVariable enrollId: Long,
    ): ResponseEntity<Void> {
        enrollService.deleteEnroll(user.id!!, timetableId, enrollId)
        return ResponseEntity.noContent().build()
    }
}