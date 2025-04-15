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
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.example.RuleEngine.Service.NumberService.processResult;

@Configuration
public class RuleConfiguration {

//    public static Class<?> personClazz;

//    public static List<Map<String, Object>> resultsList = new ArrayList<>();
    public static Map<String, Object> resultsMap = new HashMap<>();

    private static KnowledgeService knowledgeService = new KnowledgeService();
    private static Knowledge knowledge = knowledgeService.newKnowledge();

    private static Integer index = 0;

//    @Bean
    public static StatefulSession statefulSession() {
        return knowledge.newStatefulSession();
    }

//    @PostConstruct
//    public static void initializeKnowledge() throws Exception {
//        // Assuming you have the JSON data in a List<PairConditionDecision>
//        List<NumberService.PairConditionDecission> data = NumberService.readExcel();
//        System.out.println(knowledge);
//        Set<String> allAttributes = data.stream()
//                .flatMap(pair -> pair.getCondition().keySet().stream())
//                .collect(Collectors.toSet());
//        Class<?> personClazz = DynamicClassGenerator.createDynamicPersonClassUsingSetOfKeys("Person", allAttributes);
//        Map<String, Object> decisions = new ConcurrentHashMap<>();
//        for (NumberService.PairConditionDecission pair : data) {
//            Map<String, Object> condition = pair.getCondition();
//            Map<String, Object> decision = pair.getDecision();
//
//
//            // Build a rule name dynamically
//            String ruleName = "Rule for condition: " + condition.toString();
//
//            // Build the condition string dynamically
//            StringBuilder whereCondition = new StringBuilder("(");
//            List<String> conditions = new ArrayList<>();
//
//            // Iterate over the condition map to form dynamic conditions
//            for (Map.Entry<String, Object> entry : condition.entrySet()) {
//                String key = entry.getKey();
//                Object value = entry.getValue();
////                conditions.add("$p." + key.replaceAll("\\.", "_") + ".equals(\"" + value + "\")");
//                conditions.add("($p." + key.replaceAll("\\.", "_") + " == null || $p." + key.replaceAll("\\.", "_") + ".equals(\"" + value + "\"))");
//            }
//
//            // Join all conditions with AND operator
//            whereCondition.append(String.join(" && ", conditions));
//            whereCondition.append(")");
////            Class<?> personClass;
////            Object person;
////            if (personClazz != null && Object.class.isAssignableFrom(personClazz)) {
////                personClass = personClazz;
////                person = personClazz.getDeclaredConstructor().newInstance();
////            } else {
////                personClass = DynamicClassGenerator.createDynamicPersonClass("Person", condition);
////                person = personClass.getDeclaredConstructor().newInstance();
////                personClazz = personClass;
////            }
////            for (Map.Entry<String, Object> entry : condition.entrySet()) {
////                try {
////                    String fieldName = entry.getKey().replaceAll("\\.", "_");
////                    Object fieldValue = entry.getValue();
////
////                    Object[] updatedValues = DynamicClassGenerator.addFieldAndSetValue(personClazz, person, fieldName, fieldValue);
////                    personClazz = (Class<?>) updatedValues[0];
////                    person = updatedValues[1];
////                } catch (Exception e) {
////                    throw new RuntimeException("Failed to set field value: " + entry.getKey(), e);
////                }
////            }
//            // Create a new instance of the dynamic class
//            ++index;
//            Object person = personClazz.getDeclaredConstructor().newInstance();
//            knowledge.newRule("Check Person Diabetes" + index)
//                    .forEach("$p", person.getClass())
//                    .where(whereCondition.toString()).execute(ctx -> {
//                        try {
//                            Object matchedPerson = ctx.get("$p");
//                            resultsMap = new HashMap<>();
//                            resultsMap.put("conditions", condition);
//                            resultsMap.put("decision", decision);
//                            System.out.println("Rule matched: " + ruleName);
//                            System.out.println("Decision applied: " + decision);
//                        } catch (Exception e) {
//                            System.err.println("Error executing rule: " + e.getMessage());
//                        }
//                    });
//
//            System.out.println("Class name : " + personClazz.getName());
//        }
////        return decisions;
//    }

    public static Class<?> dynamicPersonClass;

    @PostConstruct
    public static void initializeKnowledge() throws Exception {
        List<NumberService.PairConditionDecission> data = NumberService.readExcel();
        System.out.println(knowledge);

        // Collect all attributes from the data
        Set<String> allAttributes = data.stream()
                .flatMap(pair -> pair.getCondition().keySet().stream())
                .collect(Collectors.toSet());

        // Check if the class needs to be recreated
        if (dynamicPersonClass == null || !classContainsAllAttributes(dynamicPersonClass, allAttributes)) {
            // Release the previous class reference
            dynamicPersonClass = null;
            knowledge = null;
//            statefulSession = null;
            // Suggest garbage collection
            System.gc();
            // Create a new dynamic class
            dynamicPersonClass = DynamicClassGenerator.createDynamicPersonClassUsingSetOfKeys("Person", allAttributes);
            // Recreate the knowledge object and release the old reference
            System.gc();
            knowledge = knowledgeService.newKnowledge();
//            statefulSession = knowledge.newStatefulSession();
        }

        for (NumberService.PairConditionDecission pair : data) {
            Map<String, Object> condition = pair.getCondition();
            Map<String, Object> decision = pair.getDecision();

            String ruleName = "Rule for condition: " + condition.toString();

            StringBuilder whereCondition = new StringBuilder("(");
            List<String> conditions = new ArrayList<>();

            Object person = dynamicPersonClass.getDeclaredConstructor().newInstance();
            // Loop through all fields in the dynamic class
            Arrays.stream(dynamicPersonClass.getDeclaredFields())
                    .map(Field::getName)
                    .forEach(fieldName -> {
                        String conditionKey = fieldName.replace(".", "_");
                        if (condition.containsKey(conditionKey)) {
                            Object value = condition.get(conditionKey);
                            if (!ObjectUtils.isEmpty(value)) {
                                // Add condition for non-null value
                                conditions.add("($p." + fieldName + " != null && $p." + fieldName + ".equals(\"" + value + "\"))");
                            } else {
                                // Add condition for null value
                                conditions.add("($p." + fieldName + " == null)");
                            }
                            try {
                                // Set the field value dynamically
                                Field field = person.getClass().getDeclaredField(fieldName);
                                field.setAccessible(true);
                                field.set(person, value);
                            } catch (Exception e) {
                                throw new RuntimeException("Error setting field value for: " + fieldName, e);
                            }
                        } else {
                            // Add condition for fields not in the condition map
                            conditions.add("($p." + fieldName + " == null)");
                        }
                    });

            whereCondition.append(String.join(" && ", conditions));
            whereCondition.append(")");

            ++index;
            knowledge.newRule("Check Person Diabetes" + index)
                    .forEach("$p", person.getClass())
                    .where(whereCondition.toString())
                    .execute(ctx -> {
                        try {
                            Object matchedPerson = ctx.get("$p");
                            resultsMap = new HashMap<>();
                            resultsMap.put("conditions", condition);
                            resultsMap.put("decision", decision);
                            System.out.println("Rule matched: " + ruleName);
                            System.out.println("Decision applied: " + decision);
                        } catch (Exception e) {
                            System.err.println("Error executing rule: " + e.getMessage());
                        }
                    });

            System.out.println("Class name : " + dynamicPersonClass.getName());
        }
    }

    private static boolean classContainsAllAttributes(Class<?> clazz, Set<String> attributes) {
        Set<String> classFields = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        return classFields.containsAll(attributes.stream().map(attr -> attr.replace(".", "_")).collect(Collectors.toSet()));
    }

}
