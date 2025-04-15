package com.example.RuleEngine.Util;

import org.evrete.api.Knowledge;

import java.util.function.Consumer;

    public class ExecutorConsumer implements Consumer<String> {

        @Override
        public void accept(String t) {
            System.out.println("Consumed: " + t);
        }
    }