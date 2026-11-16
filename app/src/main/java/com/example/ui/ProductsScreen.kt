package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.CrmUiState
import com.example.CrmViewModel
import com.example.Product
import com.example.R
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicPrimary
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.CosmicTertiary
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

data class ImagePreset(val label: String, val category: String, val url: String)

val SAMPLE_IMAGE_PRESETS = listOf(
    ImagePreset("📱 Smartphone", "Tecnologia", "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&q=80"),
    ImagePreset("🎧 Fone", "Tecnologia", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&q=80"),
    ImagePreset("👕 Camiseta", "Moda", "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=400&q=80"),
    ImagePreset("☕ Café", "Alimentos", "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=400&q=80"),
    ImagePreset("💄 Cosmético", "Beleza", "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=400&q=80"),
    ImagePreset("⌚ Relógio", "Acessórios", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&q=80"),
    ImagePreset("👟 Tênis", "Moda", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&q=80")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    state: CrmUiState,
    viewModel: CrmViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    // Dialog form states for Add
    var prodName by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf("") }
    var prodPrice by remember { mutableStateOf("") }
    var prodStock by remember { mutableStateOf("") }
    var prodImageUrl by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val filteredProducts = state.products.filter {
        it.name.lowercase().contains(searchQuery.lowercase()) ||
                it.category.lowercase().contains(searchQuery.lowercase())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .size(34.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.nova),
                                contentDescription = "Nova CRM Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Estoque & Catálogo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Gerenciamento de Produtos Nova CRM",
                                fontSize = 11.sp,
                                color = CosmicPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    prodName = ""
                    prodCategory = ""
                    prodPrice = ""
                    prodStock = ""
                    prodImageUrl = ""
                    formError = null
                    showAddDialog = true
                },
                containerColor = CosmicPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Produto")
            }
        },
        containerColor = CosmicBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .padding(horizontal = 16.dp)
            ) {
                // 1. Smart Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    placeholder = {
                        Text(
                            "Buscar por nome ou categoria...",
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("product_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // 2. Products List
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = "Sem estoque",
                                tint = TextMuted,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Nenhum produto encontrado para a busca." else "Sua loja ainda não possui produtos catalogados.",
                                fontSize = 14.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("product_list"),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 84.dp)
                    ) {
                        items(filteredProducts.reversed(), key = { it.id }) { item ->
                            ProductCardRow(
                                product = item,
                                onClick = { editingProduct = item },
                                onIncrement = { viewModel.adjustProductStock(item, 1) },
                                onDecrement = { viewModel.adjustProductStock(item, -1) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Responsive Modal adding form Dialog
    if (showAddDialog) {
        var isSavingAdd by remember { mutableStateOf(false) }
        var isProcessingImage by remember { mutableStateOf(false) }

        ResponsiveProductFormDialog(
            onDismissRequest = { if (!isSavingAdd && !isProcessingImage) showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Novo item",
                        tint = CosmicPrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Novo Produto",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            content = {
                if (formError != null) {
                    Text(
                        text = formError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                ProductPhotoPickerSection(
                    imageUrl = prodImageUrl,
                    category = prodCategory,
                    productName = prodName.ifBlank { "Produto" },
                    isProcessing = isProcessingImage,
                    onProcessingChange = { isProcessingImage = it },
                    onImageSelected = { newImg ->
                        prodImageUrl = newImg
                        formError = null
                    },
                    onImageError = { err -> formError = err },
                    urlInputTestTag = "new_product_image_input"
                )

                OutlinedTextField(
                    value = prodName,
                    onValueChange = { prodName = it },
                    label = { Text("Nome do produto", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_product_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = prodCategory,
                    onValueChange = { prodCategory = it },
                    label = { Text("Categoria", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    placeholder = { Text("Ex: Tecnologia, Moda, Alimentos", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_product_category_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = prodPrice,
                        onValueChange = { prodPrice = it },
                        label = { Text("Preço (R$)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_product_price_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    OutlinedTextField(
                        value = prodStock,
                        onValueChange = { prodStock = it },
                        label = { Text("Estoque", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_product_stock_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                // Quick image sample chips
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Ou use uma foto de exemplo:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SAMPLE_IMAGE_PRESETS.forEach { preset ->
                            FilterChip(
                                selected = prodImageUrl == preset.url,
                                onClick = {
                                    prodImageUrl = preset.url
                                    if (prodCategory.isBlank()) prodCategory = preset.category
                                },
                                label = { Text(preset.label, fontSize = 11.sp, maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CosmicPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = CosmicPrimary
                                )
                            )
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false },
                    enabled = !isSavingAdd && !isProcessingImage,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("CANCELAR", color = CosmicPrimary, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedPrice = prodPrice.replace(",", ".").trim().toDoubleOrNull()
                        val parsedStock = prodStock.trim().toIntOrNull()

                        if (prodName.isBlank() || prodCategory.isBlank()) {
                            formError = "Preencha nome e categoria obrigatórios."
                        } else if (parsedPrice == null || parsedPrice <= 0.0) {
                            formError = "Insira um preço válido maior que zero."
                        } else if (parsedStock == null || parsedStock < 0) {
                            formError = "O estoque inicial não pode ser negativo."
                        } else {
                            isSavingAdd = true
                            viewModel.addProduct(
                                name = prodName.trim(),
                                price = parsedPrice,
                                category = prodCategory.trim(),
                                stock = parsedStock,
                                imageUrl = prodImageUrl.trim()
                            ) { success ->
                                isSavingAdd = false
                                if (success) {
                                    showAddDialog = false
                                } else {
                                    formError = "Falha ao gravar no Firestore. Tente novamente."
                                }
                            }
                        }
                    },
                    enabled = !isSavingAdd && !isProcessingImage,
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("submit_product_button")
                ) {
                    if (isSavingAdd) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("SALVAR", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        )
    }

    // Modal Editing / Details Dialog
    editingProduct?.let { product ->
        var editName by remember(product) { mutableStateOf(product.name) }
        var editCategory by remember(product) { mutableStateOf(product.category) }
        var editPrice by remember(product) { mutableStateOf(product.price.toString()) }
        var editStock by remember(product) { mutableStateOf(product.stock.toString()) }
        var editImageUrl by remember(product) { mutableStateOf(product.imageUrl.ifBlank { product.primaryImage }) }
        var editError by remember(product) { mutableStateOf<String?>(null) }
        var isSaving by remember { mutableStateOf(false) }
        var isProcessingEditImage by remember { mutableStateOf(false) }

        ResponsiveProductFormDialog(
            onDismissRequest = { if (!isSaving && !isProcessingEditImage) editingProduct = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = CosmicPrimary, modifier = Modifier.padding(end = 8.dp))
                        Text("Editar Produto", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    IconButton(
                        onClick = {
                            viewModel.deleteProduct(product.id) {
                                editingProduct = null
                            }
                        }
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                }
            },
            content = {
                if (editError != null) {
                    Text(editError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                ProductPhotoPickerSection(
                    imageUrl = editImageUrl,
                    category = editCategory,
                    productName = editName.ifBlank { product.name },
                    isProcessing = isProcessingEditImage,
                    onProcessingChange = { isProcessingEditImage = it },
                    onImageSelected = { newImg ->
                        editImageUrl = newImg
                        editError = null
                    },
                    onImageError = { err -> editError = err },
                    urlInputTestTag = "edit_product_image_input"
                )

                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Nome do produto", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = editCategory,
                    onValueChange = { editCategory = it },
                    label = { Text("Categoria", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text("Preço (R$)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editStock,
                        onValueChange = { editStock = it },
                        label = { Text("Estoque", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Presets in edit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SAMPLE_IMAGE_PRESETS.forEach { preset ->
                        FilterChip(
                            selected = editImageUrl == preset.url,
                            onClick = { editImageUrl = preset.url },
                            label = { Text(preset.label, fontSize = 10.sp, maxLines = 1) }
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingProduct = null },
                    enabled = !isSaving && !isProcessingEditImage,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("FECHAR", color = CosmicPrimary, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedPrice = editPrice.replace(",", ".").trim().toDoubleOrNull()
                        val parsedStock = editStock.trim().toIntOrNull()
                        if (editName.isBlank() || editCategory.isBlank()) {
                            editError = "Preencha todos os campos obrigatórios."
                        } else if (parsedPrice == null || parsedPrice <= 0.0) {
                            editError = "Preço inválido."
                        } else if (parsedStock == null || parsedStock < 0) {
                            editError = "Estoque inválido."
                        } else {
                            isSaving = true
                            viewModel.updateProduct(
                                product = product,
                                name = editName.trim(),
                                price = parsedPrice,
                                category = editCategory.trim(),
                                stock = parsedStock,
                                imageUrl = editImageUrl.trim()
                            ) { success ->
                                isSaving = false
                                if (success) {
                                    editingProduct = null
                                } else {
                                    editError = "Erro ao atualizar. Tente novamente."
                                }
                            }
                        }
                    },
                    enabled = !isSaving && !isProcessingEditImage,
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("ATUALIZAR", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        )
    }
}

@Composable
private fun ResponsiveProductFormDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    dismissButton: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .heightIn(max = maxHeight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    title()
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        content = content
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dismissButton()
                        Spacer(modifier = Modifier.width(8.dp))
                        confirmButton()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductPhotoPickerSection(
    imageUrl: String,
    category: String,
    productName: String,
    isProcessing: Boolean,
    onProcessingChange: (Boolean) -> Unit,
    onImageSelected: (String) -> Unit,
    onImageError: (String) -> Unit,
    urlInputTestTag: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isUploadedLocalImage = imageUrl.startsWith("data:image/", ignoreCase = true)

    // Zero-permission Android Photo Picker (Google Play policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onProcessingChange(true)
            scope.launch {
                val encoded = encodeImageUriToDataUrl(context, uri)
                onProcessingChange(false)
                if (encoded != null) {
                    onImageSelected(encoded)
                } else {
                    onImageError("Não foi possível carregar a foto selecionada.")
                }
            }
        }
    }

    // System Camera quick capture (returns Bitmap via Intent without requiring CAMERA permission)
    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onProcessingChange(true)
            scope.launch {
                val encoded = withContext(Dispatchers.IO) {
                    encodeBitmapToDataUrl(bitmap)
                }
                onProcessingChange(false)
                if (encoded != null) {
                    onImageSelected(encoded)
                } else {
                    onImageError("Não foi possível processar a foto da câmera.")
                }
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Image Preview Box
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProductThumbnail(
                        product = Product(
                            name = productName,
                            category = category,
                            imageUrl = imageUrl
                        ),
                        modifier = Modifier.size(84.dp)
                    )

                    if (isProcessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        }
                    } else if (imageUrl.isNotBlank()) {
                        Surface(
                            onClick = { onImageSelected("") },
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .testTag("remove_product_photo_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remover foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Upload & Camera Action Buttons
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CosmicPrimary.copy(alpha = 0.12f),
                            contentColor = CosmicPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 42.dp)
                            .testTag("upload_product_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Selecionar foto da galeria",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (imageUrl.isBlank()) "Enviar foto" else "Trocar foto",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { cameraPreviewLauncher.launch(null) },
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderDark),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 40.dp)
                            .testTag("camera_product_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Tirar foto com a câmera",
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tirar foto",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        if (isUploadedLocalImage) {
            Surface(
                color = CosmicSecondary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CosmicSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Foto selecionada",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF065F46),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Usar URL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicPrimary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onImageSelected("") }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = onImageSelected,
                label = {
                    Text(
                        "URL da imagem (Opcional)",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = "URL da imagem",
                        tint = CosmicPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(urlInputTestTag),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

private suspend fun encodeImageUriToDataUrl(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
    try {
        val rawBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val maxSide = max(info.size.width, info.size.height)
                if (maxSide > 720) {
                    val scale = 720f / maxSide.toFloat()
                    decoder.setTargetSize(
                        (info.size.width * scale).roundToInt().coerceAtLeast(1),
                        (info.size.height * scale).roundToInt().coerceAtLeast(1)
                    )
                }
            }
        } else {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            var sampleSize = 1
            val maxDim = max(boundsOptions.outWidth, boundsOptions.outHeight)
            while (maxDim / sampleSize > 1080) {
                sampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } ?: return@withContext null

        encodeBitmapToDataUrl(rawBitmap)
    } catch (e: Exception) {
        null
    }
}

private fun encodeBitmapToDataUrl(bitmap: Bitmap): String? {
    return try {
        val maxSide = max(bitmap.width, bitmap.height)
        val scaled = if (maxSide > 640) {
            val ratio = 640f / maxSide.toFloat()
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).roundToInt().coerceAtLeast(1),
                (bitmap.height * ratio).roundToInt().coerceAtLeast(1),
                true
            )
        } else {
            bitmap
        }

        val output = ByteArrayOutputStream()
        var quality = 80
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
        while (output.size() > 240_000 && quality > 35) {
            output.reset()
            quality -= 15
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
        }
        val base64 = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64"
    } catch (e: Exception) {
        null
    }
}

@Composable
fun rememberProductImageModel(rawUrl: String): Any? {
    val context = LocalContext.current
    val trimmed = rawUrl.trim()

    return remember(trimmed) {
        if (trimmed.isEmpty()) {
            null
        } else if (trimmed.startsWith("data:image/", ignoreCase = true)) {
            try {
                val base64Data = trimmed.substringAfter(",")
                val decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        } else if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            ImageRequest.Builder(context)
                .data(trimmed)
                .crossfade(true)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10)")
                .build()
        } else if (trimmed.length > 50 && !trimmed.contains(" ") && !trimmed.contains("/")) {
            try {
                val decodedBytes = android.util.Base64.decode(trimmed, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        } else {
            trimmed
        }
    }
}

@Composable
fun ProductThumbnail(
    product: Product,
    modifier: Modifier = Modifier
) {
    val categoryLower = product.category.lowercase().trim()
    val (icon, gradientColors) = getCategoryVisuals(categoryLower)
    val primaryImg = product.primaryImage
    val imageModel = rememberProductImageModel(primaryImg)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .size(62.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        if (imageModel != null) {
            SubcomposeAsyncImage(
                model = imageModel,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = CosmicPrimary
                        )
                    }
                },
                error = {
                    CategoryGradientFallback(
                        category = product.category,
                        icon = icon,
                        gradientColors = gradientColors
                    )
                }
            )
        } else {
            CategoryGradientFallback(
                category = product.category,
                icon = icon,
                gradientColors = gradientColors
            )
        }
    }
}

@Composable
fun CategoryGradientFallback(
    category: String,
    icon: ImageVector,
    gradientColors: List<Color>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

private fun getCategoryVisuals(category: String): Pair<ImageVector, List<Color>> {
    return when {
        category.contains("tec") || category.contains("eletr") || category.contains("smart") || category.contains("fone") -> {
            Icons.Default.Devices to listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
        }
        category.contains("moda") || category.contains("roup") || category.contains("calc") || category.contains("vest") -> {
            Icons.Default.Checkroom to listOf(Color(0xFFEC4899), Color(0xFFF43F5E))
        }
        category.contains("alim") || category.contains("beb") || category.contains("cafe") || category.contains("rest") || category.contains("comid") -> {
            Icons.Default.Restaurant to listOf(Color(0xFFF59E0B), Color(0xFFD97706))
        }
        category.contains("beleza") || category.contains("cosm") || category.contains("perf") || category.contains("saud") -> {
            Icons.Default.Spa to listOf(Color(0xFF10B981), Color(0xFF059669))
        }
        category.contains("livr") || category.contains("papel") || category.contains("estud") -> {
            Icons.Default.MenuBook to listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
        }
        category.contains("serv") || category.contains("manut") || category.contains("ferram") -> {
            Icons.Default.Build to listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))
        }
        category.contains("lux") || category.contains("prem") || category.contains("joia") || category.contains("aces") -> {
            Icons.Default.AutoAwesome to listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))
        }
        else -> {
            Icons.Default.LocalMall to listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
        }
    }
}

@Composable
fun ProductCardRow(
    product: Product,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val borderDarkColor = com.example.ui.theme.BorderDark
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderDarkColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("product_item_${product.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Thumbnail
            ProductThumbnail(
                product = product,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Product Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Categoria",
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = product.category,
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "R$ " + String.format(Locale.getDefault(), "%,.2f", product.price),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CosmicPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick Stock Actions (+ & - buttons directly on the card)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Decrement Button
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                        .border(1.dp, borderDarkColor.copy(alpha = 0.5f), CircleShape)
                        .testTag("decrement_${product.id}"),
                    enabled = product.stock > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Reduzir estoque",
                        tint = if (product.stock > 0) MaterialTheme.colorScheme.onBackground else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Balance Box status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    val stockColor = if (product.stock < 5) CosmicSecondary else MaterialTheme.colorScheme.onSurface
                    Text(
                        text = "${product.stock}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = stockColor
                    )
                    Text(
                        text = "un",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }

                // Increment Button
                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                        .border(1.dp, borderDarkColor.copy(alpha = 0.5f), CircleShape)
                        .testTag("increment_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar estoque",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
