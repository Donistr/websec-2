package com.trading.backend.service.impl;

import com.trading.backend.model.Group;
import com.trading.backend.model.Lesson;
import com.trading.backend.model.LessonUnit;
import com.trading.backend.model.LessonType;
import com.trading.backend.model.ScheduleType;
import com.trading.backend.model.ScheduleWeek;
import com.trading.backend.model.Teacher;
import com.trading.backend.model.WeekDay;
import com.trading.backend.service.ScheduleService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private static final String SCHEDULE_URL_EXTERNAL = "https://ssau.ru/rasp?";

    private static final String SCHEDULE_INFO_CLASS = "info-block__title";

    private static final String SCHEDULE_NUMBER_CLASS = "week-nav-current";

    private static final String SCHEDULE_ITEM_CLASS = "schedule__item";

    private static final String SCHEDULE_TIME_CLASS = "schedule__time";

    private static final String SCHEDULE_TIME_ITEM_CLASS = "schedule__time-item";

    private static final String SCHEDULE_HEAD_CLASS = "schedule__head";

    private static final String SCHEDULE_HEAD_WEEKDAY_CLASS = "schedule__head-weekday";

    private static final String SCHEDULE_HEAD_DATE_CLASS = "schedule__head-date";

    private static final String SCHEDULE_LESSON_CLASS = "schedule__lesson-wrapper";

    private static final String SCHEDULE_LESSON_TYPE_CLASS = "schedule__lesson-type";

    private static final String SCHEDULE_LESSON_NAME_CLASS = "schedule__discipline";

    private static final String SCHEDULE_LESSON_LOCATION_CLASS = "schedule__place";

    private static final String SCHEDULE_LESSON_TEACHER_CLASS = "schedule__teacher";

    private static final String SCHEDULE_LESSON_ID_ATTRIBUTE = "href";

    private static final String SCHEDULE_LESSON_TEXT_CLASS = "caption-text";

    private static final String SCHEDULE_LESSON_GROUPS_CLASS = "schedule__groups";

    private static final String SCHEDULE_LESSON_GROUP_CLASS = "schedule__group";

    private static final int SCHEDULE_HEAD_START_INDEX = 1;

    private static final int SCHEDULE_HEAD_END_INDEX = 6;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public ScheduleWeek getGroupSchedule(long groupId) {
        return getSchedule(getGroupScheduleUrl(groupId), ScheduleType.GROUP);
    }

    @Override
    public ScheduleWeek getGroupSchedule(long groupId, int week) {
        return getSchedule(getGroupScheduleUrl(groupId, week), ScheduleType.GROUP);
    }

    @Override
    public ScheduleWeek getTeacherSchedule(long teacherId) {
        return getSchedule(getTeacherScheduleUrl(teacherId), ScheduleType.TEACHER);
    }

    @Override
    public ScheduleWeek getTeacherSchedule(long teacherId, int week) {
        return getSchedule(getTeacherScheduleUrl(teacherId, week), ScheduleType.TEACHER);
    }

    private static ScheduleWeek getSchedule(String url, ScheduleType scheduleType) {
        try {
            Document doc = Jsoup.connect(url).get();

            String info = doc.getElementsByClass(SCHEDULE_INFO_CLASS).first().text();

            Element numberElement = doc.getElementsByClass(SCHEDULE_NUMBER_CLASS).first();
            if (numberElement == null) {
                throw new RuntimeException("расписание не введено");
            }
            String numberText = numberElement.text();
            Integer number = Integer.valueOf(numberText.substring(0, numberText.indexOf(' ')));

            List<String> times = parseTimes(doc);

            List<WeekDay> weekDays = parseSchedule(doc);

            return new ScheduleWeek(scheduleType, info, number, times, weekDays);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getGroupScheduleUrl(long groupId) {
        return SCHEDULE_URL_EXTERNAL + "groupId=" + groupId;
    }

    private String getGroupScheduleUrl(long groupId, int week) {
        return addWeekSelectForScheduleUrl(getGroupScheduleUrl(groupId), week);
    }

    public String getTeacherScheduleUrl(long teacherId) {
        return SCHEDULE_URL_EXTERNAL + "staffId=" + teacherId;
    }

    private String getTeacherScheduleUrl(long teacherId, int week) {
        return addWeekSelectForScheduleUrl(getTeacherScheduleUrl(teacherId), week);
    }

    private static String addWeekSelectForScheduleUrl(String url, int week) {
        return url + "&selectedWeek=" + week;
    }

    private static List<WeekDay> parseSchedule(Document doc) {
        Elements scheduleItems = doc.getElementsByClass(SCHEDULE_ITEM_CLASS);

        List<WeekDay> weekDays = parseWeekDays(scheduleItems);

        for (int i = 0; i < scheduleItems.size() - SCHEDULE_HEAD_END_INDEX - 1; ++i) {
            int weekDayIndex = (i + SCHEDULE_HEAD_END_INDEX) % weekDays.size();
            Element scheduleItem = scheduleItems.get(i + SCHEDULE_HEAD_END_INDEX + 1);

            Elements lessons = scheduleItem.getElementsByClass(SCHEDULE_LESSON_CLASS);
            if (lessons.isEmpty()) {
                weekDays.get(weekDayIndex).getLessons().add(null);
                continue;
            }

            Lesson lesson = new Lesson();
            for (Element lessonElement : lessons) {
                lesson.getLessonUnits().add(parseLessonUnit(lessonElement));
            }

            weekDays.get(weekDayIndex).getLessons().add(lesson);
        }

        return weekDays;
    }

    private static List<WeekDay> parseWeekDays(Elements scheduleItems) {
        List<WeekDay> weekDays = new ArrayList<>();
        for (int i = SCHEDULE_HEAD_START_INDEX; i <= SCHEDULE_HEAD_END_INDEX; ++i) {
            Element scheduleItem = scheduleItems.get(i);

            if (!scheduleItem.hasClass(SCHEDULE_HEAD_CLASS)) {
                throw new RuntimeException("неожиданный формат расписания");
            }

            String name = scheduleItem.getElementsByClass(SCHEDULE_HEAD_WEEKDAY_CLASS).text();
            LocalDate date = LocalDate.parse(
                    scheduleItem.getElementsByClass(SCHEDULE_HEAD_DATE_CLASS).text(),
                    DATE_TIME_FORMATTER
            );

            weekDays.add(new WeekDay(name, date));
        }

        return weekDays;
    }

    private static List<String> parseTimes(Document doc) {
        Elements timesElements = doc.getElementsByClass(SCHEDULE_TIME_CLASS);

        List<String> times = new ArrayList<>();
        for (Element timeElement : timesElements) {
            Elements children = timeElement.getElementsByClass(SCHEDULE_TIME_ITEM_CLASS);

            if (children.size() == 2) {
                times.add(children.get(0).text() + "\n" + children.get(1).text());
                continue;
            }

            StringBuilder current = new StringBuilder();
            for (int i = 0; i < children.size() / 3; ++i) {
                current.append(children.get(i * 3).text()).append('\n');
                current.append(children.get(i * 3 + 1).text()).append('\n');
                current.append(children.get(i * 3 + 2).text()).append('\n');
            }
            times.add(current.toString());
        }

        return times;
    }

    private static LessonUnit parseLessonUnit(Element lessonElement) {
        LessonType type = LessonType.of(lessonElement.getElementsByClass(SCHEDULE_LESSON_TYPE_CLASS).text());
        String name = lessonElement.getElementsByClass(SCHEDULE_LESSON_NAME_CLASS).text();
        String location = lessonElement.getElementsByClass(SCHEDULE_LESSON_LOCATION_CLASS).text();

        Teacher teacher = null;
        Elements teacherElements = lessonElement.getElementsByClass(SCHEDULE_LESSON_TEACHER_CLASS);
        if (!teacherElements.isEmpty()) {
            Element teacherElement = teacherElements.first();
            Long teacherId = null;
            String href = teacherElement.getElementsByClass(SCHEDULE_LESSON_TEXT_CLASS)
                    .attr(SCHEDULE_LESSON_ID_ATTRIBUTE);
            if (!href.isEmpty()) {
                teacherId = Long.valueOf(href.substring(href.indexOf('=') + 1));
            }

            teacher = new Teacher(teacherId, teacherElement.text());
        }

        List<Group> groups = new ArrayList<>();
        Integer subGroup = null;

        Elements groupsElements = lessonElement.getElementsByClass(SCHEDULE_LESSON_GROUP_CLASS);
        if (groupsElements.isEmpty()) {
            Element groupsElement = lessonElement.getElementsByClass(SCHEDULE_LESSON_GROUPS_CLASS).first();
            String subGroupText = groupsElement.getElementsByClass(SCHEDULE_LESSON_TEXT_CLASS).first().text();
            if (!subGroupText.isEmpty()) {
                subGroup = Integer.valueOf(subGroupText.substring(subGroupText.indexOf(' ') + 1));
            }
        }
        for (Element group : groupsElements) {
            String href = group.getElementsByClass(SCHEDULE_LESSON_TEXT_CLASS)
                    .attr(SCHEDULE_LESSON_ID_ATTRIBUTE);

            groups.add(new Group(
                    Long.valueOf(href.substring(href.indexOf('=') + 1)),
                    group.text()
            ));
        }

        return new LessonUnit(
                type,
                name,
                location,
                teacher,
                groups,
                subGroup
        );
    }

}
