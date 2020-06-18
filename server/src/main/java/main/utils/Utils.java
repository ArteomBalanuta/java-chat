package main.utils;


import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.Files.*;

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
