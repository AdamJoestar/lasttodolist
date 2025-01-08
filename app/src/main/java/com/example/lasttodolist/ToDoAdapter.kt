package com.example.lasttodolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import com.example.lasttodolist.databinding.TodoItemBinding
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(
    private val todos: List<Todo>,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    class TodoViewHolder(val binding: TodoItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = TodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todos[position]
        holder.binding.todoText.text = todo.text

        // Format and display the reminder time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedTime = dateFormat.format(Date(todo.reminderTime))
        holder.binding.reminderTime.text = "Reminder: $formattedTime"

        holder.binding.removeButton.setOnClickListener {
            onRemoveClick(todo.id)
        }
    }

    override fun getItemCount(): Int = todos.size
}