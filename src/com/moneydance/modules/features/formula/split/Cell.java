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
        return txn.getCellValue(col);
    }
}
