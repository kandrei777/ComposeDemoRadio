package com.example.myradio.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BottomToolbar(
    filterMode: FilterMode,
    onSwitchAll: () -> Unit,
    onSwitchFavorites: () -> Unit,
    onSwitchRecents: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterButton(
            text = "All",
            isSelected = filterMode == FilterMode.ALL,
            onClick = onSwitchAll,
            modifier = Modifier.weight(1f),
        )
        FilterButton(
            text = "Favorites",
            isSelected = filterMode == FilterMode.FAVORITES,
            onClick = onSwitchFavorites,
            modifier = Modifier.weight(1f),
        )
        FilterButton(
            text = "Recents",
            isSelected = filterMode == FilterMode.RECENT,
            onClick = onSwitchRecents,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val targetBackgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }
    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 250),
        label = "FilterBackgroundAnimation"
    )

    // 2. Анимируем цвет текста для бесшовного перехода
    val targetTextColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val animatedTextColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(durationMillis = 200),
        label = "FilterTextAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .background(
                color = animatedBackgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = animatedTextColor,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}

