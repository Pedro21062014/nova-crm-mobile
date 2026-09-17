package com.example

import com.google.firebase.firestore.DocumentId

data class Merchant(
    val isSuperuser: Boolean = false,
    val storeConfig: Map<String, String> = emptyMap(),
    val subscription: Subscription = Subscription(),
    val aiUsage: AiUsage = AiUsage(),
    val createdAt: Long = System.currentTimeMillis()
)

data class Subscription(
    val plan: String = "free" // 'free', 'pro', 'enterprise'
)

data class AiUsage(
    val date: String = "",
    val count: Int = 0
)

data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val stock: Int = 0,
    val imageUrl: String = "",
    val images: List<String> = emptyList(),
    val secondaryImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val primaryImage: String
        get() {
            if (imageUrl.isNotBlank()) return imageUrl
            val firstImg = images.firstOrNull { it.isNotBlank() }
            if (!firstImg.isNullOrBlank()) return firstImg
            return secondaryImageUrl
        }
}

data class Coupon(
    @DocumentId val id: String = "",
    val code: String = "",
    val type: String = "percentage", // "percentage" or "fixed"
    val value: Double = 0.0,
    val active: Boolean = true
)

data class Order(
    @DocumentId val id: String = "",
    val customerEmail: String = "",
    val total: Double = 0.0,
    val status: String = "pending", // e.g., "concluído", "pendente", "cancelado"
    val createdAt: Long = System.currentTimeMillis()
)

data class Client(
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class NotificationItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val read: Boolean = false
)

// Chat model for Realtime Database
data class Message(
    var id: String = "",
    val sender: String = "", // "customer" or "merchant"
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class TypingStatus(
    val customer: Boolean = false,
    val merchant: Boolean = false
)
