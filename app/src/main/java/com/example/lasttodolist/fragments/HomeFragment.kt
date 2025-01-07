package com.example.lasttodolist.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.semantics.text
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.lasttodolist.R
import com.example.lasttodolist.adapter.ToDoAdapter
import com.example.lasttodolist.databinding.FragmentHomeBinding
import com.example.lasttodolist.model.ToDoItem
import com.example.lasttodolist.viewmodel.ToDoViewModel
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var viewModel: ToDoViewModel
    private lateinit var adapter: ToDoAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ToDoViewModel::class.java]

        adapter = ToDoAdapter(
            emptyList(),
            onEdit = { showAddEditDialog(it) },
            onDelete = { viewModel.deleteToDo(it) },
            onCheckedChange = { viewModel.updateToDo(it) },
            onSetReminder = { showDateTimePicker(it) }
        )
        binding.recyclerView.adapter = adapter

        viewModel.toDoList.observe(viewLifecycleOwner) { toDoList ->
            Log.d("HomeFragment", "To-do list observed: ${toDoList.size} items")
            for (item in toDoList) {
                Log.d("HomeFragment", "Item: ${item.task}")
            }
            adapter.updateList(toDoList)
        }


        binding.fabAdd.setOnClickListener {
            showAddEditDialog(ToDoItem())
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showAddEditDialog(toDoItem: ToDoItem) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_edit_todo, null)
        val editTextTask = dialogLayout.findViewById<EditText>(R.id.editTextTask)
        val checkBoxCompleted = dialogLayout.findViewById<CheckBox>(R.id.checkBoxCompleted)
        val buttonSetReminder = dialogLayout.findViewById<Button>(R.id.buttonSetReminder)

        checkBoxCompleted.isChecked = toDoItem.isCompleted
        checkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
            toDoItem.isCompleted = isChecked
        }

        buttonSetReminder.setOnClickListener {
            showDateTimePicker(toDoItem)
        }
        editTextTask.setText(toDoItem.task)

        val dialog = builder.setView(dialogLayout)
            .setTitle(if (toDoItem.id == null) "Add ToDo" else "Edit ToDo")
            .setPositiveButton("Save") { _, _ ->
                val task = editTextTask.text.toString()
                if (task.isEmpty()) {
                    Log.e("HomeFragment", "Task is empty")
                    return@setPositiveButton
                }
                if (toDoItem.id == null) {
                    Log.d("HomeFragment", "Adding new to-do item: $task")
                    viewModel.addToDo(ToDoItem(task = task))
                } else {
                    Log.d("HomeFragment", "Updating to-do item: $task")
                    viewModel.updateToDo(toDoItem.copy(task = task))
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
        dialog.show()
    }

    private fun showDateTimePicker(toDoItem: ToDoItem) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        viewModel.setReminder(toDoItem, calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}