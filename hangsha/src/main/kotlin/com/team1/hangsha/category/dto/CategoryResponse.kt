package com.team1.hangsha.category.dto

import com.team1.hangsha.category.dto.core.CategoryDto
import com.team1.hangsha.category.dto.core.CategoryGroupDto

data class CategoryResponse(
    val group: CategoryGroupDto,
    val categories: List<CategoryDto>
)