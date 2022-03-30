package com.moneydance.modules.features.crypto.model

import com.infinitekind.moneydance.model.AbstractTxn
import com.infinitekind.moneydance.model.ParentTxn
import com.moneydance.modules.features.crypto.importer.BinanceAccount
import com.moneydance.modules.features.crypto.importer.BinanceOperation
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
}

data class CryptoTxn(
    val date: LocalDateTime,
    val account: BinanceAccount,
    val sourceLines: MutableList<BinanceTxn>,

//    val gain: BigDecimal,
//    val sellPrice: Float?,
    val existingTxn: AbstractTxn? = null,
    val existingTxnStatus: String? = null
)

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
