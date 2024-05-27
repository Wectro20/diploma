package com.andrii.diploma.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "rollerblinds")
data class RollerBlindEntity(
    @Id val id: String? = null,
    val name: String,
    val deviceId: String,
    val length: Float,
    var lengthToRotate: Float,
    var openedLength: Float,
    var command: String
)
