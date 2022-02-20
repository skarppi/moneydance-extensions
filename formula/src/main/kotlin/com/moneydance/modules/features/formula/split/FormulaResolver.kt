package com.moneydance.modules.features.formula.split

import com.moneydance.modules.features.MDApi.Companion.log
import java.time.LocalDate
import javax.script.ScriptException
import javax.script.ScriptEngineManager
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern

const val NAME_ERROR = "#NAME?"

class FormulaResolver {

    private val cache = HashMap<String, Any?>()

    fun getValue(column: CellCol, splitIndex: Int): Any? {
        return cache[column.name + (splitIndex + 1)]
    }

    fun getValue(splitIndex: Int): Any? {
        return getValue(CellCol.V, splitIndex)
    }

    // resolve and cache results for all transactions
    fun resolve(transactions: List<FormulaSplitTxn>, nextPayment: LocalDate?) {
        cache.clear()
        val processingQueue = initializeCellProcessingQueue(transactions, nextPayment)
        while (!processingQueue.isEmpty()) {
            val cell = processingQueue.poll()
            val value = eval(cell)
            if (value is String && value.startsWith("$NAME_ERROR ")) {
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
    ): Queue<Cell> =
        transactions.fold(LinkedList()) { processingQueue, split ->
            val row = split.splitIndex + 1

            // add global values not depending on others
            cache["BALANCE$row"] = split.txn.account.balance / 100.0
            nextPayment?.let { date ->
                cache["DAYS_IN_PREVIOUS_MONTH"] = date.minusMonths(1).lengthOfMonth()
                cache["DAYS_IN_MONTH"] = date.lengthOfMonth()
            }

            // processing queue handles values for each cell trying to handle dependencies in correct order
            processingQueue.addAll(
                CellCol.values().map { Cell(it, row, split) }
            )
            processingQueue
        }

    private fun eval(cell: Cell): Any? {
        val script = cell.script ?: return null
        return try {
            val bindings = engine.createBindings()

            // own balance available for calculations
            bindings["BALANCE"] = cell.txn.txn.account.balance / 100.0

            // all currently calculated values are available
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
                    "$NAME_ERROR ${matcher.group(1)}"
                } else NAME_ERROR
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