package com.team1.hangsha.user.service

import com.team1.hangsha.common.error.DomainException
import com.team1.hangsha.common.error.ErrorCode
import com.team1.hangsha.category.dto.core.CategoryDto
import com.team1.hangsha.category.repository.CategoryRepository
import com.team1.hangsha.user.dto.Preference.ListExcludedKeywordResponse
import com.team1.hangsha.user.dto.Preference.ListInterestCategoryResponse
import com.team1.hangsha.user.dto.Preference.ReplaceAllInterestCategoriesRequest
import com.team1.hangsha.user.model.UserExcludedKeyword
import com.team1.hangsha.user.repository.UserExcludedKeywordRepository
import com.team1.hangsha.user.repository.UserInterestCategoryRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserPreferenceService(
    private val userInterestCategoryRepository: UserInterestCategoryRepository,
    private val userExcludedKeywordRepository: UserExcludedKeywordRepository,
    private val categoryRepository: CategoryRepository,
) {

    // ==============================
    // Interest Categories
    // ==============================
    @Transactional(readOnly = true)
    fun listInterestCategory(userId: Long): List<ListInterestCategoryResponse.Item> {
        val rows = userInterestCategoryRepository.findAllWithCategoryByUserId(userId)

        return rows.map { row ->
            ListInterestCategoryResponse.Item(
                category = CategoryDto(
                    id = row.categoryId,
                    groupId = row.groupId,
                    name = row.name,
                    sortOrder = row.sortOrder,
                ),
                priority = row.priority
            )
        }
    }

    @Transactional
    fun replaceAllInterestCategories(userId: Long, req: ReplaceAllInterestCategoriesRequest) {
        val items = req.items

        // 1) categoryId 중복 금지
        val categoryIds = items.map { it.categoryId }
        if (categoryIds.size != categoryIds.distinct().size) {
            throw DomainException(ErrorCode.INVALID_REQUEST)
        }

        // 2) priority 중복 금지
        val priorities = items.map { it.priority }
        if (priorities.size != priorities.distinct().size) {
            throw DomainException(ErrorCode.INVALID_REQUEST)
        }

        // 3) priority 연속(1..N) 강제 (권장)
        if (items.isNotEmpty()) {
            val sorted = priorities.sorted()
            val expected = (1..items.size).toList()
            if (sorted != expected) {
                throw DomainException(ErrorCode.INVALID_REQUEST)
            }
        }

        // 4) category 존재 검증 (IN 한번)
        if (items.isNotEmpty()) {
            val existCount = categoryRepository.countByIds(categoryIds)
            if (existCount != categoryIds.size) {
                throw DomainException(ErrorCode.INVALID_REQUEST)
            }
        }

        // 5) 전체 교체 (트랜잭션)
        userInterestCategoryRepository.deleteAllByUserId(userId)

        if (items.isNotEmpty()) {
            userInterestCategoryRepository.saveAll(
                items.map {
                    com.team1.hangsha.user.model.UserInterestCategory(
                        userId = userId,
                        categoryId = it.categoryId,
                        priority = it.priority
                    )
                }
            )
        }
    }

    @Transactional
    fun delete(userId: Long, categoryId: Long) {
        val affected = userInterestCategoryRepository.deleteByUserIdAndCategoryId(userId, categoryId)
        if (affected == 0) {
            throw DomainException(ErrorCode.INVALID_REQUEST)
        }
    }

    // ==============================
    // Excluded Keywords
    // ==============================
    @Transactional(readOnly = true)
    fun listExcludedKeywords(userId: Long): ListExcludedKeywordResponse {
        val rows = userExcludedKeywordRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(userId)
        return ListExcludedKeywordResponse(
            items = rows.map {
                ListExcludedKeywordResponse.Item(
                    id = requireNotNull(it.id),
                    keyword = it.keyword,
                    createdAt = requireNotNull(it.createdAt),
                )
            }
        )
    }

    @Transactional
    fun addExcludedKeyword(userId: Long, rawKeyword: String) {
        val keyword = rawKeyword.trim()
        if (keyword.isBlank()) throw DomainException(ErrorCode.INVALID_REQUEST)

        // 이미 있으면 그대로 둠 (idempotent)
        if (userExcludedKeywordRepository.countByUserIdAndKeyword(userId, keyword) > 0) return

        try {
            userExcludedKeywordRepository.save(
                UserExcludedKeyword(
                    userId = userId,
                    keyword = keyword
                )
            )
        } catch (_: DuplicateKeyException) {
            // 동시성으로 unique 충돌 시 무시 (결과 동일)
        }
    }

    @Transactional
    fun deleteExcludedKeyword(userId: Long, excludedKeywordId: Long) {
        val affected = userExcludedKeywordRepository.deleteByIdAndUserId(excludedKeywordId, userId)
        if (affected == 0) {
            // "내 것이 아니거나/없는 id" → 요청 자체가 유효하지 않다고 처리
            throw DomainException(ErrorCode.INVALID_REQUEST)
        }
    }
}