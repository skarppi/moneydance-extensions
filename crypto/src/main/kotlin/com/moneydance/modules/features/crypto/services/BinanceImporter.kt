package com.moneydance.modules.features.crypto.services

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.infinitekind.moneydance.model.AbstractTxn
import com.infinitekind.moneydance.model.AbstractTxn.TRANSFER_TYPE_BANK
import com.infinitekind.moneydance.model.AbstractTxn.TRANSFER_TYPE_BUYSELL
import com.infinitekind.moneydance.model.CurrencyUtil
import com.infinitekind.moneydance.model.InvestTxnType
import com.infinitekind.moneydance.model.ParentTxn
import com.moneydance.modules.features.MDApi
import com.moneydance.modules.features.crypto.model.BinanceTxn
import com.moneydance.modules.features.crypto.model.CryptoTxn
import com.moneydance.modules.features.short
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

enum class BinanceAccount(val binanceName: String) {
    Spot("Spot"),
    Futures("USDT-Futures");

    fun toMDAccount(): String {
        return when (this) {
            Spot -> "Binance EUR"
            Futures -> "Binance Futures"
        }
    }

    companion object {
        fun binanceValueOf(binanceName: String) = values().find { e -> e.binanceName == binanceName }
            ?: throw IllegalArgumentException("$binanceName not valid binance account name")
    }
}

enum class BinanceOperation(val binanceName: String) {
    Deposit("Deposit"),
    TransferIn("transfer_in"),
    TransferOut("transfer_out"),
    Txn("Transaction Related"),
    OTC("Large OTC trading"),
    Buy("Buy"),
    Fee("Fee");

    fun toMDTransferType(): String? {
        return when(this) {
            Deposit, TransferIn, TransferOut -> TRANSFER_TYPE_BANK
            Txn, OTC, Buy -> TRANSFER_TYPE_BUYSELL
            else -> null
        }
    }

    fun toMDInvestmentType(value: Double): InvestTxnType? {
        return when(this) {
            Deposit, TransferIn, TransferOut -> InvestTxnType.BANK
            Txn, OTC, Buy -> if (value < 0) InvestTxnType.SELL else InvestTxnType.BUY
            else -> null
        }
    }

    companion object {
        fun binanceValueOf(binanceName: String) = values().find { e -> e.binanceName == binanceName }
            ?: throw IllegalArgumentException("$binanceName not valid binance operation name")
    }
}

class BinanceImporter(val api: MDApi) {
    // 2021-09-06 11:03:25
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneOffset.UTC)

    private val transactions = mutableListOf<CryptoTxn>()

    private val existingTxns = BinanceAccount.values().associateWith { api.getInvestmentTransactions(it.toMDAccount()) }

    init {
        val file = File("../short.csv")
        val rows = csvReader().readAll(file)
            .drop(1) // remove header
            .take(10)

        for(i in rows.indices) {
            val (utcTime, accountName, operation, coin, change) = rows[i]
                .drop(1) // drop the account id
                .take(7) // take

            val utcInstant: Instant = Instant.from(formatter.parse(utcTime))
            val helsinkiTime = LocalDateTime.ofInstant(utcInstant, ZoneId.of("Europe/Helsinki"))

            try {
                val row = BinanceTxn(
                    date = helsinkiTime,
                    account = BinanceAccount.binanceValueOf(accountName),
                    operation = BinanceOperation.binanceValueOf(operation),
                    amount = change.toDouble(),
                    coin = coin
                )

                parseRow(row)
            } catch(e: Exception) {
                MDApi.logError(e)
            }
        }
    }

    private fun txnToString(txn: AbstractTxn): String {
        return List(txn.otherTxnCount) { split ->
            val value = txn.getOtherTxn(split).value / 100
            val account = txn.getOtherTxn(split).account

            "${-value}${account.currencyType.prefix} from $account"
        }.joinToString(", ")
    }

    private fun transferHandler(row: BinanceTxn, existingTxn: AbstractTxn): BinanceTxn? {
        if (row.operation == BinanceOperation.Deposit && row.coin == "EUR") {
            // deposited EUR value must match the Charge minus Fees
            val otherSideValue = List(existingTxn.otherTxnCount) { split ->
                existingTxn.getOtherTxn(split).value
            }.sum()

            MDApi.log(otherSideValue.toString())

            if (-otherSideValue == row.cents) {
                MDApi.log("found $existingTxn")
                return row.copy(
                    existingTxn = existingTxn,
                    existingTxnStatus = txnToString(existingTxn)
                )
            }
        }

        if (row.operation in listOf(BinanceOperation.TransferIn, BinanceOperation.TransferOut)) {
            val direction = if (row.operation == BinanceOperation.TransferIn) 1 else -1
            if (direction * existingTxn.value == row.cents) {
                MDApi.log("found $existingTxn")
                return row.copy(
                    existingTxn = existingTxn,
                    existingTxnStatus = txnToString(existingTxn)
                )
            }
        }

        return null
    }

    private fun buySellHandler(row: BinanceTxn, existingTxn: AbstractTxn): BinanceTxn? {
        if (row.operation == BinanceOperation.Txn) {
            if (existingTxn.getOtherTxn(0).account.accountName.startsWith("${row.coin}-USDT")) {
                return null
            }

            val shares = existingTxn.getOtherTxn(0).value / 100000000.0

            if (shares == row.amount) {
                MDApi.log("found $existingTxn")

                val valueEur =  CurrencyUtil.convertValue(existingTxn.value, existingTxn.account.currencyType, MDApi.baseCurrencyType, existingTxn.dateInt)
                val coin = existingTxn.getOtherTxn(0).account.currencyType.short()

                return row.copy(
                    existingTxn = existingTxn,
                    existingTxnStatus = if ((existingTxn as ParentTxn).investTxnType == InvestTxnType.BUY) {
                        "Buy ${MDApi.formatCurrency(valueEur)} => $shares $coin"
                    } else {
                        "Sell $shares $coin => ${MDApi.formatCurrency(valueEur)}"
                    }
                )
            }
        }
        return null
    }


    private fun parseRow(row: BinanceTxn) {

        MDApi.log(row)
        val date = api.toDate(row.date)

        val rowWithTxn = existingTxns[row.account]
            ?.filter {
                with(it) {
                    dateInt == date && transferType == row.operation.toMDTransferType()
                }
            }?.mapNotNull {
                transferHandler(row, it)
                    ?: buySellHandler(row, it)
            }
            ?.firstOrNull()
            ?: row

        val lastTxn = transactions.lastOrNull()
        if (lastTxn != null && lastTxn.date == row.date) {
            // new split
            if (rowWithTxn.amount < 0) {
                // source is the first
                lastTxn.sourceLines.add(0, rowWithTxn)
            } else {
                lastTxn.sourceLines.add(rowWithTxn)
            }

            transactions.removeLast()
            transactions.add(lastTxn.copy(
                existingTxn = lastTxn.existingTxn ?: rowWithTxn.existingTxn,
                existingTxnStatus = lastTxn.existingTxnStatus ?: rowWithTxn.existingTxnStatus,
            ))
        } else {
            // new transaction
            val txn = CryptoTxn(
                row.date,
                row.account,
                mutableListOf(rowWithTxn),
                rowWithTxn.existingTxn,
                rowWithTxn.existingTxnStatus)
            transactions.add(txn)
        }

//        val transferType = when(operation) {
//            "Deposit" -> TRANSFER_TYPE_BANK
//            "Transaction Related" -> TRANSFER_TYPE_BUYSELL
//            "Large OTC trading" -> TRANSFER_TYPE_BUYSELL
////            "Fee" ->
//            else -> {
//                MDApi.log("unknown operation ${operation}, accountName=${accountName}, coin=${coin}, utcTime=${utcTime}, change=${change}")
//                null
//            }
//        }

//                && (transferType?.equals(txn.transferType) ?: true)
//        return cryptoTxn
    }

    fun transactions(): List<CryptoTxn> {
        return transactions
    }
}