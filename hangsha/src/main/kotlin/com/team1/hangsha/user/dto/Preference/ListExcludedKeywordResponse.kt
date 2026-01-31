package com.team1.hangsha.user.dto.Preference

import java.time.Instant

data class ListExcludedKeywordResponse(
    val items: List<Item>,
) {
    data class Item(
        val id: Long,
        val keyword: String,
        val createdAt: Instant,
    )
}