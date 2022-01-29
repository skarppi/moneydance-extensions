package com.moneydance.modules.features

import com.infinitekind.moneydance.model.*
import com.infinitekind.util.CustomDateFormat
import com.moneydance.apps.md.controller.FeatureModuleContext
import com.moneydance.apps.md.controller.UserPreferences
import com.moneydance.apps.md.view.gui.MoneydanceGUI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MDApi(val context: FeatureModuleContext, val gui: MoneydanceGUI) {
    init {
        Companion.baseCurrencyType = book.currencies.baseType
    }

    fun getInvestmentTransactions(accountName: String): TxnSet {
        val account = context.rootAccount.getAccountByName(accountName, Account.AccountType.INVESTMENT)
        return book.transactionSet.getTransactionsForAccount(account)
    }

    val dateTimeFormat: String
        get() = gui.preferences.shortDateFormat + " " + gui.preferences.timeFormat

    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    val book: AccountBook
        get() = context.currentAccountBook

    fun toDate(date: LocalDateTime): Int {
        return date.format(formatter).toInt()
    }

    fun getReminders(isFormulaEnabled: Boolean): List<Reminder> {
        return book.reminders.allReminders
            .filter { reminder: Reminder -> isFormulaEnabled == reminder.getBooleanParameter(ENABLED_KEY, false) }
    }

    val shortDateFormatter: CustomDateFormat
        get() = gui.preferences.shortDateFormatter

    companion object {
        const val ENABLED_KEY = "formula"
        private var baseCurrencyType: CurrencyType? = null
        private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        val decimalChar: Char
            get() = UserPreferences.getInstance().decimalChar

        @JvmStatic
        fun formatCurrency(value: Long): String {
            return baseCurrencyType!!.formatFancy(value, decimalChar)
        }

        @JvmStatic
        fun parseDate(date: Int): LocalDate {
            return LocalDate.parse(date.toString(), formatter)
        }

        @JvmStatic
        @JvmOverloads
        fun log(msg: String, error: Throwable? = null) {
            println("moneydance-formula: $msg")
            if (error != null) {
                println(error.message)
                //            error.printStackTrace();
            }
        }
    }
}