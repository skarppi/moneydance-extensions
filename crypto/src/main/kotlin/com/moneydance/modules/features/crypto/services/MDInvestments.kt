package com.moneydance.modules.features.crypto.services

import com.infinitekind.moneydance.model.AbstractTxn
import com.infinitekind.moneydance.model.ParentTxn
import com.infinitekind.moneydance.model.SplitTxn
import com.moneydance.modules.features.MDApi
import com.moneydance.modules.features.crypto.model.CryptoTxn

class MDInvestments {
    companion object {
        fun buySell(txn: CryptoTxn, api: MDApi) {
            val date = api.toDate(txn.date)

            val source = txn.sourceLines.first()
            val target = txn.sourceLines.last()
            if (txn.sourceLines.size != 2) {
                MDApi.log("Buy/sell must have 2 transactions")
                return
            }

            val account = api.book.rootAccount.getAccountByName(txn.account.toMDAccount())

            val parentTxn = ParentTxn.makeParentTxn(api.book,
                date,
                date,
                System.currentTimeMillis(),
                "",
                account,
                target.description,
                target.memo,
                -1,
                AbstractTxn.ClearedStatus.UNRECONCILED.legacyValue()
            )

            parentTxn.investTxnType = source.operation.toMDInvestmentType(target.amount)

            val splitTxn = SplitTxn.makeSplitTxn(parentTxn,
                -source.cents,
                target.moneydanceAmount,
                1.0,
                account.getAccountByName(target.moneydanceSubAccount),
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