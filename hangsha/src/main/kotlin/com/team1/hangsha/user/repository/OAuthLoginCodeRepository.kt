package com.team1.hangsha.user.repository

import com.team1.hangsha.user.model.OAuthLoginCode
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface OAuthLoginCodeRepository : CrudRepository<OAuthLoginCode, Long> {

    @Query("""
        SELECT *
        FROM oauth_login_codes
        ORDER BY id DESC
        LIMIT :limit
    """)
    fun findRecent(limit: Int): List<OAuthLoginCode>

    @Modifying
    @Query("""
        UPDATE oauth_login_codes
        SET used_at = CURRENT_TIMESTAMP(6)
        WHERE id = :id AND used_at IS NULL
    """)
    fun markUsedIfNotUsed(id: Long): Int
}