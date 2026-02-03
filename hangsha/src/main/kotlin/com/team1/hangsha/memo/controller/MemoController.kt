package com.team1.hangsha.memo.controller

import com.team1.hangsha.memo.dto.*
import com.team1.hangsha.memo.dto.core.MemoResponse
import com.team1.hangsha.memo.service.MemoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/memos")
class MemoController(
    private val memoService: MemoService
) {

    @PostMapping
    fun createMemo(
        @RequestParam userId: Long,
        @RequestBody req: CreateMemoRequest
    ): ResponseEntity<MemoResponse> {
        val response = memoService.createMemo(userId, req)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getMyMemos(
        @RequestParam userId: Long
    ): ResponseEntity<ListMemoResponse> {
        val memos = memoService.getMyMemos(userId)
        return ResponseEntity.ok(ListMemoResponse(memos))
    }

    @PatchMapping("/{memoId}")
    fun updateMemo(
        @RequestParam userId: Long,
        @PathVariable memoId: Long,
        @RequestBody req: UpdateMemoRequest
    ): ResponseEntity<MemoResponse> {
        val response = memoService.updateMemo(userId, memoId, req)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{memoId}")
    fun deleteMemo(
        @RequestParam userId: Long,
        @PathVariable memoId: Long
    ): ResponseEntity<Unit> {
        memoService.deleteMemo(userId, memoId)
        return ResponseEntity.noContent().build()
    }
}