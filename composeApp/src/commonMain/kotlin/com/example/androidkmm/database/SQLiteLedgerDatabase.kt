package com.example.androidkmm.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.androidkmm.database.CategoryDatabase
import com.example.androidkmm.screens.ledger.LedgerPerson
import com.example.androidkmm.screens.ledger.LedgerTransaction
import com.example.androidkmm.screens.ledger.TransactionType
import com.example.androidkmm.database.parseColorHex
import androidx.compose.ui.graphics.Color
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock as DateTimeClock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

class SQLiteLedgerDatabase(
    private val database: CategoryDatabase
) {
    
    // Ledger Person Operations
    fun getAllLedgerPersons(): Flow<List<LedgerPerson>> {
        return database.categoryDatabaseQueries.selectAllLedgerPersons().asFlow().mapToList(Dispatchers.IO).map { rows ->
            rows.map { it.toLedgerPerson() }
        }
    }
    
    suspend fun getLedgerPersonById(id: String): LedgerPerson? {
        return withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.selectLedgerPersonById(id).executeAsOneOrNull()?.toLedgerPerson()
        }
    }
    
    suspend fun getLedgerPersonByName(name: String): LedgerPerson? {
        return withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.selectLedgerPersonByName(name).executeAsOneOrNull()?.toLedgerPerson()
        }
    }
    
    suspend fun insertLedgerPerson(ledgerPerson: LedgerPerson) {
        withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.insertLedgerPerson(
                id = ledgerPerson.id,
                name = ledgerPerson.name,
                avatar_color_hex = ledgerPerson.avatarColor.toHexString(),
                balance = ledgerPerson.balance,
                transaction_count = ledgerPerson.transactionCount.toLong(),
                last_transaction_date = ledgerPerson.lastTransactionDate
            )
        }
    }
    
    suspend fun updateLedgerPerson(ledgerPerson: LedgerPerson) {
        withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.updateLedgerPerson(
                name = ledgerPerson.name,
                avatar_color_hex = ledgerPerson.avatarColor.toHexString(),
                balance = ledgerPerson.balance,
                transaction_count = ledgerPerson.transactionCount.toLong(),
                last_transaction_date = ledgerPerson.lastTransactionDate,
                id = ledgerPerson.id
            )
        }
    }
    
    suspend fun updateLedgerPersonBalance(id: String, balance: Double) {
        withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.updateLedgerPersonBalance(balance = balance, id = id)
        }
    }
    
    suspend fun deleteLedgerPerson(id: String) {
        withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.deleteLedgerPerson(id)
        }
    }
    
    suspend fun getLedgerPersonCount(): Long {
        return withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.getLedgerPersonCount().executeAsOne()
        }
    }
    
    // Ledger Transaction Operations
    fun getAllLedgerTransactions(): Flow<List<LedgerTransaction>> {
        return database.categoryDatabaseQueries.selectAllLedgerTransactions().asFlow().mapToList(Dispatchers.IO).map { rows ->
            rows.map { it.toLedgerTransaction() }
        }
    }
    
    fun getLedgerTransactionsByPerson(personId: String): Flow<List<LedgerTransaction>> {
        return database.categoryDatabaseQueries.selectLedgerTransactionsByPerson(personId).asFlow().mapToList(Dispatchers.IO).map { rows ->
            rows.map { it.toLedgerTransaction() }
        }
    }
    
    suspend fun getLedgerTransactionById(id: String): LedgerTransaction? {
        return withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.selectLedgerTransactionById(id).executeAsOneOrNull()?.toLedgerTransaction()
        }
    }
    
    suspend fun insertLedgerTransaction(ledgerTransaction: LedgerTransaction) {
        withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.insertLedgerTransaction(
                id = ledgerTransaction.id,
                ledger_person_id = ledgerTransaction.personId,
                amount = ledgerTransaction.amount,
                description = ledgerTransaction.description,
                date = ledgerTransaction.date,
                time = ledgerTransaction.time,
                type = ledgerTransaction.type.name,
                account_name = ledgerTransaction.account
            )
        }
    }
    
    suspend fun updateLedgerTransaction(ledgerTransaction: LedgerTransaction) {
        withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.updateLedgerTransaction(
                amount = ledgerTransaction.amount,
                description = ledgerTransaction.description,
                date = ledgerTransaction.date,
                time = ledgerTransaction.time,
                type = ledgerTransaction.type.name,
                account_name = ledgerTransaction.account,
                id = ledgerTransaction.id
            )
        }
    }
    
    suspend fun deleteLedgerTransaction(id: String) {
        withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.deleteLedgerTransaction(id)
        }
    }
    
    suspend fun deleteLedgerTransactionsByPerson(personId: String) {
        withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.deleteLedgerTransactionsByPerson(personId)
        }
    }
    
    suspend fun getLedgerTransactionCount(): Long {
        return withContext(Dispatchers.IO) {
            database.categoryDatabaseQueries.getLedgerTransactionCount().executeAsOne()
        }
    }
    
    // Combined Operations
    @OptIn(ExperimentalTime::class)
    suspend fun addLedgerTransactionAndUpdatePerson(
        ledgerTransaction: LedgerTransaction,
        personId: String
    ) {
        withContext(Dispatchers.IO) {
            // Insert the ledger transaction
            insertLedgerTransaction(ledgerTransaction)
            
            // Update person's balance and transaction count
            val person = getLedgerPersonById(personId)
            if (person != null) {
                val newBalance = when (ledgerTransaction.type) {
                    TransactionType.SENT -> person.balance - ledgerTransaction.amount
                    TransactionType.RECEIVED -> person.balance + ledgerTransaction.amount
                }
                val newTransactionCount = person.transactionCount + 1
                val lastTransactionDate = "Today"
                
                updateLedgerPerson(
                    person.copy(
                        balance = newBalance,
                        transactionCount = newTransactionCount,
                        lastTransactionDate = lastTransactionDate
                    )
                )
            }
        }
    }
    
    suspend fun deleteLedgerTransactionAndUpdatePerson(
        transactionId: String,
        personId: String
    ) {
        withContext(Dispatchers.IO) {
            // Get the transaction to reverse its effect
            val transaction = getLedgerTransactionById(transactionId)
            if (transaction != null) {
                // Delete the transaction
                deleteLedgerTransaction(transactionId)
                
                // Update person's balance and transaction count
                val person = getLedgerPersonById(personId)
                if (person != null) {
                    val newBalance = when (transaction.type) {
                        TransactionType.SENT -> person.balance + transaction.amount
                        TransactionType.RECEIVED -> person.balance - transaction.amount
                    }
                    val newTransactionCount = maxOf(0, person.transactionCount - 1)
                    
                    // Get the most recent transaction date
                    val recentTransactions = getLedgerTransactionsByPerson(personId)
                    val lastTransactionDate = recentTransactions.first().firstOrNull()?.date ?: ""
                    
                    updateLedgerPerson(
                        person.copy(
                            balance = newBalance,
                            transactionCount = newTransactionCount,
                            lastTransactionDate = lastTransactionDate
                        )
                    )
                }
            }
        }
    }
}

// Extension functions to convert database rows to models
private fun com.example.androidkmm.database.Ledger_persons.toLedgerPerson(): LedgerPerson {
    return LedgerPerson(
        id = id,
        name = name,
        avatarColor = parseColorHex(avatar_color_hex),
        balance = balance,
        transactionCount = transaction_count.toInt(),
        lastTransactionDate = last_transaction_date ?: ""
    )
}

private fun com.example.androidkmm.database.Ledger_transactions.toLedgerTransaction(): LedgerTransaction {
    return LedgerTransaction(
        id = id,
        personId = ledger_person_id,
        amount = amount,
        description = description,
        date = date,
        time = time,
        type = when (type) {
            "SENT" -> TransactionType.SENT
            "RECEIVED" -> TransactionType.RECEIVED
            else -> TransactionType.SENT
        },
        account = account_name
    )
}

// Extension function to convert Color to hex string
private fun Color.toHexString(): String {
    val alpha = (alpha * 255).toInt()
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return "#%02X%02X%02X%02X".format(alpha, red, green, blue)
}
