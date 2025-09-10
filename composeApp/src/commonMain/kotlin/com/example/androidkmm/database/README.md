# Database Implementation for Categories

## Overview
This implementation provides a local SQLite database for managing categories in the Android KMM app. The database stores both default and custom categories with their associated icons and colors.

## Database Schema

### CategoryEntity
- `id`: String (Primary Key)
- `name`: String (Category name)
- `iconName`: String (Icon identifier for rendering)
- `colorValue`: Long (Color stored as ARGB Long)
- `type`: String ("EXPENSE" or "INCOME")
- `isCustom`: Boolean (Whether it's a custom category)
- `createdAt`: Long (Timestamp)

## Default Categories

### Expense Categories (13 categories):
1. Food → Restaurant icon
2. Transport → DirectionsCar icon
3. Housing → Home icon
4. Utilities → Lightbulb icon
5. Health → LocalHospital icon
6. Shopping → ShoppingCart icon
7. Entertainment → Movie icon
8. Travel → Flight icon
9. Education → School icon
10. Savings → Savings icon
11. Loans → AccountBalance icon
12. Gifts → CardGiftcard icon
13. Others → Category icon

### Income Categories (6 categories):
1. Salary → AttachMoney icon
2. Business → Business icon
3. Investment → TrendingUp icon
4. Rental Income → Home icon
5. Gift → CardGiftcard icon
6. Bonus → Stars icon

## Key Components

### 1. CategoryDao
- Database access object with CRUD operations
- Flow-based queries for reactive UI updates
- Separate queries for different category types

### 2. CategoryRepository
- Business logic layer
- Converts between Category and CategoryEntity
- Handles icon name mapping

### 3. DatabaseManager
- Composable-friendly database manager
- Provides coroutine-based operations
- Handles success/error callbacks

### 4. AppDatabase
- Room database configuration
- Automatic population of default categories
- Type converters for Color handling

## Usage

### In ProfileScreen:
```kotlin
val databaseManager = rememberDatabaseManager()
val expenseCategories = databaseManager.getCategoriesByType(CategoryType.EXPENSE).collectAsState(initial = emptyList())
val incomeCategories = databaseManager.getCategoriesByType(CategoryType.INCOME).collectAsState(initial = emptyList())
val customCategories = databaseManager.getCustomCategories().collectAsState(initial = emptyList())
```

### Adding Custom Categories:
```kotlin
databaseManager.addCategory(category) {
    // Success callback
    showAddCategorySheet = false
}
```

### Deleting Categories:
```kotlin
databaseManager.deleteCategory(category)
```

## Features

1. **Automatic Initialization**: Database is populated with default categories on first run
2. **Reactive UI**: Uses Flow for real-time UI updates
3. **Icon Persistence**: Icons are stored as strings and mapped back to ImageVector
4. **Color Persistence**: Colors are stored as ARGB Long values
5. **Type Safety**: Strong typing with enums for category types
6. **Error Handling**: Proper error handling in database operations

## Database Initialization

The database is automatically initialized when the MainScreen is loaded. The `InitializeDatabase()` composable ensures that:
- Database is created if it doesn't exist
- Default categories are populated if the database is empty
- All operations are performed on background threads

## Icon Mapping

Icons are stored as string identifiers and mapped to actual ImageVector objects using helper functions:
- `getIconByName()`: Converts string to ImageVector
- `getIconName()`: Converts ImageVector to string

This approach ensures that icons can be persisted in the database while maintaining type safety in the UI layer.
