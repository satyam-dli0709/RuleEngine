package com.example.RuleEngine.Entity;

import javax.persistence.Entity;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Conditons {
//    private String gender;
//    private int age;
//    private String type;
//    private int duration;
//    private double hba1c;
//    private double fbs;
//
//    public double getHba1c() {
//        return hba1c;
//    }
//
//    public void setHba1c(double hba1c) {
//        this.hba1c = hba1c;
//    }
//
//    public String getGender() {
//        return gender;
//    }
//
//    public void setGender(String gender) {
//        this.gender = gender;
//    }
//
//    public int getAge() {
//        return age;
//    }
//
//    public void setAge(int age) {
//        this.age = age;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public int getDuration() {
//        return duration;
//    }
//
//    public void setDuration(int duration) {
//        this.duration = duration;
//    }
//
//    public double getFbs() {
//        return fbs;
//    }
//
//    public void setFbs(double fbs) {
//        this.fbs = fbs;
//    }
//
//    @Override
//    public String toString() {
//        return "Person{" +
//                "gender='" + gender + '\'' +
//                ", age=" + age +
//                ", type='" + type + '\'' +
//                ", duration=" + duration +
//                ", hba1c=" + hba1c +
//                ", fbs=" + fbs +
//                '}';
//    }

    Map<String , Object> condition = new HashMap<>();

    public Map<String, Object> getCondition() {
        return condition;
    }

    public void setCondition(Map<String, Object> condition) {
        this.condition = condition;
    }

}
