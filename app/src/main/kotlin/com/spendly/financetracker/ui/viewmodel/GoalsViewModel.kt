package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.GoalDraft
import com.spendly.financetracker.data.model.SavingsGoal
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.GoalRepository
import com.spendly.financetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class GoalsUiState(
    val goals: List<SavingsGoal> = emptyList(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val availableBalanceCents: Long = 0L,
    val error: String? = null
) {
    val primaryGoal: SavingsGoal?
        get() = goals.firstOrNull { it.isPrimary } ?: goals.firstOrNull()
    val otherGoals: List<SavingsGoal>
        get() = goals.filter { it.id != primaryGoal?.id }
}

data class GoalMonthlySavingUi(
    val month: String,
    val amountCents: Long,
    val percent: Float
)

fun requiredMonthlySavingsCents(goal: SavingsGoal): Long {
    if (goal.remainingCents <= 0L) return 0L
    val monthsRemaining = monthsUntilDueDate(goal.dueDateMillis)
    return (goal.remainingCents + monthsRemaining - 1L) / monthsRemaining
}

fun goalMonthlySavingsData(goal: SavingsGoal): List<GoalMonthlySavingUi> {
    val labels = lastFiveMonthLabels()
    val max = goal.savedCents.coerceAtLeast(1L)
    return labels.mapIndexed { index, label ->
        val amount = if (index == labels.lastIndex) goal.savedCents else 0L
        GoalMonthlySavingUi(label, amount, (amount.toFloat() / max.toFloat()).coerceIn(0f, 1f))
    }
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, error = "Please log in again") }
        } else {
            viewModelScope.launch {
                goalRepository.observeGoals(uid)
                    .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                    .collect { goals -> _uiState.update { it.copy(goals = goals, isLoading = false) } }
            }
            viewModelScope.launch {
                transactionRepository.observeTransactions(uid)
                    .catch { e -> _uiState.update { it.copy(error = e.message) } }
                    .collect { transactions ->
                        _uiState.update { state ->
                            state.copy(availableBalanceCents = transactions.sumOf { it.signedAmountCents })
                        }
                    }
            }
        }
    }

    fun saveDraft(draft: GoalDraft, existing: SavingsGoal? = null): Boolean {
        val uid = authRepository.getCurrentUserId() ?: return fail("Please log in again")
        val targetCents = parseAmountCents(draft.targetAmount) ?: return fail("Enter a valid target amount")
        if (draft.title.isBlank()) return fail("Enter a goal name")
        val dueDate = draft.dueDateMillis ?: parseGoalDateMillis(draft.targetDate)
        if (dueDate <= 0L) return fail("Select a target date")
        val initialSaved = parseAmountCents(draft.initialSaved.ifBlank { "0" }) ?: 0L
        if (initialSaved > targetCents) return fail("Initial saved amount cannot exceed target amount")
        if (existing == null && initialSaved > _uiState.value.availableBalanceCents) {
            return fail("amount exceed total income")
        }
        val savedCents = existing?.savedCents ?: initialSaved
        val normalizedStatus = normalizedGoalStatus(draft.status, savedCents, targetCents)
        val goal = SavingsGoal(
            id = existing?.id.orEmpty(),
            userId = uid,
            title = draft.title.trim(),
            status = normalizedStatus,
            targetCents = targetCents,
            savedCents = savedCents,
            dueDateMillis = dueDate,
            category = draft.category,
            isPrimary = draft.isPrimary && normalizedStatus != "Done",
            createdAtMillis = existing?.createdAtMillis ?: 0L,
            initialSavedCents = existing?.initialSavedCents ?: initialSaved,
            defaultCurrency = draft.defaultCurrency,
            iconKey = draft.iconKey
        )
        viewModelScope.launch {
            goalRepository.saveGoal(goal)
                .onSuccess { _uiState.update { it.copy(isSaved = true, error = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
        return true
    }

    fun deleteGoal(id: String): Boolean {
        viewModelScope.launch {
            goalRepository.deleteGoal(id)
                .onSuccess { _uiState.update { it.copy(isDeleted = true, error = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
        return true
    }

    fun addSavings(id: String, amount: String): Boolean {
        val cents = parseAmountCents(amount) ?: return fail("Enter a valid savings amount")
        if (cents <= 0L) return fail("Enter a valid savings amount")
        val goal = _uiState.value.goals.firstOrNull { it.id == id }
        if (goal != null && cents > goal.remainingCents) return fail("Amount exceed target value")
        if (cents > _uiState.value.availableBalanceCents) return fail("amount exceed total income")
        viewModelScope.launch {
            goalRepository.addSavings(id, cents)
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
        return true
    }

    fun requiredMonthlySavingsCents(goal: SavingsGoal): Long {
        return com.spendly.financetracker.ui.viewmodel.requiredMonthlySavingsCents(goal)
    }

    fun monthlySavingsData(goal: SavingsGoal): List<GoalMonthlySavingUi> {
        return goalMonthlySavingsData(goal)
    }

    private fun fail(message: String): Boolean {
        _uiState.update { it.copy(error = message) }
        return false
    }

    private fun normalizedGoalStatus(status: String, savedCents: Long, targetCents: Long): String {
        if (targetCents > 0L && savedCents >= targetCents) return "Done"
        return when (status.lowercase()) {
            "on track", "tracking" -> "Tracking"
            "not on track", "stopped" -> "Stopped"
            "done" -> "Done"
            else -> "Tracking"
        }
    }

    private fun parseAmountCents(input: String): Long? {
        val n = input.trim()
        if (n.isBlank()) return 0L
        if (!Regex("^\\d+([.]\\d{1,2})?$").matches(n)) return null
        val parts = n.split(".")
        val whole = parts[0].toLongOrNull() ?: return null
        val cents = parts.getOrNull(1)?.padEnd(2, '0') ?: "00"
        return whole * 100L + (cents.toLongOrNull() ?: return null)
    }

    private fun parseGoalDateMillis(value: String): Long {
        val patterns = listOf("yyyy-MM", "MMM d, yyyy", "MMM yyyy", "MMMM yyyy")
        return patterns.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }
                    .parse(value)
                    ?.time
            }.getOrNull()
        } ?: Date().time
    }

}

private fun monthsUntilDueDate(dueDateMillis: Long): Long {
    if (dueDateMillis <= 0L) return 12L
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
    val monthDelta = (target.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12 +
        (target.get(Calendar.MONTH) - now.get(Calendar.MONTH))
    val dayAdjustment = if (target.get(Calendar.DAY_OF_MONTH) > now.get(Calendar.DAY_OF_MONTH)) 1 else 0
    return (monthDelta + dayAdjustment).coerceAtLeast(1).toLong()
}

private fun lastFiveMonthLabels(): List<String> {
    val formatter = SimpleDateFormat("MMM", Locale.getDefault())
    val calendar = Calendar.getInstance()
    return (4 downTo 0).map { offset ->
        val monthCalendar = calendar.clone() as Calendar
        monthCalendar.add(Calendar.MONTH, -offset)
        formatter.format(Date(monthCalendar.timeInMillis))
    }
}
