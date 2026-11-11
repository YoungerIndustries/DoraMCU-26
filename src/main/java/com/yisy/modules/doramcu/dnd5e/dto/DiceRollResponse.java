package com.yisy.modules.doramcu.dnd5e.dto;

import java.util.List;

/**
 * Response from a dice roll.
 */
public class DiceRollResponse {

    private String formula;
    private List<Integer> individualRolls;
    private int total;
    private String description;

    public DiceRollResponse() {}

    public DiceRollResponse(String formula, List<Integer> individualRolls, int total, String description) {
        this.formula = formula;
        this.individualRolls = individualRolls;
        this.total = total;
        this.description = description;
    }

    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }

    public List<Integer> getIndividualRolls() { return individualRolls; }
    public void setIndividualRolls(List<Integer> individualRolls) { this.individualRolls = individualRolls; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
