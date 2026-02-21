package com.eyuppastirmaci.shriven.backend.tag

import com.eyuppastirmaci.shriven.backend.auth.AuthPrincipal
import com.eyuppastirmaci.shriven.backend.tag.dto.CreateTagRequest
import com.eyuppastirmaci.shriven.backend.tag.dto.TagResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tags")
class TagController(
    private val tagService: TagService
) {

    @GetMapping
    fun getUserTags(
        @AuthenticationPrincipal principal: AuthPrincipal
    ): ResponseEntity<List<TagResponse>> {
        return ResponseEntity.ok(tagService.getUserTags(principal.userId))
    }

    @PostMapping
    fun createTag(
        @Valid @RequestBody request: CreateTagRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ): ResponseEntity<TagResponse> {
        val tag = tagService.createTag(request, principal.userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(tag)
    }

    @DeleteMapping("/{id}")
    fun deleteTag(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: AuthPrincipal
    ): ResponseEntity<Void> {
        tagService.deleteTag(id, principal.userId)
        return ResponseEntity.noContent().build()
    }
}
