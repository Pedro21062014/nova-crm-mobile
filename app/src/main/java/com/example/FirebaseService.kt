package com.example

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseService {
    private const val TAG = "FirebaseService"

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var rtdb: FirebaseDatabase

    // Dynamic initialisation using the client's provided Firebase console credentials
    fun initialize(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyABooLzKOpSqcmQ5VjAydDgbgiUvpMEMh8")
                    .setApplicationId("1:786463592188:web:b9d12600bb856189515179")
                    .setDatabaseUrl("https://crm-e-vendas-default-rtdb.firebaseio.com")
                    .setProjectId("crm-e-vendas")
                    .setStorageBucket("crm-e-vendas.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "Firebase initialized dynamically with explicit options")
            }
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            rtdb = FirebaseDatabase.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing dynamic Firebase: ", e)
        }
    }

    // AUTH ENTITY
    fun getAuth(): FirebaseAuth = auth
    fun getFirestore(): FirebaseFirestore = firestore
    fun getRtdb(): FirebaseDatabase = rtdb

    fun getCurrentMerchantId(): String? {
        return auth.currentUser?.uid
    }

    // MERCHANT PROFILE / SUBSCRIPTION / AI LIMIT CHECK
    suspend fun getMerchantProfile(merchantId: String): Merchant {
        return try {
            val doc = firestore.collection("merchants").document(merchantId).get().await()
            if (doc.exists()) {
                val isSuperuser = doc.getBoolean("isSuperuser") ?: false
                val storeConfig = (doc.get("storeConfig") as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { it.value.toString() } ?: emptyMap()
                
                // Parse subscription
                val subMap = doc.get("subscription") as? Map<*, *>
                val plan = subMap?.get("plan")?.toString()
                    ?: subMap?.get("planId")?.toString()
                    ?: doc.getString("plan")
                    ?: doc.getString("planId")
                    ?: "free"
                val subscription = Subscription(plan = plan)
                
                // Parse aiUsage
                val aiMap = doc.get("aiUsage") as? Map<*, *>
                val dateStr = aiMap?.get("date")?.toString() ?: ""
                val countNum = (aiMap?.get("count") as? Number)?.toInt() ?: 0
                val aiUsage = AiUsage(date = dateStr, count = countNum)
                
                Merchant(
                    isSuperuser = isSuperuser,
                    storeConfig = storeConfig,
                    subscription = subscription,
                    aiUsage = aiUsage,
                    createdAt = getLongOrTimestamp(doc, "createdAt")
                )
            } else {
                // Initialize default profile
                val defaultMerchant = Merchant(
                    isSuperuser = false,
                    subscription = Subscription(plan = "free"),
                    aiUsage = AiUsage(date = getCurrentDateString(), count = 0),
                    createdAt = System.currentTimeMillis()
                )
                firestore.collection("merchants").document(merchantId).set(defaultMerchant).await()
                defaultMerchant
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching merchant profile", e)
            Merchant(subscription = Subscription("free"), aiUsage = AiUsage(getCurrentDateString(), 0))
        }
    }

    suspend fun incrementAiCount(merchantId: String, currentUsage: AiUsage): AiUsage {
        val today = getCurrentDateString()
        val newCount = if (currentUsage.date == today) currentUsage.count + 1 else 1
        val updatedUsage = AiUsage(date = today, count = newCount)
        try {
            firestore.collection("merchants").document(merchantId)
                .update("aiUsage", updatedUsage)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to increment AI Usage: ", e)
            // If update fails (e.g., due to field structure), rewrite merchant
            try {
                firestore.collection("merchants").document(merchantId)
                    .set(mapOf("aiUsage" to updatedUsage), com.google.firebase.firestore.SetOptions.merge())
                    .await()
            } catch (ex: Exception) {
                Log.e(TAG, "Failed second attempt merge AI Usage", ex)
            }
        }
        return updatedUsage
    }

    suspend fun upgradeMerchantToPro(merchantId: String) {
        try {
            firestore.collection("merchants").document(merchantId)
                .update("subscription.plan", "pro")
                .await()
        } catch (e: Exception) {
            firestore.collection("merchants").document(merchantId)
                .set(mapOf("subscription" to mapOf("plan" to "pro")), com.google.firebase.firestore.SetOptions.merge())
                .await()
        }
    }

    // PRODUCTS ENDPOINTS
    fun streamProducts(merchantId: String): Flow<List<Product>> = callbackFlow {
        val listener = firestore.collection("merchants").document(merchantId)
            .collection("products")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val name = doc.getString("name") ?: ""
                    val price = doc.getDouble("price") ?: 0.0
                    val category = doc.getString("category") ?: ""
                    val stock = doc.getLong("stock")?.toInt() ?: 0
                    val imageUrl = doc.getString("imageUrl")
                        ?: doc.getString("image")
                        ?: doc.getString("photo")
                        ?: ""
                    val secondaryImageUrl = doc.getString("secondaryImageUrl") ?: ""
                    val imagesList = (doc.get("images") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    val createdAt = getLongOrTimestamp(doc, "createdAt")
                    Product(
                        id = doc.id,
                        name = name,
                        price = price,
                        category = category,
                        stock = stock,
                        imageUrl = imageUrl,
                        images = imagesList,
                        secondaryImageUrl = secondaryImageUrl,
                        createdAt = createdAt
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addProduct(merchantId: String, name: String, price: Double, category: String, stock: Int, imageUrl: String = "") {
        val productMap = mapOf(
            "name" to name,
            "price" to price,
            "category" to category,
            "stock" to stock,
            "imageUrl" to imageUrl,
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("merchants").document(merchantId)
            .collection("products")
            .add(productMap)
            .await()
    }

    suspend fun updateProductStock(merchantId: String, productId: String, newStock: Int) {
        if (newStock < 0) return
        firestore.collection("merchants").document(merchantId)
            .collection("products").document(productId)
            .update("stock", newStock)
            .await()
    }

    suspend fun updateProduct(merchantId: String, productId: String, name: String, price: Double, category: String, stock: Int, imageUrl: String) {
        val updateMap = mapOf(
            "name" to name,
            "price" to price,
            "category" to category,
            "stock" to stock,
            "imageUrl" to imageUrl
        )
        firestore.collection("merchants").document(merchantId)
            .collection("products").document(productId)
            .update(updateMap)
            .await()
    }

    suspend fun deleteProduct(merchantId: String, productId: String) {
        firestore.collection("merchants").document(merchantId)
            .collection("products").document(productId)
            .delete()
            .await()
    }

    // ORDERS ENDPOINTS
    fun streamOrders(merchantId: String): Flow<List<Order>> = callbackFlow {
        val listener = firestore.collection("merchants").document(merchantId)
            .collection("orders")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val customerEmail = doc.getString("customerEmail") ?: ""
                    val total = doc.getDouble("total") ?: 0.0
                    val status = doc.getString("status") ?: "pending"
                    val createdAt = getLongOrTimestamp(doc, "createdAt")
                    Order(id = doc.id, customerEmail = customerEmail, total = total, status = status, createdAt = createdAt)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // CLIENTS ENDPOINTS
    fun streamClients(merchantId: String): Flow<List<Client>> = callbackFlow {
        val listener = firestore.collection("merchants").document(merchantId)
            .collection("clients")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val createdAt = getLongOrTimestamp(doc, "createdAt")
                    Client(id = doc.id, name = name, email = email, phone = phone, createdAt = createdAt)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addClient(merchantId: String, name: String, email: String, phone: String) {
        val clientMap = mapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("merchants").document(merchantId)
            .collection("clients")
            .add(clientMap)
            .await()
    }

    // CHATS ENDPOINTS (REALTIME DATABASE)
    fun streamChatMessages(merchantId: String, clientId: String): Flow<List<Message>> = callbackFlow {
        val messagesRef = rtdb.getReference("merchants/$merchantId/chats/$clientId/messages")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val sender = child.child("sender").getValue(String::class.java) ?: ""
                    val text = child.child("text").getValue(String::class.java) ?: ""
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                    val key = child.key ?: ""
                    list.add(Message(id = key, sender = sender, text = text, timestamp = timestamp))
                }
                // Sort by timestamp
                list.sortBy { it.timestamp }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    fun sendChatMessage(merchantId: String, clientId: String, text: String, sender: String = "merchant") {
        val messagesRef = rtdb.getReference("merchants/$merchantId/chats/$clientId/messages")
        val newMessageRef = messagesRef.push()
        val message = Message(id = newMessageRef.key ?: "", sender = sender, text = text, timestamp = System.currentTimeMillis())
        newMessageRef.setValue(message)
    }

    fun streamTypingStatus(merchantId: String, clientId: String): Flow<TypingStatus> = callbackFlow {
        val typingRef = rtdb.getReference("merchants/$merchantId/chats/$clientId/typing")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customer = snapshot.child("customer").getValue(Boolean::class.java) ?: false
                val merchant = snapshot.child("merchant").getValue(Boolean::class.java) ?: false
                trySend(TypingStatus(customer = customer, merchant = merchant))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        typingRef.addValueEventListener(listener)
        awaitClose { typingRef.removeEventListener(listener) }
    }

    fun updateMerchantTyping(merchantId: String, clientId: String, isTyping: Boolean) {
        val typingRef = rtdb.getReference("merchants/$merchantId/chats/$clientId/typing/merchant")
        typingRef.setValue(isTyping)
    }

    // HELPER FUNCTIONS
    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getLongOrTimestamp(doc: com.google.firebase.firestore.DocumentSnapshot, field: String): Long {
        val value = doc.get(field) ?: return System.currentTimeMillis()
        if (value is Timestamp) {
            return value.seconds * 1000 + value.nanoseconds / 1000000
        }
        if (value is Number) {
            return value.toLong()
        }
        return System.currentTimeMillis()
    }
}
