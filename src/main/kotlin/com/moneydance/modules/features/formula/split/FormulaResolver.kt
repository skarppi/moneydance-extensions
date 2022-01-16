package com.moneydance.modules.features.formula.split

import com.moneydance.modules.features.formula.MDApi.Companion.log
import java.time.LocalDate
import javax.script.ScriptException
import javax.script.ScriptEngineManager
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern

class FormulaResolver {

    private val cache = HashMap<String, Any?>()

    fun getValue(column: String, splitIndex: Int): Any? {
        return cache[column + (splitIndex + 1)]
    }

    fun getValue(splitIndex: Int): Any? {
        return getValue("V", splitIndex)
    }

    // resolve and cache results for all transactions
    fun resolve(transactions: List<FormulaSplitTxn>, nextPayment: LocalDate?) {
        cache.clear()
        val processingQueue = initializeCellProcessingQueue(transactions, nextPayment)
        while (!processingQueue.isEmpty()) {
            val cell = processingQueue.poll()
            val value = eval(cell)
            if (value is String && value.startsWith("#NAME? ")) {
                val cellMissing = value.substring(7)

                // keep track of dependencies to prevent infinite loops
                cell.deps.add(cellMissing)
                if (processingQueue.all { it.isLoop }) {
                    // all rows are in a loop, give up and show errors
                    cache[cell.cell] = value
                } else {
                    // try again later when dependency is ready
                    processingQueue.offer(cell)
                }
            } else {
                cache[cell.cell] = value
            }
        }
    }

    private fun initializeCellProcessingQueue(
        transactions: List<FormulaSplitTxn>,
        nextPayment: LocalDate?
    ): Queue<Cell> {
        val processingQueue = LinkedList<Cell>()
        for (row in 1 until transactions.size) {
            val split = transactions[row - 1]
            cache["BALANCE$row"] = split.txn.account.balance / 100.0
            nextPayment?.let { date ->
                cache["DAYS_IN_PREVIOUS_MONTH"] = date.minusMonths(1).lengthOfMonth()
                cache["DAYS_IN_MONTH"] = date.lengthOfMonth()
            }
            listOf('A', 'B', 'C', 'V').forEach { col ->
                processingQueue.add(
                    Cell(
                        col = col,
                        row = row,
                        txn = split
                    )
                )
            }
        }
        return processingQueue
    }

    private fun eval(cell: Cell): Any? {
        val script = cell.script ?: return null
        return try {
            val bindings = engine.createBindings()
            bindings["BALANCE"] = cell.txn.txn.account.balance / 100.0
            bindings.putAll(cache)
            if (script == "?") {
                bindings.keys.sorted().toString()
            } else engine.eval(script.replace("%", "/100.0"), bindings)
        } catch (e: ScriptException) {
            log("Failed to evaluate script $script", e)
            val message = e.message ?: return null
            if (message.startsWith("TypeError:")) {
                return "#N/A!"
            }
            if (message.startsWith("NameError:")) {
                val maxRow: Int = cell.txn.txn.parentTxn.splitCount
                val p = Pattern.compile("'([A-C,V][1-$maxRow])'")
                val matcher = p.matcher(e.message)
                return if (matcher.find()) {
                    "#NAME? " + matcher.group(1)
                } else "#NAME?"
            }
            if (message.startsWith("ZeroDivisionError:")) {
                return "#DIV/0!"
            }
            if (message.startsWith("SyntaxError:")) {
                "#NUM!"
            } else message
        } catch (e: Exception) {
            log("Failed to evaluate script $script", e)
            e.message
        }
    }

    companion object {
        private val engine = ScriptEngineManager().getEngineByExtension("py")
    }
}