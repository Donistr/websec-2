package com.trading.backend.controller;

import com.trading.backend.model.ScheduleWeek;
import com.trading.backend.service.ScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
@AllArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/group")
    public ScheduleWeek
    getGroupSchedule(
            @RequestParam(name = "group_id") Long groupId,
            @RequestParam(name = "week", required = false) Integer week
    ) {
        if (week != null) {
            return scheduleService.getGroupSchedule(groupId, week);
        }

        return scheduleService.getGroupSchedule(groupId);
    }

    @GetMapping("/teacher")
    public ScheduleWeek
    getTeacherSchedule(
            @RequestParam(name = "teacher_id") Long teacherId,
            @RequestParam(name = "week", required = false) Integer week
    ) {
        if (week != null) {
            return scheduleService.getTeacherSchedule(teacherId, week);
        }

        return scheduleService.getTeacherSchedule(teacherId);
    }

}
