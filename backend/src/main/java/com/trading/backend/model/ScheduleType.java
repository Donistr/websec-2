package com.trading.backend.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ScheduleType {

    GROUP("group"),
    TEACHER("teacher");

    @JsonValue
    private final String value;

}
