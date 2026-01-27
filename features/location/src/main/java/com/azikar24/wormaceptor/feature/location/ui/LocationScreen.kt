/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.location.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import com.azikar24.wormaceptor.feature.location.ui.components.LocationMapCard
import com.azikar24.wormaceptor.feature.location.ui.theme.LocationColors
import kotlinx.collections.immutable.ImmutableList
import org.osmdroid.util.GeoPoint

/**
 * Main screen for the Location Simulation feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    latitudeInput: String,
    longitudeInput: String,
    searchQuery: String,
    presets: ImmutableList<LocationPreset>,
    currentMockLocation: MockLocation?,
    isMockEnabled: Boolean,
    isMockLocationAvailable: Boolean,
    isInputValid: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    realDeviceLocation: GeoPoint?,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSetMockLocation: () -> Unit,
    onClearMockLocation: () -> Unit,
    onSetToCurrentLocation: () -> Unit,
    onPresetClick: (LocationPreset) -> Unit,
    onDeletePreset: (String) -> Unit,
    onSavePreset: (String) -> Unit,
    onClearError: () -> Unit,
    onClearSuccessMessage: () -> Unit,
    onMapTap: (GeoPoint) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showSavePresetDialog by remember { mutableStateOf(false) }

    // Show error as snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    // Show success as snackbar
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearSuccessMessage()
        }
    }

    var isMapExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = LocationColors.enabled,
                        )
                        Text(
                            text = "Location Simulator",
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (isMockEnabled) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = LocationColors.enabled.copy(alpha = 0.15f),
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LocationColors.enabled,
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(WormaCeptorDesignSystem.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
            ) {
                // Warning banner if mock locations not available
                if (!isMockLocationAvailable) {
                    item {
                        MockLocationWarningBanner()
                    }
                }

                // Current mock location status card
                item {
                    MockLocationStatusCard(
                        currentMockLocation = currentMockLocation,
                        isMockEnabled = isMockEnabled,
                        onToggle = {
                            if (isMockEnabled) onClearMockLocation() else onSetMockLocation()
                        },
                        isEnabled = isMockLocationAvailable && (isMockEnabled || isInputValid),
                    )
                }

                // Collapsible map visualization
                item {
                    CollapsibleMapSection(
                        isExpanded = isMapExpanded,
                        onToggle = { isMapExpanded = !isMapExpanded },
                        realLocation = realDeviceLocation,
                        mockLocation = currentMockLocation?.let {
                            GeoPoint(it.latitude, it.longitude)
                        },
                        isMockActive = isMockEnabled,
                        onMapTap = onMapTap,
                    )
                }

                // Coordinate input section
                item {
                    CoordinateInputCard(
                        latitudeInput = latitudeInput,
                        longitudeInput = longitudeInput,
                        isInputValid = isInputValid,
                        isLoading = isLoading,
                        isMockEnabled = isMockEnabled,
                        isMockLocationAvailable = isMockLocationAvailable,
                        onLatitudeChanged = onLatitudeChanged,
                        onLongitudeChanged = onLongitudeChanged,
                        onSetMockLocation = onSetMockLocation,
                        onSetToCurrentLocation = onSetToCurrentLocation,
                        onSaveAsPreset = { showSavePresetDialog = true },
                    )
                }

                // Presets section header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Location Presets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${presets.size} locations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Search presets
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = { Text("Search presets...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }

                // Preset items
                items(presets, key = { it.id }) { preset ->
                    PresetItem(
                        preset = preset,
                        isSelected = currentMockLocation?.let {
                            it.latitude == preset.location.latitude &&
                                it.longitude == preset.location.longitude
                        } == true,
                        onClick = { onPresetClick(preset) },
                        onDelete = if (!preset.isBuiltIn) {
                            { onDeletePreset(preset.id) }
                        } else {
                            null
                        },
                        modifier = Modifier.animateItem(),
                    )
                }

                // Empty state for presets
                if (presets.isEmpty()) {
                    item {
                        EmptyPresetsState(hasSearchQuery = searchQuery.isNotBlank())
                    }
                }
            }

            // Loading overlay
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // Save preset dialog
    if (showSavePresetDialog) {
        SavePresetDialog(
            onDismiss = { showSavePresetDialog = false },
            onSave = { name ->
                onSavePreset(name)
                showSavePresetDialog = false
            },
        )
    }
}

@Composable
private fun MockLocationWarningBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LocationColors.warning.asSubtleBackground(),
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = LocationColors.warning,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mock Locations Not Enabled",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                Text(
                    text = "To use location simulation, enable Developer Options, then set this app as the mock location app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MockLocationStatusCard(
    currentMockLocation: MockLocation?,
    isMockEnabled: Boolean,
    onToggle: () -> Unit,
    isEnabled: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMockEnabled) {
                LocationColors.enabled.asSubtleBackground()
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        border = if (isMockEnabled) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(
                WormaCeptorDesignSystem.BorderWidth.regular,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            )
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status icon
            Surface(
                shape = CircleShape,
                color = if (isMockEnabled) {
                    LocationColors.enabled
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(48.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isMockEnabled) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.lg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isMockEnabled) "Mock Location Active" else "Mock Location Disabled",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (currentMockLocation != null && isMockEnabled) {
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                    Text(
                        text = currentMockLocation.formatCoordinates(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocationColors.coordinate,
                        fontWeight = FontWeight.Medium,
                    )
                    currentMockLocation.name?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Switch(
                checked = isMockEnabled,
                onCheckedChange = { onToggle() },
                enabled = isEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = LocationColors.enabled,
                ),
            )
        }
    }
}

@Composable
private fun CoordinateInputCard(
    latitudeInput: String,
    longitudeInput: String,
    isInputValid: Boolean,
    isLoading: Boolean,
    isMockEnabled: Boolean,
    isMockLocationAvailable: Boolean,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
    onSetMockLocation: () -> Unit,
    onSetToCurrentLocation: () -> Unit,
    onSaveAsPreset: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Text(
                text = "Set Custom Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

            // Latitude input
            OutlinedTextField(
                value = latitudeInput,
                onValueChange = onLatitudeChanged,
                label = { Text("Latitude") },
                placeholder = { Text("e.g., 40.7128") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                supportingText = {
                    Text("Range: -90 to 90")
                },
                isError = latitudeInput.isNotBlank() && latitudeInput.toDoubleOrNull()?.let { it !in -90.0..90.0 } == true,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            // Longitude input
            OutlinedTextField(
                value = longitudeInput,
                onValueChange = onLongitudeChanged,
                label = { Text("Longitude") },
                placeholder = { Text("e.g., -74.0060") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                supportingText = {
                    Text("Range: -180 to 180")
                },
                isError = longitudeInput.isNotBlank() && longitudeInput.toDoubleOrNull()?.let { it !in -180.0..180.0 } == true,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                // Get current location button
                OutlinedButton(
                    onClick = onSetToCurrentLocation,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text("Current")
                }

                // Save as preset button
                OutlinedButton(
                    onClick = onSaveAsPreset,
                    enabled = isInputValid && !isLoading,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            // Set mock location button
            Button(
                onClick = onSetMockLocation,
                enabled = isMockLocationAvailable && isInputValid && !isLoading && !isMockEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocationColors.enabled,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Text("Set Mock Location")
            }
        }
    }
}

@Composable
private fun PresetItem(
    preset: LocationPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "presetItemScale",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
            .border(
                width = if (isSelected) WormaCeptorDesignSystem.BorderWidth.thick else WormaCeptorDesignSystem.BorderWidth.regular,
                color = if (isSelected) {
                    LocationColors.enabled
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            )
            .background(
                color = if (isSelected) {
                    LocationColors.enabled.asSubtleBackground()
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(WormaCeptorDesignSystem.Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Location icon
        Surface(
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
            color = if (preset.isBuiltIn) {
                LocationColors.builtIn.asSubtleBackground()
            } else {
                LocationColors.userPreset.asSubtleBackground()
            },
            modifier = Modifier.size(40.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (preset.isBuiltIn) {
                        LocationColors.builtIn
                    } else {
                        LocationColors.userPreset
                    },
                )
            }
        }

        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(16.dp),
                        tint = LocationColors.enabled,
                    )
                }
            }
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
            Text(
                text = preset.location.formatCoordinates(),
                style = MaterialTheme.typography.bodySmall,
                color = LocationColors.coordinate,
            )
        }

        // Delete button for user presets
        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete preset",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun CollapsibleMapSection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    realLocation: GeoPoint?,
    mockLocation: GeoPoint?,
    isMockActive: Boolean,
    onMapTap: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Toggle header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(WormaCeptorDesignSystem.Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Map Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isMockActive) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = LocationColors.enabled.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = "LIVE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LocationColors.enabled,
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Collapsible map content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = WormaCeptorDesignSystem.Spacing.lg,
                            end = WormaCeptorDesignSystem.Spacing.lg,
                            bottom = WormaCeptorDesignSystem.Spacing.lg,
                        ),
                ) {
                    LocationMapCard(
                        realLocation = realLocation,
                        mockLocation = mockLocation,
                        isMockActive = isMockActive,
                        onMapTap = onMapTap,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPresetsState(hasSearchQuery: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = WormaCeptorDesignSystem.Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        Text(
            text = if (hasSearchQuery) "No matches found" else "No presets saved",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

        Text(
            text = if (hasSearchQuery) {
                "Try a different search term"
            } else {
                "Save locations for quick access"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun SavePresetDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var presetName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Location Preset") },
        text = {
            OutlinedTextField(
                value = presetName,
                onValueChange = { presetName = it },
                label = { Text("Preset Name") },
                placeholder = { Text("e.g., Office, Home, Gym") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(presetName) },
                enabled = presetName.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
