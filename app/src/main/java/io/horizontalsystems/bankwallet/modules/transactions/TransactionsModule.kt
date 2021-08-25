package io.horizontalsystems.bankwallet.modules.transactions

import io.horizontalsystems.bankwallet.entities.Account
import io.horizontalsystems.bankwallet.entities.CoinSettings
import io.horizontalsystems.coinkit.models.Coin
import java.math.BigDecimal
import java.util.*

data class TransactionLockInfo(
    val lockedUntil: Date,
    val originalAddress: String,
    val amount: BigDecimal?
)

sealed class TransactionStatus {
    object Pending : TransactionStatus()
    class Processing(val progress: Double) : TransactionStatus() //progress in 0.0 .. 1.0
    object Completed : TransactionStatus()
    object Failed : TransactionStatus()
}

data class TransactionWallet(
    val coin: Coin?,
    val source: TransactionSource
)

data class TransactionSource(
    val blockchain: Blockchain,
    val account: Account,
    val coinSettings: CoinSettings
) {

    sealed class Blockchain {
        object Bitcoin : Blockchain()
        object Litecoin : Blockchain()
        object BitcoinCash : Blockchain()
        object Dash : Blockchain()
        object Ethereum : Blockchain()
        object Zcash : Blockchain()
        object BinanceSmartChain : Blockchain()
        class Bep2(val symbol: String) : Blockchain(){
            override fun hashCode(): Int {
                return this.symbol.hashCode()
            }
            override fun equals(other: Any?): Boolean {
                return when(other){
                    is Bep2 -> this.symbol == other.symbol
                    else -> false
                }
            }
        }

        fun getTitle(): String {
            return when(this){
                Bitcoin -> "Bitcoin"
                Litecoin -> "Litecoin"
                BitcoinCash -> "BitcoinCash"
                Dash -> "Dash"
                Ethereum -> "Ethereum"
                Zcash -> "Zcash"
                BinanceSmartChain -> "Binance Smart Chain"
                is Bep2 -> "Binance Chain"
            }
        }
    }

}
