package com.example

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 1. AUTH VIEW MODEL
sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Success(val uid: String) : AuthState
    data class Error(val message: String) : AuthState
}

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _resetMessage = MutableStateFlow<String?>(null)
    val resetMessage: StateFlow<String?> = _resetMessage.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        val uid = FirebaseService.getCurrentMerchantId()
        if (uid != null) {
            _authState.value = AuthState.Success(uid)
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun clearFeedback() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
        _resetMessage.value = null
    }

    fun setValidationError(message: String) {
        _resetMessage.value = null
        _authState.value = AuthState.Error(message)
    }

    fun login(email: String, password: String) {
        _resetMessage.value = null
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Preencha e-mail e senha para entrar.")
            return
        }
        _authState.value = AuthState.Loading
        FirebaseService.getAuth().signInWithEmailAndPassword(email.trim(), password.trim())
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                _authState.value = AuthState.Success(uid)
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(mapFirebaseAuthError(exception, isSignUp = false))
            }
    }

    fun signUp(email: String, password: String, confirmPassword: String = password) {
        _resetMessage.value = null
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Preencha todos os campos para criar sua conta.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("A senha deve ter pelo menos 6 caracteres.")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("As senhas informadas não coincidem.")
            return
        }
        _authState.value = AuthState.Loading
        FirebaseService.getAuth().createUserWithEmailAndPassword(email.trim(), password.trim())
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                _authState.value = AuthState.Success(uid)
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(mapFirebaseAuthError(exception, isSignUp = true))
            }
    }

    fun sendPasswordReset(email: String) {
        val trimmed = email.trim()
        if (trimmed.isBlank()) {
            _authState.value = AuthState.Error("Informe seu e-mail para recuperar a senha.")
            return
        }
        _authState.value = AuthState.Loading
        FirebaseService.getAuth().sendPasswordResetEmail(trimmed)
            .addOnSuccessListener {
                _authState.value = AuthState.Idle
                _resetMessage.value = "Enviamos um link de redefinição para $trimmed."
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(mapFirebaseAuthError(exception, isSignUp = false))
            }
    }

    fun signOut() {
        FirebaseService.getAuth().signOut()
        _authState.value = AuthState.Idle
        _resetMessage.value = null
    }

    private fun mapFirebaseAuthError(exception: Exception, isSignUp: Boolean): String {
        val raw = exception.localizedMessage.orEmpty().lowercase()
        return when {
            "badly formatted" in raw || "invalid-email" in raw ->
                "Formato de e-mail inválido."
            "email-already-in-use" in raw || "already in use" in raw ->
                "Este e-mail já está cadastrado. Tente entrar."
            "weak-password" in raw || "at least 6" in raw ->
                "Escolha uma senha com pelo menos 6 caracteres."
            "wrong-password" in raw || "invalid-credential" in raw || "incorrect" in raw || "user-not-found" in raw || "no user record" in raw ->
                "E-mail ou senha incorretos."
            "network" in raw ->
                "Sem conexão com a internet. Verifique sua rede."
            "too-many-requests" in raw ->
                "Muitas tentativas seguidas. Aguarde um instante."
            else -> exception.localizedMessage ?: if (isSignUp) "Falha ao realizar cadastro" else "Falha ao realizar login"
        }
    }
}

// 2. CRM VIEW MODEL (Dashboard, Products, Clients)
data class CrmUiState(
    val merchantProfile: Merchant = Merchant(),
    val products: List<Product> = emptyList(),
    val orders: List<Order> = emptyList(),
    val clients: List<Client> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class CrmViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CrmUiState())
    val uiState: StateFlow<CrmUiState> = _uiState.asStateFlow()

    private var activeMerchantId: String? = null
    private var streamJobs: List<Job> = emptyList()

    init {
        // Automatically hook streams if user already logged in
        val uid = FirebaseService.getCurrentMerchantId()
        if (uid != null) {
            loadMerchantData(uid)
        }
    }

    fun loadMerchantData(merchantId: String) {
        activeMerchantId = merchantId
        streamJobs.forEach { it.cancel() }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val productsJob = viewModelScope.launch {
            try {
                // Fetch Profile once
                val profile = FirebaseService.getMerchantProfile(merchantId)
                _uiState.update { it.copy(merchantProfile = profile) }

                // Collect Products Flow
                FirebaseService.streamProducts(merchantId).collectLatest { prods ->
                    _uiState.update { it.copy(products = prods, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage, isLoading = false) }
            }
        }

        val ordersJob = viewModelScope.launch {
            // Collect Orders Flow
            FirebaseService.streamOrders(merchantId).collectLatest { ords ->
                _uiState.update { it.copy(orders = ords) }
            }
        }

        val clientsJob = viewModelScope.launch {
            // Collect Clients Flow
            FirebaseService.streamClients(merchantId).collectLatest { clis ->
                _uiState.update { it.copy(clients = clis) }
            }
        }

        streamJobs = listOf(productsJob, ordersJob, clientsJob)
    }

    fun refreshDashboard() {
        val uid = activeMerchantId ?: FirebaseService.getCurrentMerchantId() ?: return
        if (_uiState.value.isRefreshing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                val profile = FirebaseService.getMerchantProfile(uid)
                _uiState.update { it.copy(merchantProfile = profile) }
                loadMerchantData(uid)
                delay(450)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage) }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    // Refresh Merchant Profile (useful after upgrade, AI calls, etc)
    fun refreshProfile() {
        val uid = activeMerchantId ?: return
        viewModelScope.launch {
            val profile = FirebaseService.getMerchantProfile(uid)
            _uiState.update { it.copy(merchantProfile = profile) }
        }
    }

    fun triggerUpgrade() {
        val uid = activeMerchantId ?: return
        viewModelScope.launch {
            FirebaseService.upgradeMerchantToPro(uid)
            refreshProfile()
        }
    }

    // Product actions
    fun addProduct(name: String, price: Double, category: String, stock: Int, imageUrl: String = "", onComplete: (Boolean) -> Unit) {
        val uid = activeMerchantId ?: return
        viewModelScope.launch {
            try {
                FirebaseService.addProduct(uid, name, price, category, stock, imageUrl)
                onComplete(true)
            } catch (e: Exception) {
                Log.e("CrmViewModel", "Failed to add product", e)
                onComplete(false)
            }
        }
    }

    fun adjustProductStock(product: Product, delta: Int) {
        val uid = activeMerchantId ?: return
        val newStock = product.stock + delta
        if (newStock < 0) return
        viewModelScope.launch {
            try {
                FirebaseService.updateProductStock(uid, product.id, newStock)
            } catch (e: Exception) {
                Log.e("CrmViewModel", "Failed to adjust stock", e)
            }
        }
    }

    fun updateProduct(product: Product, name: String, price: Double, category: String, stock: Int, imageUrl: String, onComplete: (Boolean) -> Unit) {
        val uid = activeMerchantId ?: return
        viewModelScope.launch {
            try {
                FirebaseService.updateProduct(uid, product.id, name, price, category, stock, imageUrl)
                onComplete(true)
            } catch (e: Exception) {
                Log.e("CrmViewModel", "Failed to update product", e)
                onComplete(false)
            }
        }
    }

    fun deleteProduct(productId: String, onComplete: (Boolean) -> Unit = {}) {
        val uid = activeMerchantId ?: return
        viewModelScope.launch {
            try {
                FirebaseService.deleteProduct(uid, productId)
                onComplete(true)
            } catch (e: Exception) {
                Log.e("CrmViewModel", "Failed to delete product", e)
                onComplete(false)
            }
        }
    }

    // Client actions
    fun addClient(name: String, email: String, phone: String, onComplete: (Boolean) -> Unit) {
        val uid = activeMerchantId ?: return
        viewModelScope.launch {
            try {
                FirebaseService.addClient(uid, name, email, phone)
                onComplete(true)
            } catch (e: Exception) {
                Log.e("CrmViewModel", "Failed to add client", e)
                onComplete(false)
            }
        }
    }

    // Derived State helper for Sales
    val totalSalesValue: StateFlow<Double> = _uiState.map { state ->
        state.orders
            .filter { it.status.lowercase() == "concluído" || it.status.lowercase() == "completed" }
            .sumOf { it.total }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}

// 3. AI CHAT VIEW MODEL (DeepSeek v4 Flash Integration with rate limit checking)
data class AiMessage(
    val sender: String, // "comerciante" (user) or "deepseek" (model)
    val text: String,
    val isThinking: Boolean = false
)

class AiChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage("deepseek", "Olá! Sou seu assistente de inteligência artificial de vendas (DeepSeek v4 Flash). Posso te ajudar a analisar suas métricas, planejar cupons e sugerir melhorias de vendas para a sua loja!")
        )
    )
    val messages: StateFlow<List<AiMessage>> = _messages.asStateFlow()

    private val _aiUsageText = MutableStateFlow("Mensagens da IA hoje: 0/10 (Plano Free)")
    val aiUsageText: StateFlow<String> = _aiUsageText.asStateFlow()

    private val _showLimitAlert = MutableStateFlow(false)
    val showLimitAlert: StateFlow<Boolean> = _showLimitAlert.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private var activeMerchantId: String? = null

    fun setMerchantId(merchantId: String) {
        activeMerchantId = merchantId
        updateUsageIndicator(merchantId)
    }

    private fun updateUsageIndicator(merchantId: String) {
        viewModelScope.launch {
            val profile = FirebaseService.getMerchantProfile(merchantId)
            val today = FirebaseService.getCurrentDateString()
            val plan = profile.subscription.plan.lowercase()
            
            val limit = if (plan == "pro" || plan == "enterprise") 45 else 10
            val count = if (profile.aiUsage.date == today) profile.aiUsage.count else 0
            
            _aiUsageText.value = "Mensagens da IA hoje: $count/$limit (Plano ${plan.replaceFirstChar { it.uppercase() }})"
        }
    }

    fun dismissLimitAlert() {
        _showLimitAlert.value = false
    }

    fun sendMessageToAi(text: String, crmState: CrmUiState, totalSales: Double) {
        val merchantId = activeMerchantId ?: return
        if (text.isBlank() || _isGenerating.value) return

        // Push User Message
        _messages.update { it + AiMessage("comerciante", text) }

        _isGenerating.value = true

        // Thinking Indicator
        _messages.update { it + AiMessage("deepseek", "Pensando...", isThinking = true) }

        viewModelScope.launch {
            try {
                // 1. Fetch latest profile
                val profile = FirebaseService.getMerchantProfile(merchantId)
                val today = FirebaseService.getCurrentDateString()
                val plan = profile.subscription.plan.lowercase()
                
                val limit = if (plan == "pro" || plan == "enterprise") 45 else 10
                val count = if (profile.aiUsage.date == today) profile.aiUsage.count else 0

                // 2. Limit Check
                if (count >= limit) {
                    // Exceeded limit. Remove "Thinking" indicator
                    _messages.update { old -> old.filter { !it.isThinking } }
                    _messages.update { it + AiMessage("deepseek", "Desculpe, o limite diário de mensagens da IA do seu plano ($limit chamadas) foi atingido. Faça o upgrade para o plano Pro para continuar aproveitando os insights!") }
                    _showLimitAlert.value = true
                    _isGenerating.value = false
                    return@launch
                }

                // 3. Increment usage count in Firestore
                val updatedUsage = FirebaseService.incrementAiCount(merchantId, profile.aiUsage)
                _aiUsageText.value = "Mensagens da IA hoje: ${updatedUsage.count}/$limit (Plano ${plan.replaceFirstChar { it.uppercase() }})"

                // 4. Fire API call to DeepSeek
                val apiKey = BuildConfig.DEEPSEEK_API_KEY
                
                val aiResponseText = if (apiKey.isBlank() || apiKey == "MY_DEEPSEEK_API_KEY") {
                    // Fallback local analyzer mock if no actual API key provided yet to avoid network failure
                    generateMockInsight(text, crmState, totalSales)
                } else {
                    val service = DeepSeekClient.createService(apiKey)
                    val modelName = DeepSeekClient.getModelName(apiKey)

                    // Context inject from CRM Metrics
                    val contextPrompt = "Você é o assistente inteligente da Nova CRM Mobile. " +
                            "O comerciante está logado no App Core da loja. " +
                            "Métricas atuais da loja: \n" +
                            "- Faturamento total: R$ ${String.format(Locale.getDefault(), "%.2f", totalSales)} \n" +
                            "- Quantidade total de pedidos: ${crmState.orders.size} \n" +
                            "- Quantidade total de clientes: ${crmState.clients.size} \n" +
                            "- Itens no estoque cadastrados: ${crmState.products.size} \n\n" +
                            "Responda de forma sucinta e premium em português brasileiro sobre o seguinte assunto: \n\n$text"

                    val chatRequest = ChatCompletionRequest(
                        model = modelName,
                        messages = listOf(
                            ChatMessage(role = "system", content = "Você é um especialista em e-commerce e CRM. Auxilie o lojista com estratégias valiosas baseadas em seus dados."),
                            ChatMessage(role = "user", content = contextPrompt)
                        )
                    )

                    val response = service.getCompletions("Bearer $apiKey", chatRequest)
                    if (response.isSuccessful) {
                        response.body()?.choices?.firstOrNull()?.message?.content ?: "Não obtive resposta da IA."
                    } else {
                        val errorLog = response.errorBody()?.string() ?: ""
                        Log.e("AiChatViewModel", "DeepSeek API Error: $errorLog")
                        "Erro ao consultar o DeepSeek API (${response.code()}). Vamos tentar novamente futuramente."
                    }
                }

                // 5. Remove "Thinking" and insert actual Answer
                _messages.update { old -> old.filter { !it.isThinking } }
                _messages.update { it + AiMessage("deepseek", aiResponseText) }

            } catch (e: Exception) {
                Log.e("AiChatViewModel", "AI flow crashed: ", e)
                _messages.update { old -> old.filter { !it.isThinking } }
                _messages.update { it + AiMessage("deepseek", "Ocorreu um erro ao conectar com o serviço de IA: ${e.localizedMessage}") }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun generateMockInsight(prompt: String, state: CrmUiState, totalSales: Double): String {
        val q = prompt.lowercase()
        return when {
            q.contains("faturamento") || q.contains("venda") || q.contains("dinheiro") -> {
                "**Análise Financeira Nova CRM:**\n\n" +
                        "Seu faturamento de **R$ ${String.format(Locale.getDefault(), "%.2f", totalSales)}** é composto por **${state.orders.size}** pedidos.\n" +
                        "- **Ticket Médio:** R$ ${if (state.orders.isNotEmpty()) String.format(Locale.getDefault(), "%.2f", totalSales / state.orders.size) else "0,00"}\n" +
                        "**Dica Estratégica:** Sugiro criar um cupom de desconto com frete grátis para compras acima de R$ 150 para impulsionar o ticket médio atual da sua loja!"
            }
            q.contains("produto") || q.contains("estoque") -> {
                val stockList = state.products.sortedBy { it.stock }
                val critical = stockList.filter { it.stock < 5 }
                val builder = StringBuilder("**Relatório de Inventário inteligente:**\n\n")
                builder.append("Você tem atualmente **${state.products.size}** produtos catalogados.\n\n")
                if (critical.isNotEmpty()) {
                    builder.append("⚠️ **Estoque Crítico (menos de 5 unidades):**\n")
                    critical.take(3).forEach { p ->
                        builder.append("- ${p.name}: apenas ${p.stock} em estoque.\n")
                    }
                    builder.append("\n**Ação sugerida:** Crie uma campanha promocional relâmpago de queima para os produtos críticos, ou planeje reabastecimento imediato.")
                } else {
                    builder.append("✅ Seu controle de estoque está saudável! Sem itens de alta criticidade imediata.")
                }
                builder.toString()
            }
            q.contains("cliente") || q.contains("fidelidade") -> {
                "**Análise de Relacionamento (Clientes):**\n\n" +
                        "Você possui **${state.clients.size}** clientes cadastrados no banco de dados.\n" +
                        "**Estratégias de CRM fáceis de aplicar no Nova CRM Mobile:**\n" +
                        "1. Use o chat de suporte para reengajar clientes inativos há mais de 30 dias.\n" +
                        "2. Envie um cupom exclusivo para os clientes que comprarem duas ou mais vezes."
            }
            else -> {
                "**Análise Geral da Loja:**\n\n" +
                        "Seu desempenho está estável. Com **${state.orders.size}** pedidos fechados e **${state.clients.size}** clientes, você está construindo uma base sólida.\n\n" +
                        "*Gostaria de saber mais sobre estratégias para aumentar o estoque, criar cupons ou disparar e-mails promocionais? Digite suas dúvidas!*"
            }
        }
    }
}

// 4. SUPPORT CHAT VIEW MODEL (Realtime Database companion chat)
class SupportChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _typingStatus = MutableStateFlow(TypingStatus())
    val typingStatus: StateFlow<TypingStatus> = _typingStatus.asStateFlow()

    private var activeMerchantId: String? = null
    private var activeClientId: String? = null

    fun loadChat(merchantId: String, clientId: String) {
        activeMerchantId = merchantId
        activeClientId = clientId

        _messages.value = emptyList()

        viewModelScope.launch {
            // Collect messages list
            FirebaseService.streamChatMessages(merchantId, clientId).collectLatest { msgs ->
                _messages.value = msgs
            }
        }

        viewModelScope.launch {
            // Collect typing indicators
            FirebaseService.streamTypingStatus(merchantId, clientId).collectLatest { status ->
                _typingStatus.value = status
            }
        }
    }

    fun sendMessage(text: String) {
        val mid = activeMerchantId ?: return
        val cid = activeClientId ?: return
        if (text.isBlank()) return
        
        FirebaseService.sendChatMessage(mid, cid, text)
        setMerchantTyping(false) // stop typing indicator on send
    }

    fun setMerchantTyping(isTyping: Boolean) {
        val mid = activeMerchantId ?: return
        val cid = activeClientId ?: return
        FirebaseService.updateMerchantTyping(mid, cid, isTyping)
    }

    override fun onCleared() {
        // Safe reset if view cleared
        val mid = activeMerchantId
        val cid = activeClientId
        if (mid != null && cid != null) {
            FirebaseService.updateMerchantTyping(mid, cid, false)
        }
        super.onCleared()
    }
}
