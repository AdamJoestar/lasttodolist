package com.example.lasttodolist

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lasttodolist.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val todos = mutableListOf<Todo>()
    private lateinit var adapter: TodoAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var nextRequestCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        nextRequestCode = sharedPreferences.getInt("nextRequestCode", 0)

        val userId = auth.currentUser?.uid
        if (userId == null) {
            // Handle user not logged in: redirect to AuthActivity
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("users/$userId/todos")

        adapter = TodoAdapter(todos) { todoId ->
            removeTodo(todoId)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.addButton.setOnClickListener {
            addTodo()
        }
        binding.logoutButton.setOnClickListener {
            logout()
        }

        loadTodos()
    }

    private fun addTodo() {
        val todoText = binding.todoEditText.text.toString().trim()
        if (TextUtils.isEmpty(todoText)) {
            binding.todoEditText.error = "Todo text cannot be empty"
            return
        }

        val todoId = database.push().key ?: return
        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                val reminderTime = calendar.timeInMillis

                if (reminderTime <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Reminder time must be in the future", Toast.LENGTH_SHORT).show()
                    return@OnTimeSetListener
                }

                val todo = Todo(todoId, todoText, reminderTime, nextRequestCode)
                database.child(todoId).setValue(todo)
                    .addOnSuccessListener {
                        // Data saved successfully
                        Toast.makeText(this, "Todo added successfully", Toast.LENGTH_SHORT).show()
                        setAlarm(todo)
                        nextRequestCode++
                        saveNextRequestCode()
                        binding.todoEditText.text.clear()
                    }
                    .addOnFailureListener { e ->
                        // Handle errors
                        Log.e("MainActivity", "Error adding todo: ${e.message}")
                        Toast.makeText(this, "Failed to add todo", Toast.LENGTH_SHORT).show()
                    }
            }
            TimePickerDialog(this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
        DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setAlarm(todo: Todo) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("TODO_TEXT", todo.text)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, todo.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, todo.reminderTime, pendingIntent)
    }

    private fun cancelAlarm(requestCode: Int) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }

    private fun removeTodo(todoId: String) {
        database.child(todoId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val todo = snapshot.getValue(Todo::class.java)
                todo?.let {
                    cancelAlarm(it.requestCode)
                    database.child(todoId).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this@MainActivity, "Todo removed successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("MainActivity", "Error removing todo: ${e.message}")
                            Toast.makeText(this@MainActivity, "Failed to remove todo", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Error removing todo: ${error.message}")
                Toast.makeText(this@MainActivity, "Failed to remove todo", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadTodos() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                todos.clear()
                snapshot.children.forEach {
                    val todo = it.getValue(Todo::class.java)
                    todo?.let { todos.add(it) }
                }
                adapter.notifyDataSetChanged()
                updateEmptyViewVisibility()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Error loading todos: ${error.message}")
                Toast.makeText(this@MainActivity, "Failed to load todos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logout() {
        todos.forEach { todo ->
            cancelAlarm(todo.requestCode)
        }
        auth.signOut()
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    private fun saveNextRequestCode() {
        with(sharedPreferences.edit()) {
            putInt("nextRequestCode", nextRequestCode)
            apply()
        }
    }
    private fun updateEmptyViewVisibility() {
        if (todos.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
}

data class Todo(
    val id: String = "",
    val text: String = "",
    val reminderTime: Long = 0,
    val requestCode: Int = 0
)