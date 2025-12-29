package ru.yandex.practicum;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TestLogUtils {
    public static PrintWriter createTestLogWriter() {
        return new PrintWriter(new StringWriter());
    }
}
