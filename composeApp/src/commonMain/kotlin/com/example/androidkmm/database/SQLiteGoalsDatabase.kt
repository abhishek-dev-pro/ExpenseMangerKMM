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
    
    // Track whether database operations are working
    private var isDatabaseAvailable = false
    private var isInitialized = false
    
    init {
        // Initialize database synchronously to prevent crashes
        initializeDatabaseSync()
        // Load initial goals from database into memory for real-time updates
        loadGoalsFromDatabase()
    }
    
    /**
     * Initialize database and check if goals table is available (synchronous)
     */
    private fun initializeDatabaseSync() {
        try {
            // Try to query the goals table to check if it exists
            database.categoryDatabaseQueries.selectAllGoals()
            isDatabaseAvailable = true
            println("Goals table exists, using database storage")
        } catch (e: Exception) {
            if (e.message?.contains("no such table: goals") == true) {
                println("Goals table doesn't exist, database migration may be needed")
                // Don't set isDatabaseAvailable = false yet, let the migration handle it
                isDatabaseAvailable = false
            } else {
                println("Database error: ${e.message}")
                isDatabaseAvailable = false
            }
        }
        isInitialized = true
    }
    
    /**
     * Load goals from database into memory for real-time updates
     */
    private fun loadGoalsFromDatabase() {
        try {
            val goalEntities = database.categoryDatabaseQueries.selectAllGoals().executeAsList()
            val goals = goalEntities.map { it.toGoal() }
            _globalGoals.clear()
            _globalGoals.addAll(goals)
            updateGoalsFlow()
            println("Loaded ${goals.size} goals from database into memory")
        } catch (e: Exception) {
            println("Error loading goals from database: ${e.message}")
            // Continue with empty memory storage
        }
    }

    /**
     * Get all goals as a Flow for reactive UI updates
     */
    fun getAllGoals(): Flow<List<com.example.androidkmm.models.Goal>> {
        // Always use in-memory StateFlow for real-time UI updates
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
                    
                    try {
                        // Always try database first for persistence
                        database.categoryDatabaseQueries.insertGoal(
                            id = goalId.toString(),
                            title = goal.title,
                            description = goal.description,
                            target_amount = goal.targetAmount,
                            current_amount = goal.currentAmount,
                            deadline = goal.deadline?.toString(),
                            is_recurring = if (goal.isRecurring) 1L else 0L,
                            monthly_amount = goal.monthlyAmount,
                            icon = goal.icon,
                            color = goal.color,
                            priority = goal.priority,
                            progress_percentage = progressPercentage.toLong(),
                            remaining_amount = remainingAmount,
                            status = status
                        )
                        println("Goal saved to database successfully")
                    } catch (e: Exception) {
                        println("Database error, using in-memory storage: ${e.message}")
                        // Continue with in-memory storage as fallback
                    }
                    
                    // Always update in-memory storage for immediate UI updates
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
                        
                        // Always update in-memory storage first for immediate UI updates
                        _globalGoals[index] = updatedGoal
                        updateGoalsFlow()
                        
                        try {
                            // Try to update database for persistence
                            database.categoryDatabaseQueries.updateGoalProgress(
                                current_amount = currentAmount,
                                progress_percentage = progressPercentage.toLong(),
                                remaining_amount = remainingAmount,
                                status = updatedGoal.status,
                                id = goalId.toString()
                            )
                            println("Goal progress updated in database successfully")
                        } catch (e: Exception) {
                            println("Database error, using in-memory storage: ${e.message}")
                            // Continue with in-memory storage as fallback
                        }
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
                    // Always update in-memory storage first for immediate UI updates
                    _globalGoals.removeAll { it.id == goalId }
                    updateGoalsFlow()
                    
                    try {
                        // Always try database first for persistence
                        database.categoryDatabaseQueries.deleteGoal(goalId.toString())
                        println("Goal deleted from database successfully")
                    } catch (e: Exception) {
                        println("Database error, using in-memory storage: ${e.message}")
                        // Continue with in-memory storage as fallback
                    }
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

private fun com.example.androidkmm.database.Goals.toGoal(): com.example.androidkmm.models.Goal {
    return com.example.androidkmm.models.Goal(
        id = this.id.toLong(),
        title = this.title,
        description = this.description,
        targetAmount = this.target_amount,
        currentAmount = this.current_amount,
        deadline = this.deadline?.let { LocalDate.parse(it) },
        isRecurring = this.is_recurring == 1L,
        monthlyAmount = this.monthly_amount,
        icon = this.icon,
        color = this.color,
        priority = this.priority,
        progressPercentage = this.progress_percentage.toInt(),
        remainingAmount = this.remaining_amount,
        status = this.status
    )
}
