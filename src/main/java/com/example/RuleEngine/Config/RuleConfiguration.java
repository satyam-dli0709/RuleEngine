package com.example.RuleEngine.Config;

import com.example.RuleEngine.Entity.Person;
import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;
import org.evrete.api.RuleSession;
import org.evrete.api.StatefulSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleConfiguration {


    @Bean
    public StatefulSession statefulSession() {
        KnowledgeService knowledgeService = new KnowledgeService();
        Knowledge knowledge = knowledgeService.newKnowledge();
        knowledge.newRule("Check Person Diabetes")
                .forEach("$p", Person.class)
                .where("$p.gender.equals(\"Male\") && $p.type.equals(\"2 diabetes\") && $p.age < 30 && $p.duration >= 0 && $p.duration <= 6 && $p.hba1c >= 5 && $p.hba1c <= 5.4 && $p.fbs < 76")
                .execute(ctx -> {
                    Person person = ctx.get("$p");
                    System.out.println("Person is not diabetic: "+person.toString());
                });

        knowledge.newRule("Check Person Diabetes Alternative")
                .forEach("$p", Person.class)
                .where("!($p.gender.equals(\"Male\") && $p.type.equals(\"2 diabetes\") && $p.age < 30 && $p.duration >= 0 && $p.duration <= 6 && $p.hba1c >= 5 && $p.hba1c <= 5.4 && $p.fbs < 76)")
                .execute(ctx -> {
                    Person person = ctx.get("$p");
                    System.out.println("Person may be diabetic or does not meet the criteria: " + person.toString());
                });



        return knowledge.newStatefulSession();
    }
}
