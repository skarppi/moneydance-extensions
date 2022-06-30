package com.moneydance.modules.features.crypto.model

import com.infinitekind.moneydance.model.AbstractTxn
import com.moneydance.modules.features.MDApi
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
    val cents
        get() = (amount * 100).toLong()

    val security
        get() = MDApi.securities.get(when(coin) {
            else -> coin
        })
            //?: return MDApi.log("$coin not found")

    val description = when(coin) {
        else -> security?.name ?: coin
    }

    val moneydanceSubAccount = "${account.toMDAccount()}:${security?.name ?: coin}"

    val memo = if (amount >= 0) "Osto ${amount}" else "Myynti ${amount}"

    val moneydanceAmount = security?.getLongValue(amount) ?: Math.round(amount * 100)
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
