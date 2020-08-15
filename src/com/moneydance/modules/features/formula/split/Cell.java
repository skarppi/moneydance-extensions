package com.moneydance.modules.features.formula.split;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class Cell {
    String cell;
    char col;
    int row;;
    FormulaSplitTxn txn;
    List<String> deps;

    // check if the same dependency has been tried two or more times in a row
    public boolean isLoop() {
        int lastIndex = deps.size() - 1;
        return lastIndex > 1 && deps.get(lastIndex).equals(deps.get(lastIndex - 1));
    }

    public String getScript() {
        return txn.getCellSource(col);
    }
}
