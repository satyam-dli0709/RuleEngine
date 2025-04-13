package com.example.RuleEngine.Config;

//import com.example.RuleEngine.Entity.Person;

import com.example.RuleEngine.Service.NumberService;
import com.example.RuleEngine.Util.DynamicClassGenerator;
import org.apache.poi.ss.formula.functions.T;
import org.evrete.KnowledgeService;
import org.evrete.api.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.example.RuleEngine.Service.NumberService.processResult;

@Configuration
public class RuleConfiguration {

    public static Class<?> personClazz;

//    public static List<Map<String, Object>> resultsList = new ArrayList<>();
    public static Map<String, Object> resultsMap = new HashMap<>();

//    @Autowired
//    public NumberService numberService;
//    @Bean
//    public StatefulSession statefulSession() throws IOException {
//
//        List<String[]> data = numberService.readExcel();
//
//
//        KnowledgeService knowledgeService = new KnowledgeService();
//        Knowledge knowledge = knowledgeService.newKnowledge();
//        knowledge.newRule("Check Person Diabetes")
//                .forEach("$p", Map.class)
//                .where("$p.gender.equals(\"Male\") && $p.type.equals(\"2 diabetes\") && $p.age < 30 && $p.duration >= 0 && $p.duration <= 6 && $p.hba1c >= 5 && $p.hba1c <= 5.4 && $p.fbs < 76")
//                .execute(ctx -> {
//                    Person person = ctx.get("$p");
//                    System.out.println("Person is not diabetic: "+person.toString());
//                });
//
//        knowledge.newRule("Check Person Diabetes Alternative")
//                .forEach("$p", Person.class)
//                .where("!($p.gender.equals(\"Male\") && $p.type.equals(\"2 diabetes\") && $p.age < 30 && $p.duration >= 0 && $p.duration <= 6 && $p.hba1c >= 5 && $p.hba1c <= 5.4 && $p.fbs < 76)")
//                .execute(ctx -> {
//                    Person person = ctx.get("$p");
//                    System.out.println("Person may be diabetic or does not meet the criteria: " + person.toString());
//                });
//
//
//
//        return knowledge.newStatefulSession();
//    }

//    @Bean
//    public StatefulSession statefulSession() throws IOException {
//
//        List<NumberService.PairConditionDecission> data = numberService.readExcel();
//        KnowledgeService knowledgeService = new KnowledgeService();
//        Knowledge knowledge = knowledgeService.newKnowledge();

//        // Define rules directly
//        knowledge.newRule("Check Person Diabetes")
//                .forEach("$p", Map.class)
//                .where("$p.gender.equals(\"Male\") && $p.type.equals(\"2 diabetes\") && $p.age < 30 && $p.duration >= 0 && $p.duration <= 6 && $p.hba1c >= 5 && $p.hba1c <= 5.4 && $p.fbs < 76")
//                .execute(ctx -> {
//                    Map<String, Object> person = ctx.get("$p");
//                    System.out.println("Person is not diabetic: " + person);
//                });
//
//        knowledge.newRule("Check Person Diabetes Alternative")
//                .forEach("$p", Map.class)
//                .where("!($p.gender.equals(\"Male\") && $p.type.equals(\"2 diabetes\") && $p.age < 30 && $p.duration >= 0 && $p.duration <= 6 && $p.hba1c >= 5 && $p.hba1c <= 5.4 && $p.fbs < 76)")
//                .execute(ctx -> {
//                    Map<String, Object> person = ctx.get("$p");
//                    System.out.println("Person may be diabetic or does not meet the criteria: " + person);
//                });

//        Knowledge knowledge = knowledgeService.newKnowledge();

// Loop through the data and dynamically create rules
//        for (NumberService.PairConditionDecission pair : data) {
//            Map<String, Object> condition = pair.getCondition();
//            Map<String, Object> decision = pair.getDecision();
//
//            // Build a rule name dynamically
//            String ruleName = "Rule for " + condition.toString();
//
//            // Create a new rule
//            knowledge.newRule(ruleName)
//                    .forEach("$p", Map.class)
//                    .where((ValuesPredicate) ctx -> {
//                        Map<String, Object> fact = ctx.get("$p");
//                        // Check if all conditions match
//                        return condition.entrySet().stream()
//                                .allMatch(entry -> fact.containsKey(entry.getKey()) &&
//                                        fact.get(entry.getKey()).equals(entry.getValue()));
//                    })
//                    .execute(ctx -> {
//                        // Execute the decision logic
//                        System.out.println("Rule matched: " + ruleName);
//                        System.out.println("Decision: " + decision);
//                    });
//        }
//
//        return knowledge.newStatefulSession();
//    }


//    @Autowired
//    private NumberService numberService;

    private Knowledge knowledge;

    @Bean
    public StatefulSession statefulSession() {
        return knowledge.newStatefulSession();
    }

    @PostConstruct
    public void initializeKnowledge() throws Exception {
        // Assuming you have the JSON data in a List<PairConditionDecision>
        List<NumberService.PairConditionDecission> data = NumberService.readExcel();
        KnowledgeService knowledgeService = new KnowledgeService();
        knowledge = knowledgeService.newKnowledge();
        Map<String, Object> decisions = new ConcurrentHashMap<>();
        Integer index = 0;
        for (NumberService.PairConditionDecission pair : data) {
            Map<String, Object> condition = pair.getCondition();
            Map<String, Object> decision = pair.getDecision();


            // Build a rule name dynamically
            String ruleName = "Rule for condition: " + condition.toString();

            // Build the condition string dynamically
            StringBuilder whereCondition = new StringBuilder("(");
            List<String> conditions = new ArrayList<>();

            // Iterate over the condition map to form dynamic conditions
            for (Map.Entry<String, Object> entry : condition.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                conditions.add("$p." + key.replaceAll("\\.", "_") + ".equals(\"" + value + "\")");
            }

            // Join all conditions with AND operator
            whereCondition.append(String.join(" && ", conditions));
            whereCondition.append(")");
            Class<?> personClass;
            Object person;
            if (personClazz != null && Object.class.isAssignableFrom(personClazz)) {
                personClass = personClazz;
                person = personClazz.getDeclaredConstructor().newInstance();
            } else {
                personClass = DynamicClassGenerator.createDynamicPersonClass("Person", condition);
                person = personClass.getDeclaredConstructor().newInstance();
                personClazz = personClass;
            }
            for (Map.Entry<String, Object> entry : condition.entrySet()) {
                try {
                    String fieldName = entry.getKey().replaceAll("\\.", "_");
                    Object fieldValue = entry.getValue();

                    Object[] updatedValues = DynamicClassGenerator.addFieldAndSetValue(personClazz, person, fieldName, fieldValue);
                    personClazz = (Class<?>) updatedValues[0];
                    person = updatedValues[1];
                } catch (Exception e) {
                    throw new RuntimeException("Failed to set field value: " + entry.getKey(), e);
                }
            }
            ++index;

            knowledge.newRule("Check Person Diabetes" + index.toString())
                    .forEach("$p", person.getClass())
                    .where(whereCondition.toString()).execute(ctx -> {
                        try {
                            Object matchedPerson = ctx.get("$p");
                            // Retrieve the callback by iterating over facts of type Consumer
                            Consumer<Map<String, Object>> callback = null;
                            resultsMap = new HashMap<>();
                            resultsMap.put("conditions", condition);
                            resultsMap.put("decision", decision);

                            // Call the callback
                            processResult(resultsMap, callback);

                            System.out.println("Rule matched: " + ruleName);
                            System.out.println("Decision applied: " + decision);
                        } catch (Exception e) {
                            System.err.println("Error executing rule: " + e.getMessage());
                        }
                    });

        }
//        return decisions;
    }

}
