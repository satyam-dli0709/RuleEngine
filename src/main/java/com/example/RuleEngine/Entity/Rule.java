package com.example.RuleEngine.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    private Person condition;
    private Result result;

    public Person getCondition() {
        return condition;
    }

    public void setCondition(Person condition) {
        this.condition = condition;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "condition=" + condition +
                ", result=" + result +
                '}';
    }
}
