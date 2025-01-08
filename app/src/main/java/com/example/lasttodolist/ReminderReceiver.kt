package com.example.lasttodolist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val todoText = intent.getStringExtra("TODO_TEXT")
        Toast.makeText(context, "Reminder: $todoText", Toast.LENGTH_LONG).show()
    }
}