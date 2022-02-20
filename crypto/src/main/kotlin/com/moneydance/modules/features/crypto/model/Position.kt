package com.moneydance.modules.features.crypto.model

import com.infinitekind.moneydance.model.AbstractTxn
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

data class CryptoTxn(
    val date: LocalDateTime,
    val account: String,
    val operation: String,
    val amount: Float,
//    val gain: BigDecimal,
    val coin: String,
//    val sellPrice: Float?,
    val remark: String? = null,
    val existingTxn: AbstractTxn? = null
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
