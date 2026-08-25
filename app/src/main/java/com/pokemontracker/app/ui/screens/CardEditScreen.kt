package com.pokemontracker.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pokemontracker.app.ui.CardEditViewModel
import com.pokemontracker.app.ui.components.CardImage
import com.pokemontracker.app.util.ImageStorage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditScreen(
    viewModel: CardEditViewModel,
    onSaved: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        val file = pendingCameraFile
        if (success && file != null) {
            val path = ImageStorage.persistFromFile(context, file)
            if (path != null) viewModel.onImageSelected(path)
        }
        pendingCameraFile = null
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            val path = ImageStorage.persistFromUri(context, uri)
            if (path != null) viewModel.onImageSelected(path)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (viewModel.isEditing) "Karte bearbeiten" else "Neue Karte")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save(onSaved) },
                        enabled = viewModel.canSave,
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Speichern")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Image + capture buttons.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                CardImage(
                    imagePath = viewModel.imagePath,
                    modifier = Modifier
                        .width(160.dp)
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val (uri, file) = ImageStorage.createCameraOutput(context)
                        pendingCameraFile = file
                        takePictureLauncher.launch(uri)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Kamera")
                }
                OutlinedButton(
                    onClick = {
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Galerie")
                }
            }
            if (viewModel.imagePath != null) {
                TextButton(
                    onClick = { viewModel.onImageSelected(null) },
                    modifier = Modifier.align(Alignment.End),
                ) { Text("Bild entfernen") }
            }

            // Text fields.
            LabeledField("Name *", viewModel.name, { viewModel.name = it })
            LabeledField("Sprache", viewModel.language, { viewModel.language = it })
            LabeledField("Set / Pack", viewModel.setPack, { viewModel.setPack = it })
            LabeledField("Kartennummer", viewModel.cardNumber, { viewModel.cardNumber = it })
            LabeledField("Zustand", viewModel.condition, { viewModel.condition = it })
            LabeledField(
                label = "Menge",
                value = viewModel.quantity,
                onValueChange = { viewModel.quantity = it.filter { c -> c.isDigit() } },
                keyboardType = KeyboardType.Number,
            )
            LabeledField(
                label = "Kaufpreis (€)",
                value = viewModel.purchasePrice,
                onValueChange = { viewModel.purchasePrice = it },
                keyboardType = KeyboardType.Decimal,
            )
            if (!viewModel.isEditing) {
                LabeledField(
                    label = "Aktueller Wert (€)",
                    value = viewModel.initialValue,
                    onValueChange = { viewModel.initialValue = it },
                    keyboardType = KeyboardType.Decimal,
                    supporting = "Startwert für den Preisverlauf. Später jederzeit aktualisierbar.",
                )
            }
            LabeledField(
                label = "Notizen",
                value = viewModel.notes,
                onValueChange = { viewModel.notes = it },
                singleLine = false,
            )

            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    supporting: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = supporting?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
    )
}
