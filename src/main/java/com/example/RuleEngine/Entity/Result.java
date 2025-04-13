package com.example.RuleEngine.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private String decision;
    private Integer loadingPercentage;

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    @Override
    public String toString() {
        return "Result{" +
                "decision='" + decision + '\'' +
                '}';
    }
}
