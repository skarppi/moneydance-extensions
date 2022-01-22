package com.moneydance.modules.features.formula.split

import com.moneydance.modules.features.MDApi.Companion.formatCurrency
import com.infinitekind.moneydance.model.SplitTxn
import org.apache.commons.lang3.StringUtils
import java.math.BigInteger
import kotlin.math.roundToLong

class FormulaSplitTxn(private val splitIndex: Int, val txn: SplitTxn, private val resolver: FormulaResolver) {
    var a: String? = txn.getParameter("formula_a")
    var b: String? = txn.getParameter("formula_b")
    var c: String? = txn.getParameter("formula_c")

    fun syncSettings() {
        val value = amount
        txn.setAmount(value, value)
        txn.setParameter("formula_a", a)
        txn.setParameter("formula_b", b)
        txn.setParameter("formula_c", c)
    }

    private fun addParentheses(formula: String): String {
        return if (formula.contains("+") || formula.contains("-")) {
            "($formula)"
        } else formula
    }

    private fun formula(): String? {
        val add = if (StringUtils.isNoneBlank(c)) "+$c" else ""
        return if (StringUtils.isNoneBlank(a) && StringUtils.isNoneBlank(b)) {
            addParentheses(a!!) + "*" + addParentheses(b!!) + add
        } else if (StringUtils.isNoneBlank(a)) {
            a + add
        } else if (StringUtils.isNoneBlank(b)) {
            b + add
        } else if (StringUtils.isNoneBlank(c)) {
            c
        } else {
            null
        }
    }

    val amount: Long
        get() {
            val value = toCents(resolver.getValue(splitIndex))
            return value ?: txn.value
        }
    val description: String
        get() {
            var description = txn.description
            val formula = formula()
            if (formula != null) {
                description += " ($formula)"
            }
            return description
        }

    fun formatValue(valueOrError: Any?, showNegativeOnly: Boolean): String {
        if (valueOrError != null && valueOrError !is Number) {
            return valueOrError.toString()
        }
        var value = toCents(valueOrError)
        var defaultValue = txn.value
        if (value == null) {
            value = defaultValue
        }
        if (showNegativeOnly && value < 0) {
            // render values as positive
            value *= -1
            defaultValue *= -1
        } else if (showNegativeOnly || value < 0) {
            return ""
        }
        return if (value != defaultValue) {
            String.format(
                "%s (was %s)",
                formatCurrency(value),
                formatCurrency(defaultValue)
            )
        } else {
            formatCurrency(value)
        }
    }

    override fun toString(): String {
        return txn.description
    }

    fun getCellSource(col: Char): String? {
        return when (col) {
            'A' -> a
            'B' -> b
            'C' -> c
            'V' -> formula()
            else -> null
        }
    }

    companion object {
        fun toCents(value: Any?): Long? {
            return when (value) {
                is Double -> (100 * value).roundToLong()
                is Float -> (100 * value).roundToLong()
                is Long -> 100 * value
                is Int -> (100 * value).toLong()
                is BigInteger -> 100 * value.toLong()
                else -> null
            }
        }
    }
}