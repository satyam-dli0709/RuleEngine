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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.example.RuleEngine.Config.RuleConfiguration.personClazz;

@Service
public class NumberService {

    @Autowired
    private StatefulSession statefulSession;

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
                if (cell.toString().contains("Condtion:")) {
                    String[] conditionData = cell.toString().split(":");
                    String[] conditionKeyValue = conditionData[1].split("=");
                    condition.put(conditionKeyValue[0], conditionKeyValue[1]);
                }
                if (cell.toString().contains("Result:")) {
                    String[] decisionData = cell.toString().split(":");
                    String[] decisionKeyValue = decisionData[1].split("=");
                    decision.put(decisionKeyValue[0], decisionKeyValue[1]);
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

    public void writeExcel(List<Rule> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i);
            Rule rule = data.get(i);
            Map<String,Object> person = rule.getCondition();
//            Result result = rule.getResult();
            int j = 0;
            for(String key : person.keySet()){
                Cell cell = row.createCell(j++);
                cell.setCellValue("Condtion:" + key + "=" + person.get(key));
            }
            Map<String,Object> result = rule.getResult();
            for(String key : result.keySet()){
                Cell cell = row.createCell(j++);
                cell.setCellValue("Result:" + key + "=" + result.get(key));
            }


//            row.createCell(0).setCellValue("Condtion :"+"gender="+person.getGender());
//            row.createCell(1).setCellValue("Condtion :"+"age="+person.getAge());
//            row.createCell(2).setCellValue("Condtion :"+"type="+person.getType());
//            row.createCell(3).setCellValue("Condtion :"+"duration="+person.getDuration());
//            row.createCell(4).setCellValue("Condtion :"+"hba1c="+person.getHba1c());
//            row.createCell(5).setCellValue("Condtion :"+"fbs="+person.getFbs());
//            row.createCell(6).setCellValue("result:"+result.getDecision());

        }

        try (FileOutputStream fileOutputStream = new FileOutputStream("D:\\RuleEngine\\RulesXlsxSheets\\output.xlsx")) {
            workbook.write(fileOutputStream);
        }
        workbook.close();
    }

    public Map<String, Object> checkRules(Map<String, Object> rules) throws Exception {
        Class<?> personClass = personClazz;
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
        Consumer<Map<String, Object>> callback = result -> {
            System.out.println("Callback invoked with result: " + result);
            results.add(result); // Add the result to the shared list
        };

        // Insert the person object and callback into the session
        statefulSession.insert(person);
//        statefulSession.insert("callback" , callback);
        statefulSession.set("callback", callback);
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

