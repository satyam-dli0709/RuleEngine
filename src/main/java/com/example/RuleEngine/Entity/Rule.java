package com.example.RuleEngine.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    private Map<String,Object> condition;
    private Map<String,Object> result;

    public Map<String, Object> getCondition() {
        return condition;
    }

    public void setCondition(Map<String,Object> condition) {
        this.condition = condition;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String,Object> result) {
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
