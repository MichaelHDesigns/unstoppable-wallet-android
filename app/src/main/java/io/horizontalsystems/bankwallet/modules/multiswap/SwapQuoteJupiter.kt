package io.horizontalsystems.bankwallet.modules.multiswap

import io.horizontalsystems.bankwallet.modules.multiswap.action.ISwapProviderAction
import io.horizontalsystems.bankwallet.modules.multiswap.settings.ISwapSetting
import io.horizontalsystems.bankwallet.modules.multiswap.ui.DataField
import io.horizontalsystems.marketkit.models.Token
import java.math.BigDecimal

class SwapQuoteJupiter(
    val jupiterTradeData: JupiterTradeData,
    override val fields: List<DataField>,
    override val settings: List<ISwapSetting>,
    override val tokenIn: Token,
    override val tokenOut: Token,
    override val amountIn: BigDecimal,
    override val actionRequired: ISwapProviderAction?
) : ISwapQuote {
    override val amountOut: BigDecimal = jupiterTradeData.amountOut
    override val priceImpact: BigDecimal? = jupiterTradeData.priceImpact
}

data class JupiterTradeData(
    val amountOut: BigDecimal,
    val priceImpact: BigDecimal?,
    val computeUnitPriceMicroLamports: String,
    val slippageBps: Int
)