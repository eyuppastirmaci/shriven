package com.eyuppastirmaci.shriven.backend.url.dto.response

import com.eyuppastirmaci.shriven.backend.tag.dto.TagResponse

data class UserUrlResponse(
    val shortCode: String,
    val shortUrl: String,
    val longUrl: String,
    val clickCount: Long,
    val createdAt: String,
    val expiresAt: String?,
    val isActive: Boolean = true,
    val isCustomAlias: Boolean = false,
    val passwordProtected: Boolean = false,
    val tags: List<TagResponse> = emptyList()
)
