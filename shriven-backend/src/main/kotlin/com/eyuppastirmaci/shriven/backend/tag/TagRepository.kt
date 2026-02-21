package com.eyuppastirmaci.shriven.backend.tag

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : JpaRepository<TagEntity, Long> {

    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<TagEntity>

    fun findByNameAndUserId(name: String, userId: Long): TagEntity?

    fun existsByNameAndUserId(name: String, userId: Long): Boolean
}
