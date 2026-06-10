package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.AppNotification
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.BudgetRepository
import com.spendly.financetracker.data.repository.NotificationRepository
import com.spendly.financetracker.data.repository.RecurringTransactionRepository
import com.spendly.financetracker.data.service.AppNotificationDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val unreadCount: Int get() = notifications.count { !it.isRead }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    private val budgetRepository: BudgetRepository,
    private val recurringRepository: RecurringTransactionRepository,
    private val notificationDispatcher: AppNotificationDispatcher
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val uid: String? = authRepository.getCurrentUserId()

    init {
        val userId = uid
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, error = "Please log in again") }
        } else {
            observe(userId)
            generateLocalNotifications(userId)
        }
    }

    fun markAllRead() {
        val userId = uid ?: return
        viewModelScope.launch {
            notificationRepository.markAllRead(userId)
            notificationDispatcher.cancelAll()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
            notificationDispatcher.cancel(id)
        }
    }

    fun postUnreadToSystem() {
        _uiState.value.notifications
            .filterNot(AppNotification::isRead)
            .forEach(notificationDispatcher::post)
    }

    private fun observe(userId: String) {
        viewModelScope.launch {
            notificationRepository.observeNotifications(userId)
                .catch { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
                .collect { rows -> _uiState.update { it.copy(notifications = rows, isLoading = false) } }
        }
    }

    private fun generateLocalNotifications(userId: String) {
        viewModelScope.launch {
            combine(
                budgetRepository.observeBudgets(userId),
                recurringRepository.observeRules(userId)
            ) { budgets, rules -> budgets to rules }
                .catch { }
                .collect { (budgets, rules) ->
                    val now = System.currentTimeMillis()
                    budgets.filter { it.deletedAtMillis == null }.take(3).forEach { budget ->
                        publishIfNew(
                            AppNotification(
                                id = "budget-${budget.id}",
                                userId = userId,
                                title = "Budget active",
                                body = "${budget.category} budget is ready for ${formatMonth(budget.monthStartMillis)}.",
                                type = "BUDGET",
                                isRead = false,
                                createdAtMillis = budget.updatedAtMillis.takeIf { it > 0L } ?: now
                            )
                        )
                    }
                    rules.filter { it.isActive && it.deletedAtMillis == null }
                        .sortedBy { it.nextRunDateMillis }
                        .take(2)
                        .forEach { rule ->
                            publishIfNew(
                                AppNotification(
                                    id = "recurring-${rule.id}",
                                    userId = userId,
                                    title = "Upcoming recurring item",
                                    body = "${rule.name} is scheduled for ${formatDate(rule.nextRunDateMillis)}.",
                                    type = "RECURRING",
                                    isRead = false,
                                    createdAtMillis = rule.updatedAtMillis.takeIf { it > 0L } ?: now
                                )
                            )
                        }
                }
        }
    }

    private fun formatMonth(timeMillis: Long): String =
        SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(timeMillis)

    private fun formatDate(timeMillis: Long): String =
        SimpleDateFormat("MMM d", Locale.getDefault()).format(timeMillis)

    private suspend fun publishIfNew(notification: AppNotification) {
        if (notificationRepository.getNotification(notification.id) != null) return
        notificationRepository.upsert(notification)
        notificationDispatcher.post(notification)
    }
}
