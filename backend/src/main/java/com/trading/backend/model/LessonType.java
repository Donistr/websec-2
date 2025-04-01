package com.trading.backend.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum LessonType {

    LECTURE("Лекция"),
    PRACTICE("Практика"),
    LABORATORY("Лабораторная"),
    EXAM("Экзамен"),
    CREDIT("Зачёт"),
    CONSULTATION("Консультация"),
    OTHER("Другое");

    private static final Map<String, LessonType> valueToLessonType = new HashMap<>();

    static {
        for (LessonType lessonType : LessonType.values()) {
            valueToLessonType.put(lessonType.value, lessonType);
        }
    }

    public static LessonType of(String value) {
        if (!valueToLessonType.containsKey(value)) {
            throw new RuntimeException("неверное значение LessonType");
        }

        return valueToLessonType.get(value);
    }

    @JsonValue
    private final String value;

}
