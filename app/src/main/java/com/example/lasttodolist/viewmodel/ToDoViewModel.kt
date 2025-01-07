package com.example.lasttodolist.viewmodel

import android.util.Log
import androidx.compose.ui.input.key.key
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lasttodolist.model.ToDoItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ToDoViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()
    private val toDoRef = database.getReference("todos")

    private val _toDoList = MutableLiveData<List<ToDoItem>>()
    val toDoList: LiveData<List<ToDoItem>> = _toDoList

    init {
        loadToDoList()
    }

    fun addToDo(toDoItem: ToDoItem) {
        val key = toDoRef.push().key
        if (key == null) {
            Log.e("ToDoViewModel", "Couldn't get push key for new to-do item")
            return
        }
        toDoItem.id = key
        Log.d("ToDoViewModel", "Saving to Firebase: $toDoItem")
        toDoRef.child(key).setValue(toDoItem)
            .addOnSuccessListener {
                Log.d("ToDoViewModel", "Successfully added to-do item: ${toDoItem.task}")
            }
            .addOnFailureListener {
                Log.e("ToDoViewModel", "Error adding to-do item", it)
            }
    }


    fun updateToDo(toDoItem: ToDoItem) {
        toDoItem.id?.let {
            toDoRef.child(it).setValue(toDoItem)
                .addOnSuccessListener {
                    Log.d("ToDoViewModel", "Successfully updated to-do item: ${toDoItem.task}")
                }
                .addOnFailureListener {
                    Log.e("ToDoViewModel", "Error updating to-do item", it)
                }
        }
    }

    fun deleteToDo(toDoItem: ToDoItem) {
        toDoItem.id?.let {
            toDoRef.child(it).removeValue()
                .addOnSuccessListener {
                    Log.d("ToDoViewModel", "Successfully deleted to-do item: ${toDoItem.task}")
                }
                .addOnFailureListener {
                    Log.e("ToDoViewModel", "Error deleting to-do item", it)
                }
        }
    }

    fun setReminder(toDoItem: ToDoItem, reminderDateTime: Long) {
        toDoItem.reminderDateTime = reminderDateTime
        updateToDo(toDoItem)
    }

    private fun loadToDoList() {
        toDoRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ToDoItem>()
                Log.d("ToDoViewModel", "Firebase snapshot: ${snapshot.value}")
                for (dataSnapshot in snapshot.children) {
                    val toDoItem = dataSnapshot.getValue(ToDoItem::class.java)
                    if (toDoItem != null) {
                        list.add(toDoItem)
                    } else {
                        Log.e("ToDoViewModel", "Error getting to-do item")
                    }
                }
                _toDoList.value = list
                Log.d("ToDoViewModel", "To-do list updated: ${list.size} items")
            }


            override fun onCancelled(error: DatabaseError) {
                Log.e("ToDoViewModel", "Error loading to-do list", error.toException())
            }
        })
    }
}