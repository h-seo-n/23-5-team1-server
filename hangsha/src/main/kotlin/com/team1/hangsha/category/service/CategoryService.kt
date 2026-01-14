package com.team1.hangsha.category.service

import com.team1.hangsha.category.dto.CategoryResponse
import com.team1.hangsha.category.dto.core.CategoryDto
import com.team1.hangsha.category.dto.core.CategoryGroupDto
import com.team1.hangsha.category.repository.CategoryGroupRepository
import com.team1.hangsha.category.repository.CategoryRepository
import com.team1.hangsha.common.error.DomainException
import com.team1.hangsha.common.error.ErrorCode
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryGroupRepository: CategoryGroupRepository,
    private val categoryRepository: CategoryRepository
) {
    fun getCategoryGroupsWithCategories(): List<CategoryResponse> {
        val groups = categoryGroupRepository.findAllByOrderBySortOrderAsc()

        return groups.map { g ->
            val groupId = g.id
                ?: throw DomainException(ErrorCode.CATEGORY_GROUP_NOT_FOUND)

            val categories = categoryRepository.findAllByGroupIdOrderBySortOrderAsc(groupId)
                .map { c ->
                    CategoryDto(
                        id = c.id ?: throw DomainException(ErrorCode.CATEGORY_NOT_FOUND),
                        groupId = c.groupId,
                        name = c.name,
                        sortOrder = c.sortOrder
                    )
                }

            CategoryResponse(
                group = CategoryGroupDto(
                    id = groupId,
                    name = g.name,
                    sortOrder = g.sortOrder
                ),
                categories = categories
            )
        }
    }
}
