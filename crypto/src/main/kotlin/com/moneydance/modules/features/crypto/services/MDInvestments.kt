package com.moneydance.modules.features.crypto.services

import com.infinitekind.moneydance.model.*
import com.moneydance.modules.features.MDApi
import com.moneydance.modules.features.crypto.model.BinanceTxn
import com.moneydance.modules.features.crypto.model.CryptoTxn

class MDInvestments {
    companion object {
        fun buySell(txn: CryptoTxn, api: MDApi) {
            val source = txn.sourceLines.first()
            val target = txn.sourceLines.last()
            if (txn.sourceLines.size != 2) {
                MDApi.log("Buy/sell must have 2 transactions")
                return
            }

            val date = api.toDate(txn.date)
            val account = api.book.rootAccount.getAccountByName(txn.account.toMDAccount())


            if ( source.coin == "EUR" || target.coin == "EUR") {
                // direct buy from/to EUR
                registerTxn(source, target,
                    "${source.coin} -> ${target.coin}",
                    target.memo,
                    date, account, api)
            } else {
                if (source.coin == "USDT") {
                    val usd = MDApi.currencies.get("USD")
                    val usdToEur = CurrencyUtil.getRawRate(usd, MDApi.baseCurrencyType, date)
                    registerTxn(
                        source,
                        source.copy(
                            amount = source.amount
                        ),
                        "USDT -> ${target.coin}",
                        "Osa 1: myynti ${source.amount} USDT euroiksi",
                        date,
                        account,
                        api)

                    registerTxn(source.copy(
                            amount = -source.amount * usdToEur
                        ), target,
                        "USDT -> ${target.coin}",
                        "Osa 2: osto ${source.amount} euroilla",
                        date, account, api)

//                } else if(target.coin == "USDT") {
//                    val valueEur =  CurrencyUtil.convertValue(source.moneydanceAmount, source.security, MDApi.baseCurrencyType, date)
//                    // check value in
                } else {
                    MDApi.log("Transaction $source -> $target cannot be converted to EUR")
                }
            }



        }

        fun registerTxn(source: BinanceTxn,
                        target: BinanceTxn,
                        description: String,
                        memo: String,
                        date: Int,
                        account: Account,
                        api: MDApi) {

            val parentTxn = ParentTxn.makeParentTxn(api.book,
                date,
                date,
                System.currentTimeMillis(),
                "",
                account,
                description,
                memo,
                -1,
                AbstractTxn.ClearedStatus.UNRECONCILED.legacyValue()
            )

            parentTxn.investTxnType = source.operation.toMDInvestmentType(target.amount)

            val category = account.getAccountByName(target.moneydanceSubAccount)

            val splitTxn = SplitTxn.makeSplitTxn(parentTxn,
                -source.cents,
                target.moneydanceAmount,
                1.0,
                category,
                target.description,
                -1,
                ParentTxn.STATUS_UNRECONCILED
            )
            splitTxn.setParameter(AbstractTxn.TAG_INVST_SPLIT_TYPE, AbstractTxn.TAG_INVST_SPLIT_SEC)

            parentTxn.addSplit(splitTxn)

            api.book.transactionSet.addNewTxn(parentTxn)
        }

    }
}