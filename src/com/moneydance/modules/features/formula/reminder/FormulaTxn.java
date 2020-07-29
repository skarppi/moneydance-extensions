package com.moneydance.modules.features.formula.reminder;

import com.infinitekind.moneydance.model.SplitTxn;
import com.moneydance.modules.features.formula.MDApi;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Data
public class FormulaTxn {
    private SplitTxn txn;
    private String A;
    private String B;

    private static ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("py");

    public FormulaTxn(SplitTxn txn) {
        this.txn = txn;
        A = txn.getParameter("formula_a");
        B = txn.getParameter("formula_b");
    }

    private void readSettings() {
    }

    public void syncSettings() {
        long value = calculateAmount();
        txn.setAmount(value, value);
        txn.setParameter("formula_a", A);
        txn.setParameter("formula_b", B);
    }

    private String replacePercent(String formula) {
        return formula.replace("%", "/100.0");
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

    public Long calculateAmount() {
        String formula = formula();
        try {
            Object value;
            if (formula != null) {
                value = engine.eval(replacePercent(formula));
            } else {
                return txn.getValue();
            }

            if (value instanceof Double) {
                return Math.round(100 * (Double) value);
            } else if (value instanceof Float) {
                return (long) Math.round(100 * (Float) value);
            } else if (value instanceof Long) {
                return 100 * (Long)value;
            } else if (value instanceof Integer) {
                return (long) (100 * (Integer) value);
            } else {
                MDApi.log("Unknown value type " + (value != null ? value.getClass() : null));
                return null;
            }
        } catch (Exception e) {
            MDApi.log("Failed to evaluate formula " + formula, e);
            return null;
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

    private String format(Long value) {
        if (value == null) {
            return "N/A";
        }

        long defaultValue = txn.getValue();

        if (value < 0) {
            // render values as positive
            value *= -1;
            defaultValue *= -1;
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
        Long value = calculateAmount();
        return value >= 0 ? format(value) : "";
    }

    public String formatDeposit() {
        Long value = calculateAmount();
        return value < 0 ? format(value) : "";
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
