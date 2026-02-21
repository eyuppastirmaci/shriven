package com.eyuppastirmaci.shriven.backend.tag

import com.eyuppastirmaci.shriven.backend.exception.AccessDeniedException
import com.eyuppastirmaci.shriven.backend.snowflake.SnowflakeIdGenerator
import com.eyuppastirmaci.shriven.backend.tag.dto.CreateTagRequest
import com.eyuppastirmaci.shriven.backend.tag.dto.TagResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class TagService(
    private val tagRepository: TagRepository,
    private val snowflakeIdGenerator: SnowflakeIdGenerator
) {

    @Transactional(readOnly = true)
    fun getUserTags(userId: Long): List<TagResponse> =
        tagRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .map { it.toResponse() }

    @Transactional
    fun createTag(request: CreateTagRequest, userId: Long): TagResponse {
        val trimmedName = request.name.trim()
        if (tagRepository.existsByNameAndUserId(trimmedName, userId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Tag '$trimmedName' already exists")
        }
        val tag = tagRepository.save(
            TagEntity(
                id = snowflakeIdGenerator.nextId(),
                name = trimmedName,
                userId = userId
            )
        )
        return tag.toResponse()
    }

    @Transactional
    fun deleteTag(tagId: Long, userId: Long) {
        val tag = tagRepository.findById(tagId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found: $tagId")
        }
        if (tag.userId != userId) {
            throw AccessDeniedException("You do not have permission to delete this tag")
        }
        tagRepository.delete(tag)
    }

    private fun TagEntity.toResponse() = TagResponse(
        id = id,
        name = name,
        createdAt = createdAt.toString()
    )
}
