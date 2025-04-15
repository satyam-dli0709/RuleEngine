package com.example.RuleEngine.Controller;

//import com.example.RuleEngine.Entity.Person;
import com.example.RuleEngine.Entity.Rule;
import com.example.RuleEngine.Service.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class NumberController {

    @Autowired
     private  NumberService numberService;

//    @GetMapping("/checkEvenOdd")
//    public String checkNumber(@RequestParam int number) {
//           numberService.checkEvenOdd(number);
//           return "Check the console for the result.";
//      }

//      @PostMapping("/checkPersonDiabetes")
//    public ResponseEntity<String> checkPersonDiabetes(@RequestBody Person person) {
//        numberService.checkPersonDiabetes(person);
//        return ResponseEntity.ok("Check the console for the result.");
//    }


    @GetMapping("/readExcel")
    public Object readExcel() throws IOException {
    return numberService.readExcel();
   }

    @PostMapping("/writeExcel")
    public String writeExcel(@RequestBody List<Rule> data) throws Exception {
        numberService.writeExcel(data);
        return "Data written to Excel file successfully.";
    }

    @PostMapping("/checkRules")
    public ResponseEntity<?> checkRules(@RequestBody Map<String, Object> rules) throws Exception {
        return ResponseEntity.ok(numberService.checkRules(rules));
//        return ResponseEntity.ok("Check the console for the result.");
    }

}
