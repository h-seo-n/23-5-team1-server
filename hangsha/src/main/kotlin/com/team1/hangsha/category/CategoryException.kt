package com.team1.hangsha.category

import com.team1.hangsha.common.error.DomainException
import com.team1.hangsha.common.error.ErrorCode

class CategoryException(
    errorCode: ErrorCode,
    message: String = errorCode.message,
    cause: Throwable? = null,
) : DomainException(errorCode, message, cause) {

    companion object {
        fun groupNotFound(name: String? = null): CategoryException =
            CategoryException(
                ErrorCode.CATEGORY_GROUP_NOT_FOUND,
                if (name.isNullOrBlank()) ErrorCode.CATEGORY_GROUP_NOT_FOUND.message
                else "${ErrorCode.CATEGORY_GROUP_NOT_FOUND.message}: $name"
            )

        fun categoryNotFound(id: Long? = null): CategoryException =
            CategoryException(
                ErrorCode.CATEGORY_NOT_FOUND,
                if (id == null) ErrorCode.CATEGORY_NOT_FOUND.message
                else "${ErrorCode.CATEGORY_NOT_FOUND.message}: id=$id"
            )
    }
}
