package com.example.androidkmm.data

import com.example.androidkmm.models.Goal
import kotlinx.datetime.LocalDate

object SampleGoals {
    private val _goals = mutableListOf<Goal>()
    
    val goals: List<Goal> get() = _goals.toList()
    
    fun addGoal(goal: Goal) {
        val newId = (_goals.maxOfOrNull { it.id } ?: 0) + 1
        _goals.add(goal.copy(id = newId))
    }
}
