package com.team1.hangsha.user.dto.Preference

import jakarta.validation.constraints.NotBlank

data class AddExcludedKeywordRequest(
    @field:NotBlank
    val keyword: String,
)