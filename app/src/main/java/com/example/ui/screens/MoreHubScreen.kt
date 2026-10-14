package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserPreferences
import com.example.ui.components.SafeNoticeBanner

@Composable
fun MoreHubScreen(
    preferences: UserPreferences,
    onNavigateToIngredients: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToShelf: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Beauty-Tech Suite",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Specialized cosmetic tools, vanity management, and skin tracking",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            HubItemCard(
                title = "Ingredient Intelligence",
                description = "Encyclopedia of cosmetic actives & paste formula decoder",
                icon = Icons.Default.Science,
                iconColor = MaterialTheme.colorScheme.primary,
                tag = "more_nav_ingredients",
                onClick = onNavigateToIngredients
            )
        }

        item {
            HubItemCard(
                title = "Budget-Based Finder",
                description = "Calculate optimal product bundles strictly within your target budget",
                icon = Icons.Default.Payments,
                iconColor = MaterialTheme.colorScheme.tertiary,
                tag = "more_nav_budget",
                onClick = onNavigateToBudget
            )
        }

        item {
            HubItemCard(
                title = "My Beauty Shelf",
                description = "Manage owned products, opened dates, and PAO expiry tracking",
                icon = Icons.Default.Spa,
                iconColor = MaterialTheme.colorScheme.secondary,
                tag = "more_nav_shelf",
                onClick = onNavigateToShelf
            )
        }

        item {
            HubItemCard(
                title = "Progress & Skin Journal",
                description = "Daily comfort ratings, routine checkoffs, and barrier observations",
                icon = Icons.Default.Book,
                iconColor = MaterialTheme.colorScheme.primary,
                tag = "more_nav_journal",
                onClick = onNavigateToJournal
            )
        }

        item {
            HubItemCard(
                title = "Profile & Settings",
                description = "Skin goals, currency selector (${preferences.currency}), and safety principles",
                icon = Icons.Default.Settings,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                tag = "more_nav_settings",
                onClick = onNavigateToSettings
            )
        }

        item {
            SafeNoticeBanner(
                message = "SkinRoutine AI operates locally with secure on-device storage. Explore with confidence."
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun HubItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
