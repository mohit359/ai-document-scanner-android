package com.mohitkumar.docscanner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mohitkumar.docscanner.domain.model.Scan

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val summary: String? = null,
    val createdAt: Long,
)

fun ScanEntity.toDomain() = Scan(id, text, summary, createdAt)
