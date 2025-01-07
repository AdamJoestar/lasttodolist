package com.example.lasttodolist.model

import java.util.UUID

data class ToDoItem(
    var id: String? = UUID.randomUUID().toString(), // Unique ID
    var task: String? = null,
    var isCompleted: Boolean = false,
    var reminderDateTime: Long? = null // Store reminder as timestamp
)