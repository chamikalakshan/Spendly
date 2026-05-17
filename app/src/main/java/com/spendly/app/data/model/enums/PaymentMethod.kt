package com.spendly.app.data.model.enums

enum class PaymentMethod(val displayName: String) {
    CARD("Card"),
    CASH("Cash"),
    PICKME("PickMe"),
    UBEREATS("UberEats"),
    AUTODEBIT("Auto-debit")
}
