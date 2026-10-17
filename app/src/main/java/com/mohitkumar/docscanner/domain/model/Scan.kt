package com.mohitkumar.docscanner.domain.model

data class Scan(
    val id: Long,
    val text: String,
    val summary: String?,
    val createdAt: Long,
)
