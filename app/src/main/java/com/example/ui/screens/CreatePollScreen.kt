package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.PollOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollScreen(
    onCreatePoll: (String, List<PollOption>, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var question by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Outros") }
    
    // Dynamic list of options. Each option has a text and an optional image url
    val optionsList = remember { 
        mutableStateListOf(
            OptionInputState("", null),
            OptionInputState("", null)
        ) 
    }
    
    var showImageSelectorForIdx by remember { mutableStateOf<Int?>(null) }
    
    val mockImages = emptyList<String>()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Criar Enquete",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("poll_back_button")) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    val isFormValid = question.isNotBlank() && optionsList.count { it.text.isNotBlank() } >= 2
                    IconButton(
                        onClick = {
                            if (isFormValid) {
                                val finalOptions = optionsList
                                    .filter { it.text.isNotBlank() }
                                    .mapIndexed { idx, opt ->
                                        PollOption("opt-new-$idx", opt.text, imageUrl = opt.imageUrl, votes = 0)
                                    }
                                onCreatePoll(question, finalOptions, selectedCategory)
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.testTag("publish_poll_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Publish,
                            contentDescription = "Publicar",
                            tint = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            // 1. Question input at the top
            item {
                Text(
                    text = "Faça sua pergunta",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    placeholder = { Text("Qual a sua pergunta/discussão coletiva?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("poll_question_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 2. Category selection
            item {
                Text(
                    text = "Selecione uma Categoria",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CATEGORIES.filter { it.first != "Todos" }) { (catName, emoji) ->
                        val isCatSelected = selectedCategory == catName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isCatSelected) {
                                        Color(0xFF6F3FF5)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isCatSelected) Color(0xFF6F3FF5) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedCategory = catName }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "$emoji $catName",
                                fontSize = 13.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCatSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. Options Header
            item {
                Text(
                    text = "Opções da Enquete",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // 4. Dynamic Options list
            itemsIndexed(optionsList) { idx, optionState ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = optionState.text,
                        onValueChange = { optionState.text = it },
                        label = { Text("Opção ${idx + 1}") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("poll_option_input_$idx"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Image picker button
                    IconButton(
                        onClick = { showImageSelectorForIdx = idx },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (optionState.imageUrl != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .testTag("poll_option_image_$idx")
                    ) {
                        if (optionState.imageUrl != null) {
                            AsyncImage(
                                model = optionState.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AddPhotoAlternate,
                                contentDescription = "Adicionar imagem",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Delete option button (only if count > 2)
                    if (optionsList.size > 2) {
                        IconButton(
                            onClick = { optionsList.removeAt(idx) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Excluir opção",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // 5. Add more options button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { optionsList.add(OptionInputState("", null)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_option_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Adicionar mais opções",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Footer publish button in-screen
                val isFormValid = question.isNotBlank() && optionsList.count { it.text.isNotBlank() } >= 2
                Button(
                    onClick = {
                        if (isFormValid) {
                            val finalOptions = optionsList
                                .filter { it.text.isNotBlank() }
                                .mapIndexed { idx, opt ->
                                    PollOption("opt-new-$idx", opt.text, imageUrl = opt.imageUrl, votes = 0)
                                }
                            onCreatePoll(question, finalOptions, selectedCategory)
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("publish_poll_footer_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Publicar Enquete",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Preset Image Selector
    if (showImageSelectorForIdx != null) {
        val currentIdx = showImageSelectorForIdx!!
        Dialog(onDismissRequest = { showImageSelectorForIdx = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Escolha uma Imagem",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        mockImages.forEach { url ->
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        optionsList[currentIdx] = optionsList[currentIdx].copy(imageUrl = url)
                                        showImageSelectorForIdx = null
                                    }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(onClick = { showImageSelectorForIdx = null }) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

// Data class representation for options input state management
class OptionInputState(text: String, imageUrl: String?) {
    var text by mutableStateOf(text)
    var imageUrl by mutableStateOf(imageUrl)

    fun copy(text: String = this.text, imageUrl: String? = this.imageUrl): OptionInputState {
        return OptionInputState(text, imageUrl)
    }
}
