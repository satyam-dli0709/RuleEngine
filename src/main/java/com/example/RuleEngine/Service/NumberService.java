package com.example.RuleEngine.Service;

import com.example.RuleEngine.Entity.Person;
import com.example.RuleEngine.Entity.Result;
import com.example.RuleEngine.Entity.Rule;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.evrete.api.StatefulSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NumberService {

    @Autowired
    private StatefulSession statefulSessionDiabetes;

//    public void checkEvenOdd(int number) {
//        statefulSessionEvenOdd.insert(number);
//        statefulSessionEvenOdd.fire();
//    }

    public void checkPersonDiabetes(Person person) {
        statefulSessionDiabetes.insert(person);
        statefulSessionDiabetes.fire();
    }


    public List<String[]> readExcel(String filePath) throws IOException {
              List<String[]> data = new ArrayList<>();
              FileInputStream fileInputStream = new FileInputStream(filePath);
              Workbook workbook = new XSSFWorkbook(fileInputStream);
              Sheet sheet = workbook.getSheetAt(0);

              for (Row row : sheet) {
              String[] rowData = new String[row.getPhysicalNumberOfCells()];
              for (Cell cell : row) {
              rowData[cell.getColumnIndex()] = cell.toString();
               }
                 data.add(rowData);
          }

                 workbook.close();
                 fileInputStream.close();
            return data;
    }

    public void writeExcel(String filePath, List<Rule> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i);
            Rule rule = data.get(i);
            Person person = rule.getCondition();
            Result result = rule.getResult();

            row.createCell(0).setCellValue("Condtion :"+"gender="+person.getGender());
            row.createCell(1).setCellValue("Condtion :"+"age="+person.getAge());
            row.createCell(2).setCellValue("Condtion :"+"type="+person.getType());
            row.createCell(3).setCellValue("Condtion :"+"duration="+person.getDuration());
            row.createCell(4).setCellValue("Condtion :"+"hba1c="+person.getHba1c());
            row.createCell(5).setCellValue("Condtion :"+"fbs="+person.getFbs());
            row.createCell(6).setCellValue("result:"+result.getDecision());

        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            workbook.write(fileOutputStream);
        }
        workbook.close();
    }

}

