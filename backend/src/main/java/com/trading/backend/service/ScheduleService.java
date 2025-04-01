package com.trading.backend.service;

import com.trading.backend.model.ScheduleWeek;

public interface ScheduleService {

    ScheduleWeek getGroupSchedule(long groupId);

    ScheduleWeek getGroupSchedule(long groupId, int week);

    ScheduleWeek getTeacherSchedule(long teacherId);

    ScheduleWeek getTeacherSchedule(long teacherId, int week);

}
