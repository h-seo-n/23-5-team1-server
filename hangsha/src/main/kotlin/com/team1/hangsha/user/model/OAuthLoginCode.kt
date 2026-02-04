package com.team1.hangsha.user.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("oauth_login_codes")
data class OAuthLoginCode(
    @Id val id: Long? = null,

    @Column("user_id")
    val userId: Long,

    @Column("code_hash")
    val codeHash: String,

    @Column("expires_at")
    val expiresAt: Instant,

    @Column("used_at")
    val usedAt: Instant? = null,

    @CreatedDate
    val createdAt: Instant? = null
)