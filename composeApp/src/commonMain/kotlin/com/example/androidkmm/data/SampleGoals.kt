package com.example.androidkmm.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import com.example.androidkmm.models.Goal
import com.example.androidkmm.database.rememberSQLiteGoalsDatabase
import com.example.androidkmm.database.SQLiteGoalsDatabase

/**
 * SampleGoals object for managing goals with database persistence
 * This replaces the in-memory storage with proper database persistence
 */
object SampleGoals {
    
    /**
     * Get goals from database as a Flow
     * This should be used in composables that need reactive goal updates
     */
    @Composable
    fun getGoalsFlow(): Flow<List<Goal>> {
        val goalsDatabase = rememberSQLiteGoalsDatabase()
        return goalsDatabase.getAllGoals()
    }
    
    /**
     * Get goals as State for use in composables
     */
    @Composable
    fun getGoalsState(): List<Goal> {
        val goalsFlow = getGoalsFlow()
        return goalsFlow.collectAsState(initial = emptyList()).value
    }
    
    /**
     * Add a goal to the database
     */
    @Composable
    fun addGoal(
        goal: Goal,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val goalsDatabase = rememberSQLiteGoalsDatabase()
        goalsDatabase.addGoal(goal, onSuccess, onError)
    }
    
    /**
     * Add a goal to the database (non-composable version)
     */
    fun addGoalNonComposable(
        goalsDatabase: SQLiteGoalsDatabase,
        goal: Goal,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        goalsDatabase.addGoal(goal, onSuccess, onError)
    }
    
    /**
     * Update a goal in the database
     */
    @Composable
    fun updateGoal(
        goal: Goal,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val goalsDatabase = rememberSQLiteGoalsDatabase()
        goalsDatabase.updateGoal(goal, onSuccess, onError)
    }
    
    /**
     * Update goal progress
     */
    @Composable
    fun updateGoalProgress(
        goalId: Long,
        currentAmount: Double,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val goalsDatabase = rememberSQLiteGoalsDatabase()
        goalsDatabase.updateGoalProgress(goalId, currentAmount, onSuccess, onError)
    }
    
    /**
     * Delete a goal from the database
     */
    @Composable
    fun deleteGoal(
        goalId: Long,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val goalsDatabase = rememberSQLiteGoalsDatabase()
        goalsDatabase.deleteGoal(goalId, onSuccess, onError)
    }
}
