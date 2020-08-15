package com.moneydance.modules.features.formula.split;

import com.infinitekind.moneydance.model.SplitTxn;
import com.moneydance.modules.features.formula.MDApi;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

@Data
public class FormulaSplitTxn {
    private int splitIndex;
    private SplitTxn txn;
    private FormulaResolver resolver;
    private String A;
    private String B;
    private String C;

    public FormulaSplitTxn(int splitIndex, SplitTxn txn, FormulaResolver resolver) {
        this.splitIndex = splitIndex;
        this.txn = txn;
        this.resolver = resolver;

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

    public long getAmount() {
        Long value = toCents(resolver.getValue(splitIndex));
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
            case 'V': return formula();
            default: return null;
        }
    }
}
