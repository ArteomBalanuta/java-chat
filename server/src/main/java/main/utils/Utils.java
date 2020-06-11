package main.utils;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Utils {
    public static boolean isNotNullOrEmpty(String str) {
        return str != null && !str.isEmpty() && !str.isBlank();
    }
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty() || !str.isBlank();
    }


    public static void log(String str) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[ yyyy-MM-dd hh:mm:ss ]");
        String localDateTime = LocalDateTime.now().format(formatter);
        System.out.println(localDateTime + " - " + str + '\n');
    }

    public static String generateTrip() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}
