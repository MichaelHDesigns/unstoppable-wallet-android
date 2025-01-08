package io.horizontalsystems.bankwallet.modules.multiswap.providers

import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.modules.multiswap.ISwapFinalQuote
import io.horizontalsystems.bankwallet.modules.multiswap.ISwapQuote
import io.horizontalsystems.bankwallet.modules.multiswap.JupiterTradeData
import io.horizontalsystems.bankwallet.modules.multiswap.SwapFinalQuoteJupiter
import io.horizontalsystems.bankwallet.modules.multiswap.SwapQuoteJupiter
import io.horizontalsystems.bankwallet.modules.multiswap.sendtransaction.SendTransactionSettings
import io.horizontalsystems.bankwallet.modules.multiswap.settings.ISwapSetting
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Body
import java.math.BigDecimal
import android.util.Log

interface JupiterApi {
    @GET("v6/quote")
    suspend fun getQuote(
        @Query("inputMint") inputMint: String,
        @Query("outputMint") outputMint: String,
        @Query("amount") amount: BigDecimal,
        @Query("slippageBps") slippageBps: Int
    ): JupiterQuoteResponse

    @POST("v4/swap")
    suspend fun executeSwap(
        @Body swapRequest: SwapRequest
    ): SwapResponse
}

class JupiterProvider : IMultiSwapProvider {
    private val jupiterApi: JupiterApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.jupiter.ag/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        jupiterApi = retrofit.create(JupiterApi::class.java)
    }

    override val id = "jupiter"
    override val title = "Jupiter Routing"
    override val url = "https://jupiter.ag"
    override val icon = R.drawable.raydium
    override val priority = 0

    override fun supports(blockchainType: BlockchainType): Boolean {
        return blockchainType == BlockchainType.Solana
    }

    override suspend fun fetchQuote(
        tokenIn: Token,
        tokenOut: Token,
        amountIn: BigDecimal,
        settings: Map<String, Any?>
    ): ISwapQuote {
        val tokenInAddress = getTokenAddress(tokenIn)
        val tokenOutAddress = getTokenAddress(tokenOut)
        val slippageBps = settings["slippageBps"] as? Int ?: 500 // Default to 500 bps (5%)

        Log.d("JupiterProvider", "Requesting quote: inputMint=$tokenInAddress, outputMint=$tokenOutAddress, amount=$amountIn, slippageBps=$slippageBps")

        val response = jupiterApi.getQuote(tokenInAddress, tokenOutAddress, amountIn, slippageBps)

        Log.d("JupiterProvider", "Response: success=${response.success}, data=${response.data}")

        if (!response.success) {
            throw Exception("No route found for the swap from ${tokenIn.type} to ${tokenOut.type}.")
        }

        return SwapQuoteJupiter(
            jupiterTradeData = JupiterTradeData(
                amountOut = response.data.outputAmount.toBigDecimal(),
                priceImpact = response.data.priceImpactPct.toBigDecimal(),
                slippageBps = response.data.slippageBps,
                computeUnitPriceMicroLamports = (response.data.computeUnitPriceMicroLamports ?: BigDecimal.ZERO).toString()
            ),
            fields = listOf(),
            settings = settings as List<ISwapSetting>,
            tokenIn = tokenIn,
            tokenOut = tokenOut,
            amountIn = amountIn,
            actionRequired = null
        )
    }

    override suspend fun fetchFinalQuote(
        tokenIn: Token,
        tokenOut: Token,
        amountIn: BigDecimal,
        swapSettings: Map<String, Any?>,
        sendTransactionSettings: SendTransactionSettings?
    ): ISwapFinalQuote {
        val swapRequest = SwapRequest(
            inputMint = getTokenAddress(tokenIn),
            outputMint = getTokenAddress(tokenOut),
            amount = amountIn
        )

        val transactionResponse = jupiterApi.executeSwap(swapRequest)

        return SwapFinalQuoteJupiter(
            tokenIn = tokenIn,
            tokenOut = tokenOut,
            amountIn = amountIn,
            amountOut = transactionResponse.amountOut,
            amountOutMin = transactionResponse.amountOutMin,
            transaction = transactionResponse.transactionId,
            priceImpact = transactionResponse.priceImpact,
            fields = listOf()
        )
    }

    private fun getTokenAddress(token: Token): String {
        return when (val tokenType = token.type) {
            is TokenType.Spl -> tokenType.address
            TokenType.Native -> "So11111111111111111111111111111111111111112" // SOL address
            else -> throw IllegalStateException("Unsupported tokenType: $tokenType")
        }
    }
}

data class JupiterQuoteResponse(
    val success: Boolean,
    val data: JupiterQuoteData
)

data class JupiterQuoteData(
    val inputMint: String,
    val inputAmount: String,
    val outputMint: String,
    val outputAmount: String,
    val priceImpactPct: Double,
    val slippageBps: Int,
    val computeUnitPriceMicroLamports: BigDecimal?,
    val routePlan: List<RoutePlan>
)

data class RoutePlan(
    val poolId: String,
    val inputMint: String,
    val outputMint: String,
    val feeMint: String,
    val feeRate: Int,
    val feeAmount: String,
    val remainingAccounts: List<String>
)

data class SwapRequest(
    val inputMint: String,
    val outputMint: String,
    val amount: BigDecimal
)

data class SwapResponse(
    val amountOut: BigDecimal,
    val amountOutMin: BigDecimal,
    val transactionId: String,
    val priceImpact: BigDecimal?
)