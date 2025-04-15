package com.example.RuleEngine.Service;

import com.example.RuleEngine.Config.RuleConfiguration;
import com.example.RuleEngine.Entity.Conditons;
import com.example.RuleEngine.Entity.Result;
import com.example.RuleEngine.Entity.Rule;
import com.example.RuleEngine.Util.DynamicClassGenerator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.evrete.api.StatefulSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.example.RuleEngine.Config.RuleConfiguration.dynamicPersonClass;
//import static com.example.RuleEngine.Config.RuleConfiguration.personClazz;

@Service
public class NumberService {

//    @Autowired

//    public void checkEvenOdd(int number) {
//        statefulSessionEvenOdd.insert(number);
//        statefulSessionEvenOdd.fire();
//    }

//    public void checkConditonsDiabetes(Conditons person) {
//        statefulSessionDiabetes.insert(person);
//        statefulSessionDiabetes.fire();
//    }


    public static List<PairConditionDecission> readExcel() throws IOException {
        List<PairConditionDecission> data = new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream("D:\\RuleEngine\\RulesXlsxSheets\\output.xlsx");
        Workbook workbook = new XSSFWorkbook(fileInputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            String[] rowData = new String[row.getPhysicalNumberOfCells()];
            Map<String, Object> condition = new HashMap<>();
            Map<String, Object> decision = new HashMap<>();
            PairConditionDecission pairConditionDecission = new PairConditionDecission(condition, decision);
            for (Cell cell : row) {
                rowData[cell.getColumnIndex()] = cell.toString();
                if (cell.toString().contains("Condition:")) {
                    String[] conditionData = cell.toString().split(":");
                    String[] conditionKeyValue = conditionData[1].split("=");
                    condition.put(conditionKeyValue[0].replace("." , "_"), conditionKeyValue[1]);
                }
                if (cell.toString().contains("Result:")) {
                    String[] decisionData = cell.toString().split(":");
                    String[] decisionKeyValue = decisionData[1].split("=");
                    decision.put(decisionKeyValue[0].replace("." , "_"), decisionKeyValue[1]);
                }
            }
            data.add(pairConditionDecission);
        }
        // Close the workbook and file input stream
        new JSONArray(data);
        workbook.close();
        fileInputStream.close();
        return data;
    }

    public void writeExcel(List<Rule> data) throws Exception {
        String filePath = "D:\\RuleEngine\\RulesXlsxSheets\\output.xlsx";
        File file = new File(filePath);
        Workbook workbook;
        Sheet sheet;

        // Check if the file exists
        if (file.exists()) {
            // Read the existing workbook
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                workbook = new XSSFWorkbook(fileInputStream);
            }
            sheet = workbook.getSheetAt(0); // Get the first sheet
        } else {
            // Create a new workbook and sheet
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Sheet1");
        }

        // Get the starting row index
        int rowIndex = sheet.getLastRowNum() + 1;
        List<NumberService.PairConditionDecission> xlData = new ArrayList<>();


        // Append new data
        for (Rule rule : data) {
            Row row = sheet.createRow(rowIndex++);
            Map<String, Object> person = rule.getCondition();
            int cellIndex = 0;

            // Write condition data
            for (String key : person.keySet()) {
                Cell cell = row.createCell(cellIndex++);
                cell.setCellValue("Condition:" + key + "=" + person.get(key));
            }

            // Write result data
            Map<String, Object> result = rule.getResult();
            for (String key : result.keySet()) {
                Cell cell = row.createCell(cellIndex++);
                cell.setCellValue("Result:" + key + "=" + result.get(key));
            }
            xlData.add(new PairConditionDecission(rule.getCondition(), rule.getResult()));
        }

        // Write the updated workbook back to the file
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            workbook.write(fileOutputStream);
        }
        workbook.close();
        RuleConfiguration.initializeKnowledge();

    }

    public Map<String, Object> checkRules(Map<String, Object> rules) throws Exception {
        Class<?> personClass = dynamicPersonClass;
        Object person = personClass.getDeclaredConstructor().newInstance();

        for (Map.Entry<String, Object> entry : rules.entrySet()) {
            try {
                String fieldName = entry.getKey().replaceAll("\\.", "_");
                Object fieldValue = entry.getValue();

                Field field = personClass.getField(fieldName);
                field.set(person, fieldValue != null ? fieldValue.toString() : null);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set field value: " + entry.getKey(), e);
            }
        }

        // Create a shared collection to store the results
        List<Map<String, Object>> results = new ArrayList<>();

        // Define a callback to handle results
//        Consumer<Map<String, Object>> callback = result -> {
//            System.out.println("Callback invoked with result: " + result);
//            results.add(result); // Add the result to the shared list
//        };
        StatefulSession statefulSession = RuleConfiguration.statefulSession();
        // Insert the person object and callback into the session
        statefulSession.insert(person);
//        statefulSession.insert("callback" , callback);
//        statefulSession.set("callback", callback);
        // Fire the rules
        statefulSession.fire();

        return RuleConfiguration.resultsMap;
    }
    public static void processResult(Map<String, Object> result, Consumer<Map<String, Object>> callback) {
        // Process the result
        System.out.println("Processing result: " + result);

        // Invoke the callback
        callback.accept(result);
    }

    public static class PairConditionDecission {
        private Map<String, Object> condition;
        private Map<String, Object> decision;

        public PairConditionDecission(Map<String, Object> condition, Map<String, Object> decision) {
            this.condition = condition;
            this.decision = decision;
        }

        public Map<String, Object> getCondition() {
            return condition;
        }

        public void setCondition(Map<String, Object> condition) {
            this.condition = condition;
        }

        public Map<String, Object> getDecision() {
            return decision;
        }

        public void setDecision(Map<String, Object> decision) {
            this.decision = decision;
        }

    }

}

