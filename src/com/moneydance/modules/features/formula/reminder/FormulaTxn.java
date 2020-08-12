package com.moneydance.modules.features.formula.reminder;

import com.infinitekind.moneydance.model.SplitTxn;
import com.moneydance.modules.features.formula.MDApi;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class FormulaTxn {
    private SplitTxn txn;
    private SplitTxnTableModel tableModel;
    private String A;
    private String B;

    private static ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("py");

    public FormulaTxn(SplitTxn txn, SplitTxnTableModel tableModel) {
        this.txn = txn;
        this.tableModel = tableModel;

        A = txn.getParameter("formula_a");
        B = txn.getParameter("formula_b");
    }

    public void syncSettings() {
        long value = calculateAmountInCents();
        txn.setAmount(value, value);
        txn.setParameter("formula_a", A);
        txn.setParameter("formula_b", B);
    }

    private String addParentheses(String formula) {
        if (formula.contains("+") || formula.contains("-")) {
            return "(" + formula + ")";
        }
        return formula;
    }

    private String formula() {
        if (StringUtils.isNoneBlank(A)  && StringUtils.isNoneBlank(B)) {
            return addParentheses(A) + "*" + addParentheses(B);
        } else if (StringUtils.isNoneBlank(A)) {
            return A;
        } else if (StringUtils.isNoneBlank(B)) {
            return B;
        } else {
            return null;
        }
    }

    public long calculateAmountInCents() {
        String formula = formula();

        if (formula == null) {
            return txn.getValue();
        }

        Pair<Long, String> value = centsOrError(formula);
        return value.getLeft() != null ? value.getLeft() : 0;
    }

    private Pair<Long, String> centsOrError(String script) {
        Object value = eval(script);

        if (value instanceof Double) {
            return Pair.of(Math.round(100 * (Double) value), null);
        } else if (value instanceof Float) {
            return Pair.of((long) Math.round(100 * (Float) value), null);
        } else if (value instanceof Long) {
            return Pair.of(100 * (Long)value, null);
        } else if (value instanceof Integer) {
            return Pair.of((long) (100 * (Integer) value), null);
        } else if (value instanceof BigInteger) {
            return Pair.of(100 * ((BigInteger) value).longValue(), null);
        } else {
            // MDApi.log("Unknown value type " + (value != null ? value.getClass() : null));
            return Pair.of(null, (value != null ? value.toString() : null));
        }
    }

    private Object cellValue(char column, int row) {
        FormulaTxn txn = tableModel.getTransactions().get(row - 1);

        if (txn == this) {
            return "#REF!";
        } else {
            return txn.eval('A' == column ? txn.A : txn.B);
        }
    }

    private Object eval(String script) {
        if (script == null) {
            return null;
        }

        try {
            Bindings bindings = engine.createBindings();
            bindings.put("DAYS_IN_MONTH", LocalDate.now().lengthOfMonth());
            bindings.put("BALANCE", txn.getAccount().getBalance());

            IntStream.rangeClosed(1, tableModel.getRowCount()).forEach(row -> {
                bindings.put("A" + row, script.contains("A" + row) ? cellValue('A', row) : null);
                bindings.put("B" + row, script.contains("B" + row) ? cellValue('B', row) : null);
            });

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

    public String getDescription() {
        String description = txn.getDescription();
        String formula = formula();
        if (formula != null) {
            description += " (" + formula + ")";
        }
        return description;
    }

    private String format(Pair<Long, String> valueOrError, boolean showNegativeOnly) {
        if (valueOrError.getRight() != null) {
            return valueOrError.getRight();
        }

        Long value = valueOrError.getLeft();

        long defaultValue = txn.getValue();
        if (value == null) {
            value = defaultValue;
        }

        if (showNegativeOnly && value < 0) {
            // render values as positive
            value *= -1;
            defaultValue *= -1;
        } else if (showNegativeOnly || value < 0) {
            return "";
        }

        if (value != defaultValue) {
            return String.format("%s (was %s)",
                    MDApi.formatCurrency(value),
                    MDApi.formatCurrency(defaultValue));
        } else {
            return MDApi.formatCurrency(value);
        }
    }

    public String formatPayment() {
        return format(centsOrError(formula()), false);
    }

    public String formatDeposit() {
        return format(centsOrError(formula()), true);
    }

    public String toString() {
        return txn.getDescription();
    }

    public void setA(String value) {
        A = value;
    }

    public void setB(String value) {
        B = value;
    }
}
