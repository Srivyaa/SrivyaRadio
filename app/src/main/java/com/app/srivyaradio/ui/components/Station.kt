package com.app.srivyaradio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.app.srivyaradio.R
import androidx.compose.material.icons.outlined.FileDownload

@Composable
fun Station(
    name: String,
    image: String,
    label: String,
    isOffline: Boolean = false,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onDownload: (() -> Unit)? = null,
    onClick: () -> Unit,
    onOptions: () -> Unit,
    modifier: Modifier
) {

    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }

    ) {
        Box(modifier = Modifier.padding(15.dp)) {
            RadioLogoSmall(imageUrl = image, size = 53)
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = TextUnit(18f, TextUnitType.Sp),
                )
                if (isOffline) {
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.error, shape = CircleShape)
                    )
                }
            }
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    fontWeight = FontWeight.W400,
                    fontSize = TextUnit(14f, TextUnitType.Sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onDownload != null) {
                IconButton(onClick = { onDownload() }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Outlined.FileDownload,
                        modifier = Modifier.size(24.dp),
                        contentDescription = null
                    )
                }
            }
            IconButton(onClick = { onToggleFavorite() }) {
                Icon(
                    painter = painterResource(id = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outlined),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null
                )
            }
            IconButton(
                modifier = Modifier.padding(6.dp),
                onClick = { onOptions() }
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    modifier = Modifier.size(24.dp),
                    contentDescription = null
                )
            }
        }
    }
}