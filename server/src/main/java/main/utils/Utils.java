package main.utils;


import java.util.UUID;

public class Utils {
    public static boolean isNotNullOrEmpty(String str) {
        return str != null && !str.isEmpty() && !str.isBlank();
    }
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty() || !str.isBlank();
    }

    public static String generateTrip() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}
