package com.example.androidkmm.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidkmm.models.Transaction
import com.example.androidkmm.models.TransactionType
import com.example.androidkmm.repository.TransactionRepository
import com.example.androidkmm.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for transaction management
 * 
 * Provides state management and business logic for transaction operations.
 * Implements MVVM pattern for clean separation of concerns.
 */
class TransactionViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()
    
    // Transactions state
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    /**
     * Load all transactions
     */
    fun loadTransactions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                transactionRepository.getAllTransactions().collect { transactionList ->
                    _transactions.value = transactionList
                    _uiState.value = _uiState.value.copy(
                        transactions = transactionList,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Logger.error("Failed to load transactions", "TransactionViewModel", e)
                _error.value = "Failed to load transactions: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Add a new transaction
     */
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val result = transactionRepository.addTransaction(transaction)
                result.fold(
                    onSuccess = {
                        Logger.info("Transaction added successfully", "TransactionViewModel")
                        loadTransactions() // Refresh the list
                    },
                    onFailure = { error ->
                        Logger.error("Failed to add transaction", "TransactionViewModel", error)
                        _error.value = "Failed to add transaction: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                Logger.error("Failed to add transaction", "TransactionViewModel", e)
                _error.value = "Failed to add transaction: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update an existing transaction
     */
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val result = transactionRepository.updateTransaction(transaction)
                result.fold(
                    onSuccess = {
                        Logger.info("Transaction updated successfully", "TransactionViewModel")
                        loadTransactions() // Refresh the list
                    },
                    onFailure = { error ->
                        Logger.error("Failed to update transaction", "TransactionViewModel", error)
                        _error.value = "Failed to update transaction: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                Logger.error("Failed to update transaction", "TransactionViewModel", e)
                _error.value = "Failed to update transaction: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Delete a transaction
     */
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val result = transactionRepository.deleteTransaction(transactionId)
                result.fold(
                    onSuccess = {
                        Logger.info("Transaction deleted successfully", "TransactionViewModel")
                        loadTransactions() // Refresh the list
                    },
                    onFailure = { error ->
                        Logger.error("Failed to delete transaction", "TransactionViewModel", error)
                        _error.value = "Failed to delete transaction: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                Logger.error("Failed to delete transaction", "TransactionViewModel", e)
                _error.value = "Failed to delete transaction: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Add multiple transactions in batch
     */
    fun addTransactionsBatch(transactions: List<Transaction>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val result = transactionRepository.addTransactionsBatch(transactions)
                result.fold(
                    onSuccess = {
                        Logger.info("Batch transactions added successfully", "TransactionViewModel")
                        loadTransactions() // Refresh the list
                    },
                    onFailure = { error ->
                        Logger.error("Failed to add batch transactions", "TransactionViewModel", error)
                        _error.value = "Failed to add batch transactions: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                Logger.error("Failed to add batch transactions", "TransactionViewModel", e)
                _error.value = "Failed to add batch transactions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Search transactions
     */
    fun searchTransactions(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                transactionRepository.searchTransactions(query).collect { searchResults ->
                    _transactions.value = searchResults
                    _uiState.value = _uiState.value.copy(
                        transactions = searchResults,
                        searchQuery = query,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Logger.error("Failed to search transactions", "TransactionViewModel", e)
                _error.value = "Failed to search transactions: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Filter transactions by type
     */
    fun filterTransactionsByType(type: TransactionType) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                transactionRepository.getTransactionsByType(type).collect { filteredTransactions ->
                    _transactions.value = filteredTransactions
                    _uiState.value = _uiState.value.copy(
                        transactions = filteredTransactions,
                        selectedType = type,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Logger.error("Failed to filter transactions by type", "TransactionViewModel", e)
                _error.value = "Failed to filter transactions: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Refresh transactions
     */
    fun refreshTransactions() {
        loadTransactions()
    }
}

/**
 * UI State for transaction screen
 */
data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedType: TransactionType? = null,
    val error: String? = null
)
