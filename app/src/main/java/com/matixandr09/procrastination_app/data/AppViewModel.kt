package com.matixandr09.procrastination_app.data

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class AppViewModel : ViewModel() {
    private val _completedDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    val completedDates: StateFlow<Set<LocalDate>> = _completedDates

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
