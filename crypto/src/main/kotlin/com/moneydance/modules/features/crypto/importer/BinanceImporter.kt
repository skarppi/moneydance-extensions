package com.moneydance.modules.features.crypto.importer

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.infinitekind.moneydance.model.AbstractTxn
import com.moneydance.modules.features.MDApi
import com.moneydance.modules.features.crypto.model.CryptoTxn
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

enum class BinanceAccount(val value: String) {
    Spot("Spot"),
    Futures("USDT-Futures");

    fun toMDAccount(): String {
        return when (this) {
            Spot -> "Binance"
            Futures -> "Binance Futures"
        }
    }
}

enum class BinanceOperations(val op: String) {
    Deposit("Xfr"),
    Txn("Transaction Related"),
    OTC("Large OTC trading"),
    Fee("Fee");

    fun toMDTransferType(): String? {
        return when(this) {
            Deposit -> AbstractTxn.TRANSFER_TYPE_BANK
            Txn -> AbstractTxn.TRANSFER_TYPE_BUYSELL
            OTC -> AbstractTxn.TRANSFER_TYPE_BUYSELL
            else -> null
        }
    }
}

class BinanceImporter(val api: MDApi) {
    // 2021-09-06 11:03:25
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneOffset.UTC)

    val transactions = mutableListOf<CryptoTxn>()

    val existingTxns = BinanceAccount.values()
        .map { it to api.getInvestmentTransactions(it.toMDAccount()) }
        .toMap()

    init {
        val file = File("../short.csv")
        val rows = csvReader().readAll(file).drop(1).take(5)

        for(i in rows.indices) {
            val txn = parseRow(rows[i])
            transactions.add(txn)
        }
    }

    fun parseRow(row: List<String>): CryptoTxn {
        val (utcTime, accountName, operation, coin, change) = row.drop(1).take(7)

        val utcInstant: Instant = Instant.from(formatter.parse(utcTime))
        val helsinkiTime = LocalDateTime.ofInstant(utcInstant, ZoneId.of("Europe/Helsinki"))
        val date = api.toDate(helsinkiTime)

        val account = BinanceAccount.valueOf(accountName)

        return CryptoTxn(
            date = helsinkiTime,
            account = account.toMDAccount(),
            operation = operation,
            amount = change.toFloat(),
            coin = coin,
            existingTxn = existingTxns[account]?.find { txn -> txn.dateInt === date}
//            sellPrice = 0.0
        )
    }

    fun transactions(): List<CryptoTxn> {
        return transactions
    }
}