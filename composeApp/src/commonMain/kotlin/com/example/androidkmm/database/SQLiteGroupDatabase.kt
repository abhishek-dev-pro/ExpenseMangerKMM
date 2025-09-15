package com.example.androidkmm.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.androidkmm.database.CategoryDatabase
import com.example.androidkmm.models.Group
import com.example.androidkmm.models.GroupMember
// import com.example.androidkmm.utils.formatDouble // Not needed for String.format
import com.example.androidkmm.models.GroupExpense
import com.example.androidkmm.models.GroupExpenseSplit
import com.example.androidkmm.models.SplitType
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

class SQLiteGroupDatabase(
    private val database: CategoryDatabase
) {
    
    // Group Operations
    fun getAllGroups(): Flow<List<Group>> {
        return database.categoryDatabaseQueries.selectAllGroups()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { groups -> groups.map { it.toGroup() } }
    }
    
    suspend fun getGroupById(id: String): Group? {
        return withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.selectGroupById(id).executeAsOneOrNull()?.toGroup()
        }
    }
    
    suspend fun getGroupByName(name: String): Group? {
        return withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.selectGroupByName(name).executeAsOneOrNull()?.toGroup()
        }
    }
    
    suspend fun insertGroup(group: Group) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.insertGroup(
                id = group.id,
                name = group.name,
                description = group.description,
                color_hex = group.color.toHexString(),
                total_spent = group.totalSpent,
                member_count = group.memberCount.toLong()
            )
        }
    }
    
    suspend fun updateGroup(group: Group) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.updateGroup(
                name = group.name,
                description = group.description,
                color_hex = group.color.toHexString(),
                total_spent = group.totalSpent,
                member_count = group.memberCount.toLong(),
                id = group.id
            )
        }
    }
    
    suspend fun deleteGroup(id: String) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.deleteGroup(id)
        }
    }
    
    suspend fun getGroupCount(): Long {
        return withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.getGroupCount().executeAsOne()
        }
    }
    
    // Group Member Operations
    fun getAllGroupMembers(): Flow<List<GroupMember>> {
        return database.categoryDatabaseQueries.selectAllGroupMembers()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { members -> members.map { it.toGroupMember() } }
    }
    
    fun getGroupMembersByGroup(groupId: String): Flow<List<GroupMember>> {
        return database.categoryDatabaseQueries.selectGroupMembersByGroup(groupId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { members -> members.map { it.toGroupMember() } }
    }
    
    suspend fun getGroupMemberById(id: String): GroupMember? {
        return withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.selectGroupMemberById(id).executeAsOneOrNull()?.toGroupMember()
        }
    }
    
    suspend fun getGroupMemberByName(name: String, groupId: String): GroupMember? {
        return withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.selectGroupMemberByName(name, groupId).executeAsOneOrNull()?.toGroupMember()
        }
    }
    
    suspend fun insertGroupMember(member: GroupMember) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.insertGroupMember(
                id = member.id,
                group_id = member.groupId,
                name = member.name,
                email = member.email,
                phone = member.phone,
                avatar_color_hex = member.avatarColor.toHexString(),
                balance = member.balance,
                total_paid = member.totalPaid,
                total_owed = member.totalOwed
            )
        }
    }
    
    suspend fun updateGroupMember(member: GroupMember) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.updateGroupMember(
                name = member.name,
                email = member.email,
                phone = member.phone,
                avatar_color_hex = member.avatarColor.toHexString(),
                balance = member.balance,
                total_paid = member.totalPaid,
                total_owed = member.totalOwed,
                id = member.id
            )
        }
    }
    
    suspend fun deleteGroupMember(id: String) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.deleteGroupMember(id)
        }
    }
    
    suspend fun deleteGroupMembersByGroup(groupId: String) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.deleteGroupMembersByGroup(groupId)
        }
    }
    
    suspend fun getGroupMemberCount(): Long {
        return withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.getGroupMemberCount().executeAsOne()
        }
    }
    
    suspend fun getGroupMemberCountByGroup(groupId: String): Long {
        return withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.getGroupMemberCountByGroup(groupId).executeAsOne()
        }
    }
    
    // Group Expense Operations
    fun getAllGroupExpenses(): Flow<List<GroupExpense>> {
        return database.categoryDatabaseQueries.selectAllGroupExpenses()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { expenses -> expenses.map { it.toGroupExpense() } }
    }
    
    fun getGroupExpensesByGroup(groupId: String): Flow<List<GroupExpense>> {
        return database.categoryDatabaseQueries.selectGroupExpensesByGroup(groupId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { expenses -> expenses.map { it.toGroupExpense() } }
    }
    
    suspend fun getGroupExpenseById(id: String): GroupExpense? {
        return withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.selectGroupExpenseById(id).executeAsOneOrNull()?.toGroupExpense()
        }
    }
    
    suspend fun insertGroupExpense(expense: GroupExpense) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.insertGroupExpense(
                id = expense.id,
                group_id = expense.groupId,
                paid_by = expense.paidBy,
                amount = expense.amount,
                description = expense.description,
                category = expense.category,
                date = expense.date,
                time = expense.time,
                split_type = expense.splitType.name,
                split_details = expense.splitDetails
            )
        }
    }
    
    suspend fun updateGroupExpense(expense: GroupExpense) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.updateGroupExpense(
                paid_by = expense.paidBy,
                amount = expense.amount,
                description = expense.description,
                category = expense.category,
                date = expense.date,
                time = expense.time,
                split_type = expense.splitType.name,
                split_details = expense.splitDetails,
                id = expense.id
            )
        }
    }
    
    suspend fun deleteGroupExpense(id: String) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.deleteGroupExpense(id)
        }
    }
    
    suspend fun deleteGroupExpensesByGroup(groupId: String) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.deleteGroupExpensesByGroup(groupId)
        }
    }
    
    suspend fun getGroupExpenseCount(): Long {
        return withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.getGroupExpenseCount().executeAsOne()
        }
    }
    
    suspend fun getGroupExpenseCountByGroup(groupId: String): Long {
        return withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.getGroupExpenseCountByGroup(groupId).executeAsOne()
        }
    }
    
    // Group Expense Split Operations
    fun getAllGroupExpenseSplits(): Flow<List<GroupExpenseSplit>> {
        return database.categoryDatabaseQueries.selectAllGroupExpenseSplits()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { splits -> splits.map { it.toGroupExpenseSplit() } }
    }
    
    fun getGroupExpenseSplitsByExpense(expenseId: String): Flow<List<GroupExpenseSplit>> {
        return database.categoryDatabaseQueries.selectGroupExpenseSplitsByExpense(expenseId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { splits -> splits.map { it.toGroupExpenseSplit() } }
    }
    
    fun getGroupExpenseSplitsByMember(memberId: String): Flow<List<GroupExpenseSplit>> {
        return database.categoryDatabaseQueries.selectGroupExpenseSplitsByMember(memberId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { splits -> splits.map { it.toGroupExpenseSplit() } }
    }
    
    suspend fun insertGroupExpenseSplit(split: GroupExpenseSplit) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.insertGroupExpenseSplit(
                id = split.id,
                expense_id = split.expenseId,
                member_id = split.memberId,
                amount = split.amount,
                is_paid = if (split.isPaid) 1 else 0
            )
        }
    }
    
    suspend fun updateGroupExpenseSplit(split: GroupExpenseSplit) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.updateGroupExpenseSplit(
                amount = split.amount,
                is_paid = if (split.isPaid) 1 else 0,
                id = split.id
            )
        }
    }
    
    suspend fun deleteGroupExpenseSplit(id: String) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.deleteGroupExpenseSplit(id)
        }
    }
    
    suspend fun deleteGroupExpenseSplitsByExpense(expenseId: String) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.deleteGroupExpenseSplitsByExpense(expenseId)
        }
    }
    
    suspend fun deleteGroupExpenseSplitsByMember(memberId: String) {
        withContext(Dispatchers.Default) {
            database.categoryDatabaseQueries.deleteGroupExpenseSplitsByMember(memberId)
        }
    }
    
    // Complex Operations
    suspend fun addGroupWithMembers(group: Group, members: List<GroupMember>) {
        withContext(Dispatchers.Default) {
            database.transaction {
                database.categoryDatabaseQueries.insertGroup(
                    id = group.id,
                    name = group.name,
                    description = group.description,
                    color_hex = group.color.toHexString(),
                    total_spent = group.totalSpent,
                    member_count = group.memberCount.toLong()
                )
                members.forEach { member ->
                    database.categoryDatabaseQueries.insertGroupMember(
                        id = member.id,
                        group_id = member.groupId,
                        name = member.name,
                        email = member.email,
                        phone = member.phone,
                        avatar_color_hex = member.avatarColor.toHexString(),
                        balance = member.balance,
                        total_paid = member.totalPaid,
                        total_owed = member.totalOwed
                    )
                }
            }
        }
    }
    
    suspend fun addExpenseWithSplits(expense: GroupExpense, splits: List<GroupExpenseSplit>) {
        withContext(Dispatchers.Default) {
            database.transaction {
                database.categoryDatabaseQueries.insertGroupExpense(
                    id = expense.id,
                    group_id = expense.groupId,
                    paid_by = expense.paidBy,
                    amount = expense.amount,
                    description = expense.description,
                    category = expense.category,
                    date = expense.date,
                    time = expense.time,
                    split_type = expense.splitType.name,
                    split_details = expense.splitDetails
                )
                splits.forEach { split ->
                    database.categoryDatabaseQueries.insertGroupExpenseSplit(
                        id = split.id,
                        expense_id = split.expenseId,
                        member_id = split.memberId,
                        amount = split.amount,
                        is_paid = if (split.isPaid) 1 else 0
                    )
                }
            }
        }
    }
    
    suspend fun deleteGroupWithAllData(groupId: String) {
        withContext(Dispatchers.Default) {
            database.transaction {
                // Delete all related data (cascading deletes will handle most of this)
                database.categoryDatabaseQueries.deleteGroup(groupId)
            }
        }
    }
}

// Extension functions to convert database entities to models
private fun com.example.androidkmm.database.Groups.toGroup(): Group {
    return Group(
        id = id,
        name = name,
        description = description,
        color = parseColorHex(color_hex),
        createdAt = created_at,
        totalSpent = total_spent,
        memberCount = member_count.toInt()
    )
}

private fun com.example.androidkmm.database.Group_members.toGroupMember(): GroupMember {
    return GroupMember(
        id = id,
        groupId = group_id,
        name = name,
        email = email,
        phone = phone,
        avatarColor = parseColorHex(avatar_color_hex),
        balance = balance,
        totalPaid = total_paid,
        totalOwed = total_owed
    )
}

private fun com.example.androidkmm.database.Group_expenses.toGroupExpense(): GroupExpense {
    return GroupExpense(
        id = id,
        groupId = group_id,
        paidBy = paid_by,
        amount = amount,
        description = description,
        category = category,
        date = date,
        time = time,
        splitType = try { SplitType.valueOf(split_type) } catch (e: Exception) { SplitType.EQUAL },
        splitDetails = split_details,
        createdAt = created_at
    )
}

private fun com.example.androidkmm.database.Group_expense_splits.toGroupExpenseSplit(): GroupExpenseSplit {
    return GroupExpenseSplit(
        id = id,
        expenseId = expense_id,
        memberId = member_id,
        amount = amount,
        isPaid = is_paid == 1L
    )
}

private fun Color.toHexString(): String {
    val alpha = (alpha * 255).toInt()
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return "#${alpha.toString(16).padStart(2, '0')}${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}${blue.toString(16).padStart(2, '0')}"
}
