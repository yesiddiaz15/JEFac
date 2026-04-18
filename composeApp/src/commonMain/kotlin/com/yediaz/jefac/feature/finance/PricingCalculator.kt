package com.yediaz.jefac.feature.finance

data class PricingResult(
    val basePrice: Double,
    val discountAmount: Double,
    val finalPrice: Double,
    val commissionPct: Double,
    val professionalEarn: Double,
    val businessEarn: Double
)

class PricingCalculator {
    fun calculate(
        basePrice: Double,
        discountType: String? = null,
        discountValue: Double = 0.0,
        commissionPct: Double = 0.0
    ): PricingResult {
        val discountAmount = when (discountType) {
            "percentage" -> basePrice * (discountValue / 100.0)
            "fixed" -> discountValue.coerceAtMost(basePrice)
            else -> 0.0
        }
        val finalPrice = (basePrice - discountAmount).coerceAtLeast(0.0)
        val professionalEarn = basePrice * (commissionPct / 100.0)
        val businessEarn = (finalPrice - professionalEarn).coerceAtLeast(0.0)

        return PricingResult(
            basePrice = basePrice,
            discountAmount = discountAmount,
            finalPrice = finalPrice,
            commissionPct = commissionPct,
            professionalEarn = professionalEarn,
            businessEarn = businessEarn
        )
    }

    fun recalculateDiscount(
        basePrice: Double,
        discountType: String?,
        discountValue: Double,
        commissionPct: Double
    ): PricingResult = calculate(basePrice, discountType, discountValue, commissionPct)
}
