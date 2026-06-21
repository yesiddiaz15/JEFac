package com.yediaz.jefac.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yediaz.jefac.resources.Res
import com.yediaz.jefac.resources.quicksand_bold
import com.yediaz.jefac.resources.quicksand_light
import com.yediaz.jefac.resources.quicksand_medium
import com.yediaz.jefac.resources.quicksand_regular
import com.yediaz.jefac.resources.quicksand_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun quicksandFontFamily() = FontFamily(
    Font(Res.font.quicksand_light, FontWeight.Light),
    Font(Res.font.quicksand_regular, FontWeight.Normal),
    Font(Res.font.quicksand_medium, FontWeight.Medium),
    Font(Res.font.quicksand_semibold, FontWeight.SemiBold),
    Font(Res.font.quicksand_bold, FontWeight.Bold)
)

@Composable
fun jefacTypography(): Typography {
    val quicksand = quicksandFontFamily()
    return Typography().copy(
        headlineMedium = Typography().headlineMedium.copy(
            fontFamily = quicksand,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        ),
        titleLarge = Typography().titleLarge.copy(
            fontFamily = quicksand,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp
        ),
        titleMedium = Typography().titleMedium.copy(
            fontFamily = quicksand,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        ),
        bodyLarge = Typography().bodyLarge.copy(
            fontFamily = quicksand,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        bodyMedium = Typography().bodyMedium.copy(
            fontFamily = quicksand,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        ),
        labelMedium = Typography().labelMedium.copy(
            fontFamily = quicksand,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    )
}
