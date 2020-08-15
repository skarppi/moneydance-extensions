package com.moneydance.modules.features.formula.split;

import com.moneydance.modules.features.formula.reminder.FormulaTxn;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Builder
@Data
public class Cell {
    String cell;
    char col;
    int row;;
    FormulaTxn txn;
    List<String> deps;

    // check if the same dependency has been tried two or more times in a row
    public boolean isLoop() {
        int lastIndex = deps.size() - 1;
        return lastIndex > 1 && deps.get(lastIndex).equals(deps.get(lastIndex - 1));
    }

    public Object evalCell() {
        if (Arrays.asList('D', 'E').contains(col)) {
            Object value = txn.getRowValue();
            if (value instanceof Number) {
                if (col == 'D') {
                    return ((Number)value).doubleValue() >= 0 ? value : null;
                } else if (col == 'E') {
                    return ((Number)value).doubleValue() < 0 ? value : null;
                }
            } else {
                return value;
            }

        }

        return txn.getCellValue(col);
    }
}
