package com.team1.hangsha.event.controller

import com.team1.hangsha.event.service.EventSyncService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/events")
class EventSyncController(
    private val eventSyncService: EventSyncService
) {
    @PostMapping("/sync")
    fun sync() = eventSyncService.syncFromFile()
}