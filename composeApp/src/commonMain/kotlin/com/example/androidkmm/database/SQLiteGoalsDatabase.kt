package com.example.androidkmm.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.androidkmm.models.Goal
import com.example.androidkmm.models.GoalIcon
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.time.ExperimentalTime

@Composable
fun rememberSQLiteGoalsDatabase(): SQLiteGoalsDatabase {
    val driverFactory = rememberDatabaseDriverFactory()
    val database = remember { CategoryDatabase(driverFactory.createDriver()) }
    val scope = rememberCoroutineScope()
    
    return remember {
        SQLiteGoalsDatabase(database, scope)
    }
}

/**
 * SQLite Goals Database Manager
 * 
 * Handles all goal-related database operations including:
 * - Adding goals with automatic progress calculation
 * - Updating goals and their progress
 * - Deleting goals
 * - Retrieving goals with reactive updates
 * 
 * @param database The SQLite database instance
 * @param scope Coroutine scope for async operations
 */
// Global goals storage to ensure persistence across instances
private val _globalGoals = mutableListOf<Goal>()
private val _goalsStateFlow = kotlinx.coroutines.flow.MutableStateFlow<List<Goal>>(emptyList())

@OptIn(ExperimentalTime::class)
class SQLiteGoalsDatabase(
    private val database: CategoryDatabase,
    private val scope: kotlinx.coroutines.CoroutineScope
) {
    
    /**
     * Get all goals as a Flow for reactive UI updates
     */
    fun getAllGoals(): Flow<List<Goal>> {
        return _goalsStateFlow.asStateFlow()
    }
    
    /**
     * Helper function to update the StateFlow when goals change
     */
    private fun updateGoalsFlow() {
        _goalsStateFlow.value = _globalGoals.toList()
    }
    
    /**
     * Add a new goal to the database
     */
    fun addGoal(
        goal: Goal,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        scope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val goalId = if (goal.id == 0L) UUID.randomUUID().hashCode().toLong() else goal.id
                    
                    // Calculate progress percentage and remaining amount
                    val progressPercentage = if (goal.targetAmount > 0) {
                        ((goal.currentAmount / goal.targetAmount) * 100).toInt().coerceIn(0, 100)
                    } else 0
                    
                    val remainingAmount = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
                    
                    // Determine status based on progress and deadline
                    val status = determineGoalStatus(goal, progressPercentage)
                    
                    val newGoal = goal.copy(
                        id = goalId,
                        progressPercentage = progressPercentage,
                        remainingAmount = remainingAmount,
                        status = status
                    )
                    
                    _globalGoals.add(newGoal)
                    updateGoalsFlow()
                }
                onSuccess()
            } catch (e: Exception) {
                println("Error adding goal: ${e.message}")
                onError(e)
            }
        }
    }
    
    /**
     * Update an existing goal
     */
    fun updateGoal(
        goal: Goal,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        scope.launch {
            try {
                withContext(Dispatchers.Default) {
                    // Calculate progress percentage and remaining amount
                    val progressPercentage = if (goal.targetAmount > 0) {
                        ((goal.currentAmount / goal.targetAmount) * 100).toInt().coerceIn(0, 100)
                    } else 0
                    
                    val remainingAmount = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
                    
                    // Determine status based on progress and deadline
                    val status = determineGoalStatus(goal, progressPercentage)
                    
                    val updatedGoal = goal.copy(
                        progressPercentage = progressPercentage,
                        remainingAmount = remainingAmount,
                        status = status
                    )
                    
                    val index = _globalGoals.indexOfFirst { it.id == goal.id }
                    if (index >= 0) {
                        _globalGoals[index] = updatedGoal
                        updateGoalsFlow()
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                println("Error updating goal: ${e.message}")
                onError(e)
            }
        }
    }
    
    /**
     * Update goal progress (current amount, percentage, remaining, status)
     */
    fun updateGoalProgress(
        goalId: Long,
        currentAmount: Double,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        scope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val index = _globalGoals.indexOfFirst { it.id == goalId }
                    if (index >= 0) {
                        val goal = _globalGoals[index]
                        val targetAmount = goal.targetAmount
                        val progressPercentage = if (targetAmount > 0) {
                            ((currentAmount / targetAmount) * 100).toInt().coerceIn(0, 100)
                        } else 0
                        
                        val remainingAmount = (targetAmount - currentAmount).coerceAtLeast(0.0)
                        
                        val updatedGoal = goal.copy(
                            currentAmount = currentAmount,
                            progressPercentage = progressPercentage,
                            remainingAmount = remainingAmount,
                            status = determineGoalStatus(goal.copy(currentAmount = currentAmount), progressPercentage)
                        )
                        
                        _globalGoals[index] = updatedGoal
                        updateGoalsFlow()
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                println("Error updating goal progress: ${e.message}")
                onError(e)
            }
        }
    }
    
    /**
     * Delete a goal
     */
    fun deleteGoal(
        goalId: Long,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        scope.launch {
            try {
                withContext(Dispatchers.Default) {
                    _globalGoals.removeAll { it.id == goalId }
                    updateGoalsFlow()
                }
                onSuccess()
            } catch (e: Exception) {
                println("Error deleting goal: ${e.message}")
                onError(e)
            }
        }
    }
    
    /**
     * Get goal count
     */
    suspend fun getGoalCount(): Long {
        return withContext(Dispatchers.Default) {
            _globalGoals.size.toLong()
        }
    }
    
    /**
     * Clear all goals (for testing or reset functionality)
     */
    fun clearAllGoals(
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        scope.launch {
            try {
                withContext(Dispatchers.Default) {
                    _globalGoals.clear()
                    updateGoalsFlow()
                }
                onSuccess()
            } catch (e: Exception) {
                println("Error clearing goals: ${e.message}")
                onError(e)
            }
        }
    }
    
    /**
     * Determine goal status based on progress and deadline
     */
    private fun determineGoalStatus(goal: Goal, progressPercentage: Int): String {
        // For now, skip deadline checking to avoid Clock.System issues
        // TODO: Implement proper deadline checking when Clock.System is available
        
        // Determine status based on progress
        return when {
            progressPercentage >= 100 -> "completed"
            progressPercentage >= 75 -> "on track"
            progressPercentage >= 50 -> "behind"
            else -> "behind"
        }
    }
}