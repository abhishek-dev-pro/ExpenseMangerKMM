package com.example.androidkmm.models

import kotlinx.datetime.LocalDate

data class Goal(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: LocalDate? = null,
    val isRecurring: Boolean = false,
    val monthlyAmount: Double? = null,
    val icon: String = "target",
    val color: String = "blue",
    val priority: String = "medium",
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val progressPercentage: Int = 0,
    val remainingAmount: Double = 0.0,
    val status: String = "on track"
)

enum class GoalIcon(val iconName: String, val displayName: String) {
    TARGET("target", "Target"),
    DOLLAR("dollar", "Dollar"),
    EXCLAMATION("exclamation", "Exclamation"),
    GAME_CONTROLLER("game_controller", "Game"),
    SHOPPING_CART("shopping_cart", "Shopping"),
    CAR("car", "Car"),
    HOUSE("house", "House"),
    PAPER_AIRPLANE("paper_airplane", "Travel"),
    HEART("heart", "Health"),
    GRADUATION_CAP("graduation_cap", "Education"),
    MOBILE_PHONE("mobile_phone", "Phone"),
    COFFEE_CUP("coffee_cup", "Coffee"),
    GIFT_BOX("gift_box", "Gift")
}

enum class GoalColor(val colorName: String, val hexValue: String) {
    RED("red", "#FF5722"),
    ORANGE("orange", "#FF9800"),
    YELLOW("yellow", "#FFC107"),
    GREEN("green", "#4CAF50"),
    TEAL("teal", "#009688"),
    BLUE("blue", "#2196F3"),
    PURPLE("purple", "#9C27B0"),
    MAGENTA("magenta", "#E91E63"),
    PINK("pink", "#E91E63"),
    GRAY("gray", "#607D8B")
}

enum class GoalPriority(val priorityName: String, val displayName: String, val dotColor: String) {
    HIGH("high", "High", "#F44336"),
    MEDIUM("medium", "Medium", "#FF9800"),
    LOW("low", "Low", "#4CAF50")
}
