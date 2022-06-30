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
        baseCurrencyType = book.currencies.baseType
        currencies = getCurrencies(CurrencyType.Type.CURRENCY)
        securities = getCurrencies(CurrencyType.Type.SECURITY)
    }

    fun getInvestmentTransactions(accountName: String): TxnSet {
        val account = context.rootAccount.getAccountByName(accountName, Account.AccountType.INVESTMENT)
        return book.transactionSet.getTransactionsForAccount(account)
    }

    fun getCurrencies(type: CurrencyType.Type): Map<String, CurrencyType> =
        book.currencies
            .allCurrencies
            .filter { c -> c.currencyType == type }
            .map { c -> c.short() to c}
            .toMap()

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
        var baseCurrencyType: CurrencyType? = null
        var currencies: Map<String, CurrencyType> = emptyMap()
        var securities: Map<String, CurrencyType> = emptyMap()

        private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        val decimalChar: Char
            get() = UserPreferences.getInstance().decimalChar

        fun formatCurrency(value: Long): String {
            return baseCurrencyType!!.formatFancy(value, decimalChar)
        }

        fun formatCurrency(value: Long, currencyType: CurrencyType): String {
            return currencyType.formatFancy(value, decimalChar)
        }

        fun parseDate(date: Int): LocalDate {
            return LocalDate.parse(date.toString(), formatter)
        }

        fun logError(error: Throwable? = null) {
            println("${StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass}: ${error?.message}")
            error?.printStackTrace();
        }

        fun log(vararg vars:Any) {
            vars.mapNotNull { v -> v as? Throwable }.map(this::logError)

            val nonThrowables = vars.filterNot { v -> v is Throwable }

            println("${StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass}: ${nonThrowables.joinToString(" ")}")
        }
    }
}

fun CurrencyType.short() = idString.split("-").first()