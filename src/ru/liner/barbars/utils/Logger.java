package ru.liner.barbars.utils;

import ru.liner.barbars.network.Connection;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static String composeLogout(String message){
        return String.format("%s [%s] | %s", new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis())), StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getSimpleName(), message);
    }
}
