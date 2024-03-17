package org.example.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) throws Exception {
        JobMatcher jobMatcher = new JobMatcher();
        int a = jobMatcher.parsePrice("≈1,136 EUR");
        System.out.println(a);
    }
}
