package com.moneydance.modules.features.formula.split;

import com.moneydance.modules.features.formula.MDApi;
import lombok.Getter;
import lombok.Setter;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FormulaResolver {
    @Getter
    private HashMap<String, Object> cache = new HashMap<>();

    private static ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("py");

    public Object getValue(int splitIndex) {
        return cache.get("V" + (splitIndex + 1));
    }

    // resolve and cache results for all transactions
    public void resolve(List<FormulaSplitTxn> transactions, LocalDate nextPayment) {
        cache.clear();

        Queue<Cell> processingQueue = initializeCellProcessingQueue(transactions, nextPayment);

        while(!processingQueue.isEmpty()) {

            Cell cell = processingQueue.poll();

            Object value = eval(cell);
            if (value instanceof String && ((String) value).startsWith("#NAME? ")) {
                String cellMissing = ((String) value).substring(7);

                // keep track of dependencies to prevent infinite loops
                cell.getDeps().add(cellMissing);

                if (processingQueue.stream().allMatch(c -> c.isLoop())) {
                    // all rows are in a loop, give up and show errors
                    cache.put(cell.getCell(), value);
                } else {
                    // try again later when dependency is ready
                    processingQueue.offer(cell);
                }
            } else {
                cache.put(cell.getCell(), value);
            }
        }
    }

    private Queue<Cell> initializeCellProcessingQueue(List<FormulaSplitTxn> transactions, LocalDate nextPayment) {
        Queue<Cell> processingQueue = new LinkedList<>();
        IntStream.rangeClosed(1, transactions.size()).forEach(row -> {
            FormulaSplitTxn split = transactions.get(row - 1);

            cache.put("BALANCE" + row, split.getTxn().getAccount().getBalance() / 100.0);
            cache.put("DAYS_IN_PREVIOUS_MONTH", nextPayment.minusMonths(1).lengthOfMonth());
            cache.put("DAYS_IN_MONTH", nextPayment.lengthOfMonth());

            Arrays.asList('A', 'B', 'C', 'V').forEach(col ->
                    processingQueue.add(Cell.builder()
                            .cell("" + col + row)
                            .col(col)
                            .row(row)
                            .txn(split)
                            .deps(new ArrayList())
                            .build())
            );
        });
        return processingQueue;
    }

    private Object eval(Cell cell) {
        String script = cell.getScript();

        if (script == null) {
            return null;
        }

        try {
            Bindings bindings = engine.createBindings();
            bindings.put("BALANCE", cell.getTxn().getTxn().getAccount().getBalance() / 100.0);

            bindings.putAll(cache);

            if (script.equals("?")) {
                return bindings.keySet().stream().sorted().collect(Collectors.toList()).toString();
            }

            return engine.eval(script.replace("%", "/100.0"), bindings);
        } catch (ScriptException e) {
            MDApi.log("Failed to evaluate script " + script, e);

            if (e.getMessage().startsWith("TypeError:")) {
                return "#N/A!";
            }
            if (e.getMessage().startsWith("NameError:")) {
                int maxRow = cell.getTxn().getTxn().getParentTxn().getSplitCount();

                Pattern p = Pattern.compile("'([A-C,V][1-" + maxRow + "])'");
                Matcher matcher = p.matcher(e.getMessage());
                if (matcher.find()) {
                    return "#NAME? " + matcher.group(1);
                }
                return "#NAME?";
            }
            if (e.getMessage().startsWith("ZeroDivisionError:")) {
                return "#DIV/0!";
            }
            if (e.getMessage().startsWith("SyntaxError:")) {
                return "#NUM!";
            }

            return e.getMessage();
        } catch (Exception e) {
            MDApi.log("Failed to evaluate script " + script, e);
            return e.getMessage();
        }
    }
}
