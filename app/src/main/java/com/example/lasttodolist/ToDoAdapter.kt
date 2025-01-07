package com.example.lasttodolist.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import com.example.lasttodolist.R
import com.example.lasttodolist.model.ToDoItem

class ToDoAdapter(
    private var toDoList: List<ToDoItem>,
    private val onEdit: (ToDoItem) -> Unit,
    private val onDelete: (ToDoItem) -> Unit,
    private val onCheckedChange: (ToDoItem) -> Unit,
    private val onSetReminder: (ToDoItem) -> Unit
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val textViewTask: TextView = itemView.findViewById(R.id.textViewTask)
        val imageViewDelete: ImageView = itemView.findViewById(R.id.imageViewDelete)
        val buttonSetReminder: Button = itemView.findViewById(R.id.buttonSetReminder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return ToDoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val currentItem = toDoList[position]
        holder.checkBox.isChecked = currentItem.isCompleted
        holder.textViewTask.text = currentItem.task

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChange(currentItem.copy(isCompleted = isChecked))
        }

        holder.textViewTask.setOnClickListener {
            onEdit(currentItem)
        }

        holder.imageViewDelete.setOnClickListener {
            onDelete(currentItem)
        }

        holder.buttonSetReminder.setOnClickListener {
            onSetReminder(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return toDoList.size
    }

    fun updateList(newList: List<ToDoItem>) {
        toDoList = newList
        notifyDataSetChanged()
        Log.d("ToDoAdapter", "To-do list updated: ${newList.size} items")
    }
}