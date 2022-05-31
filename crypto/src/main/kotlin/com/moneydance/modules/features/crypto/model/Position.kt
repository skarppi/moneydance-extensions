package com.moneydance.modules.features.crypto.model

import com.infinitekind.moneydance.model.AbstractTxn
import com.moneydance.modules.features.crypto.services.BinanceAccount
import com.moneydance.modules.features.crypto.services.BinanceOperation
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class BinanceAllStatement(
    val User_ID: Int,
    val UTC_Time: String,
    val Account: String,
    val Operation: String,
    val Coin: String,
    val Change: BigDecimal,
    val Remark: String
)

data class BinanceTxn(
    val date: LocalDateTime,
    val account: BinanceAccount,
    val operation: BinanceOperation,
    val coin: String,
    val amount: Double,
    val existingTxn: AbstractTxn? = null,
    val existingTxnStatus: String? = null
) {
    val cents get() = (amount * 100).toLong()

    val description = when(coin) {
        "ADA" -> "Cardano"
        else -> coin
    }

    val moneydanceSubAccount = when(coin) {
        "ADA" -> "${account.toMDAccount()}:Cardano"
        else -> "${account.toMDAccount()}:$coin"
    }

    val memo = if (amount >= 0) "Osto2 ${amount}" else "Myynti2 ${amount}"
}

data class CryptoTxn(
    val date: LocalDateTime,
    val account: BinanceAccount,
    val sourceLines: MutableList<BinanceTxn>,

//    val gain: BigDecimal,
//    val sellPrice: Float?,
    val existingTxn: AbstractTxn? = null,
    val existingTxnStatus: String? = null
) {
    fun transferType() =
        sourceLines.firstOrNull()?.operation?.toMDTransferType()
}

data class Position(
    val date: Date,
    val account: String,
    val amount: Float,
    val gainCents: Float,
    val coin: String,
    val sellPrice: Float,
    val fees: List<Fee>
)

data class Fee(
    val date: Date,
    val gainCents: Float,
    val amount: Float,
    val coin: String,
    val sellPrice: Float,
    val fees: List<Fee>

)
