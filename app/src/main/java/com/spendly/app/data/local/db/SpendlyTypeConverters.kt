package com.spendly.app.data.local.db

import androidx.room.TypeConverter
import com.spendly.app.data.model.enums.*

class SpendlyTypeConverters {
    @TypeConverter
    fun fromIncomeSource(value: IncomeSource): String = value.name

    @TypeConverter
    fun toIncomeSource(value: String): IncomeSource = enumValueOf<IncomeSource>(value)

    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory): String = value.name

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory = enumValueOf<ExpenseCategory>(value)

    @TypeConverter
    fun fromExpenseType(value: ExpenseType): String = value.name

    @TypeConverter
    fun toExpenseType(value: String): ExpenseType = enumValueOf<ExpenseType>(value)

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = enumValueOf<PaymentMethod>(value)

    @TypeConverter
    fun fromCurrency(value: Currency): String = value.name

    @TypeConverter
    fun toCurrency(value: String): Currency = enumValueOf<Currency>(value)

    @TypeConverter
    fun fromInvoiceStatus(value: InvoiceStatus?): String? = value?.name

    @TypeConverter
    fun toInvoiceStatus(value: String?): InvoiceStatus? = value?.let { enumValueOf<InvoiceStatus>(it) }
}
