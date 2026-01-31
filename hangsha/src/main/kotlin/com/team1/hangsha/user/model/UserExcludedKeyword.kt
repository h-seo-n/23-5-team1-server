package com.team1.hangsha.user.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("user_excluded_keywords")
data class UserExcludedKeyword(
    @Id
    val id: Long? = null,
    val userId: Long,
    val keyword: String,
    @CreatedDate
    val createdAt: Instant? = null,
)
