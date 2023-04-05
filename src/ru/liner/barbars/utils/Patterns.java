package ru.liner.barbars.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class Patterns {

    public static String get(String patternString, String content, int group){
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(group) : null;
    }

    public static <T> T get(String patternString, String content, int group, T defValue){
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(content);
        String result =  matcher.find() ? matcher.group(group) : null;
        Object o = defValue;
        if(defValue instanceof String){
            o =  result == null ? defValue : (T) result;
        } else if(defValue instanceof Integer){
            o = result == null ? defValue : Integer.parseInt(result);
        } else if(defValue instanceof Float){
            o = result == null ? defValue : Float.parseFloat(result);
        } else if(defValue instanceof Long){
            o = result == null ? defValue : Long.parseLong(result);
        } else if(defValue instanceof Double){
            o = result == null ? defValue : Double.parseDouble(result);
        }
        return (T) o;
    }

    public static boolean exists(String patternString, String content){
        Pattern pattern = Pattern.compile(patternString);
        return pattern.matcher(content).find();
    }

    public static List<String> getAll(String patternString, String content, int group){
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(content);
        List<String> resultList = new ArrayList<>();
        while (matcher.find())
            resultList.add(matcher.group(group));
        return resultList;
    }

}
