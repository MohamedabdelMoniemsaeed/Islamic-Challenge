package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws subtle eight-pointed Islamic stars (Rub el Hizb / Khatim)
 * as decorative geometry on backgrounds and cards.
 */
@Composable
fun IslamicGeometricBackground(
  modifier: Modifier = Modifier,
  patternColor: Color = GoldAccent.copy(alpha = 0.08f),
  strokeWidth: Float = 1.5f
) {
  Canvas(modifier = modifier.fillMaxSize()) {
    val step = 80.dp.toPx()
    val starRadius = 22.dp.toPx()

    var x = step / 2
    while (x < size.width + step) {
      var y = step / 2
      while (y < size.height + step) {
        drawEightPointStar(
          center = Offset(x, y),
          outerRadius = starRadius,
          innerRadius = starRadius * 0.707f,
          color = patternColor,
          strokeWidth = strokeWidth
        )
        y += step
      }
      x += step
    }
  }
}

/**
 * Draws an 8-pointed star by drawing two overlapping squares rotated by 45 degrees
 */
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEightPointStar(
  center: Offset,
  outerRadius: Float,
  innerRadius: Float,
  color: Color,
  strokeWidth: Float
) {
  val path = Path()
  val numPoints = 16
  for (i in 0 until numPoints) {
    val angle = (i * Math.PI / 8) - (Math.PI / 2)
    val r = if (i % 2 == 0) outerRadius else innerRadius
    val px = (center.x + r * cos(angle)).toFloat()
    val py = (center.y + r * sin(angle)).toFloat()
    if (i == 0) {
      path.moveTo(px, py)
    } else {
      path.lineTo(px, py)
    }
  }
  path.close()
  drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
}
