package com.team1.hangsha.user.repository

import com.team1.hangsha.user.model.UserExcludedKeyword
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface UserExcludedKeywordRepository : CrudRepository<UserExcludedKeyword, Long> {

    @Query(
        """
        select *
        from user_excluded_keywords
        where user_id = :userId
        order by created_at desc, id desc
        """
    )
    fun findAllByUserIdOrderByCreatedAtDescIdDesc(
        @Param("userId") userId: Long
    ): List<UserExcludedKeyword>

    @Query(
        """
        select count(*)
        from user_excluded_keywords
        where user_id = :userId and keyword = :keyword
        """
    )
    fun countByUserIdAndKeyword(
        @Param("userId") userId: Long,
        @Param("keyword") keyword: String
    ): Int

    @Modifying
    @Query(
        """
        delete from user_excluded_keywords
        where id = :id and user_id = :userId
        """
    )
    fun deleteByIdAndUserId(
        @Param("id") id: Long,
        @Param("userId") userId: Long
    ): Int
}