package com.moneydance.modules.features.formula.reminder;

import com.infinitekind.moneydance.model.SplitTxn;
import com.moneydance.modules.features.formula.MDApi;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class FormulaTxn {
    private SplitTxn txn;
    private SplitTxnTableModel tableModel;
    private String A;
    private String B;
    private String C;

    private static ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("py");

    public FormulaTxn(SplitTxn txn, SplitTxnTableModel tableModel) {
        this.txn = txn;
        this.tableModel = tableModel;

        A = txn.getParameter("formula_a");
        B = txn.getParameter("formula_b");
        C = txn.getParameter("formula_c");
    }

    public void syncSettings() {
        long value = getAmount();
        txn.setAmount(value, value);
        txn.setParameter("formula_a", A);
        txn.setParameter("formula_b", B);
        txn.setParameter("formula_c", C);
    }

    private String addParentheses(String formula) {
        if (formula.contains("+") || formula.contains("-")) {
            return "(" + formula + ")";
        }
        return formula;
    }

    private String formula() {
        String add = StringUtils.isNoneBlank(C) ? ("+" + C) : "";
        if (StringUtils.isNoneBlank(A)  && StringUtils.isNoneBlank(B)) {
            return addParentheses(A) + "*" + addParentheses(B) + add;
        } else if (StringUtils.isNoneBlank(A)) {
            return A + add;
        } else if (StringUtils.isNoneBlank(B)) {
            return B + add;
        } else if (StringUtils.isNoneBlank(C)) {
            return C;
        } else {
            return null;
        }
    }

    public static Long toCents(Object value) {
        if (value instanceof Double) {
            return Math.round(100 * (Double) value);
        } else if (value instanceof Float) {
            return (long) Math.round(100 * (Float) value);
        } else if (value instanceof Long) {
            return 100 * (Long)value;
        } else if (value instanceof Integer) {
            return (long) (100 * (Integer) value);
        } else if (value instanceof BigInteger) {
            return 100 * ((BigInteger) value).longValue();
        } else {
            return null;
        }
    }

    private Object eval(String script) {
        if (script == null) {
            return null;
        }

        try {
            Bindings bindings = engine.createBindings();
            bindings.put("DAYS_IN_MONTH", LocalDate.now().lengthOfMonth());
            bindings.put("BALANCE", txn.getAccount().getBalance() / 100.0);

            bindings.putAll(tableModel.getCache());

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
                Pattern p = Pattern.compile("'([A-E][1-" + tableModel.getRowCount() + "])'");
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

    public long getAmount() {
        Long value = toCents(getCellValue('V'));
        return value != null ? value : txn.getValue();
    }

    public String getDescription() {
        String description = txn.getDescription();
        String formula = formula();
        if (formula != null) {
            description += " (" + formula + ")";
        }
        return description;
    }

    public String formatValue(Object valueOrError, boolean showNegativeOnly) {
        if (valueOrError != null && !(valueOrError instanceof Number)) {
            return valueOrError.toString();
        }

        Long value = toCents(valueOrError);

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

    public String toString() {
        return txn.getDescription();
    }

    public String getCellSource(char col) {
        switch (col) {
            case 'A': return A;
            case 'B': return B;
            case 'C': return C;
            default: return null;
        }
    }

    public Object getCellValue(char col) {
        return eval('V' == col ? formula() : getCellSource(col));
    }

}
