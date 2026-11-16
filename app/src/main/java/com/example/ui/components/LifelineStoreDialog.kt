package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.LifelineType
import com.example.data.models.PlayerProfile
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldContainer

@Composable
fun LifelineStoreDialog(
  profile: PlayerProfile,
  isArabic: Boolean,
  onPurchase: (LifelineType) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isArabic) "متجر وسائل المساعدة" else "Lifelines Store",
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleLarge
        )
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = GoldContainer
        ) {
          Text(
            text = "${profile.coins} 🪙",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF78350F),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.testTag("lifeline_store_dialog")
      ) {
        Text(
          text = if (isArabic) {
            "استبدل نقاطك الذهبية بوسائل مساعدة إضافية للتحديات:"
          } else {
            "Redeem your earned coins for extra quiz lifelines:"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LifelineType.entries.forEach { type ->
          val owned = when (type) {
            LifelineType.FIFTY_FIFTY -> profile.lifelines5050
            LifelineType.HINT -> profile.lifelinesHint
            LifelineType.EXTRA_TIME -> profile.lifelinesTime
            LifelineType.SKIP -> profile.lifelinesSkip
          }
          val canAfford = profile.coins >= type.costCoins

          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.14f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = type.icon,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = if (isArabic) type.nameAr else type.nameEn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = if (isArabic) "المتوفر: $owned" else "Owned: $owned",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              Button(
                onClick = { onPurchase(type) },
                enabled = canAfford,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = EmeraldDark,
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .height(48.dp)
                  .testTag("buy_lifeline_${type.id}")
              ) {
                Text(
                  text = "${type.costCoins} 🪙",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(text = if (isArabic) "إغلاق" else "Close", fontWeight = FontWeight.Bold)
      }
    }
  )
}
