package io.horizontalsystems.bankwallet.modules.multiswap

import io.horizontalsystems.bankwallet.modules.multiswap.ui.DataField
import io.horizontalsystems.bankwallet.modules.multiswap.sendtransaction.SendTransactionData
import io.horizontalsystems.marketkit.models.Token
import java.math.BigDecimal

class SwapFinalQuoteJupiter(
    override val tokenIn: Token,
    override val tokenOut: Token,
    override val amountIn: BigDecimal,
    override val amountOut: BigDecimal,
    override val amountOutMin: BigDecimal,
    val transaction: String,
    override val priceImpact: BigDecimal?,
    override val fields: List<DataField>
) : ISwapFinalQuote {
    override val sendTransactionData: SendTransactionData
        get() = SendTransactionData.Solana(transaction)
}