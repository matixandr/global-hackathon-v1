package com.matixandr09.procrastination_app.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.matixandr09.procrastination_app.screens.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class AppViewModel : ViewModel() {
    private val _completedDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    val completedDates: StateFlow<Set<LocalDate>> = _completedDates

    val tasks = mutableStateListOf<Task>()

    fun addTask(text: String) {
        tasks.add(0, Task(text = text))
    }

    fun deleteTask(task: Task) {
        tasks.remove(task)
    }

    fun editTask(taskId: String, newText: String, newColor: String) {
        val taskIndex = tasks.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            tasks[taskIndex] = tasks[taskIndex].copy(text = newText, color = newColor)
        }
    }

    fun toggleTaskDone(task: Task) {
        val taskIndex = tasks.indexOfFirst { it.id == task.id }
        if (taskIndex != -1) {
            tasks[taskIndex] = tasks[taskIndex].copy(isDone = !tasks[taskIndex].isDone)
        }
    }

    fun addCompletedDate(date: LocalDate) {
        val currentDates = _completedDates.value.toMutableSet()
        currentDates.add(date)
        _completedDates.value = currentDates
    }

    fun removeCompletedDate(date: LocalDate) {
        val currentDates = _completedDates.value.toMutableSet()
        currentDates.remove(date)
        _completedDates.value = currentDates
    }
}
