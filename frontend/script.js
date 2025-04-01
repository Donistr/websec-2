$(document).ready(function() {
    const apiBaseUrl = "http://localhost:8080/api/schedule";

    if ($("#searchInput").length) {
        $("#searchBtn").click(function() {
            let query = $("#searchInput").val();
            if (query) {
                $.ajax({
                    url: `${apiBaseUrl}/search`,
                    method: "POST",
                    contentType: "application/json",
                    data: JSON.stringify({ text: query }),
                    success: function(response) {
                        $("#searchResults").empty();
                        response.elements.forEach(function(element) {
                            let type = element.type
                            let link = 'schedule.html?';
                            if (type == 'group') {
                                link += `group_id=${element.id}` 
                            } else if (type == 'teacher') {
                                link += `teacher_id=${element.id}` 
                            }
                            $("#searchResults").append(
                                `<li class="list-group-item"><a href="${link}">${element.name}</a></li>`
                            );
                        });
                    },
                    error: function(xhr) {
                        let error = JSON.parse(xhr.responseText).error || "Ошибка поиска";
                        $("#searchResults").html(`<li class="list-group-item text-danger">${error}</li>`);
                    }
                });
            }
        });
    }

    function getUrlParam(param) {
        return new URLSearchParams(window.location.search).get(param);
    }

    if ($("#scheduleTitle").length) {
        let groupId = getUrlParam("group_id");
        let teacherId = getUrlParam("teacher_id");
        let week = getUrlParam("week");

        if (groupId) {
            loadSchedule("group", groupId, week);
        } else if (teacherId) {
            loadSchedule("teacher", teacherId, week);
        }
    }

    $("#prevWeek, #nextWeek").click(function() {
        let groupId = getUrlParam("group_id");
        let teacherId = getUrlParam("teacher_id");

        let newUrl = "schedule.html?";
        if (groupId) {
            newUrl += `group_id=${groupId}`;
        } else if (teacherId) {
            newUrl += `teacher_id=${teacherId}`;
        }
        newUrl += `&week=${$(this).data("week")}`;

        window.location.href = newUrl;
    });

    function loadSchedule(type, id, week = null) {
        let url = `${apiBaseUrl}/${type}?${type}_id=${id}`;
        if (week) {
            url += `&week=${week}`;
        }
    
        $.getJSON(url, function(data) {
            $("#scheduleTitle").text(
                type === "group" 
                    ? `Расписание группы: ${data.info}` 
                    : `Расписание преподавателя: ${data.info}`
            );
    
            const currentWeekNum = data.number;
            $("#currentWeek").text(`${currentWeekNum} неделя`);
            $("#prevWeek").text(`${currentWeekNum - 1} неделя`).data("week", currentWeekNum - 1);
            $("#nextWeek").text(`${currentWeekNum + 1} неделя`).data("week", currentWeekNum + 1);
    
            $("#scheduleContent").empty();
    
            const $table = $('<table>').addClass('table table-bordered table-responsive');
            const $thead = $('<thead>').appendTo($table);
            const $theadRow = $('<tr>').appendTo($thead);
            const $tbody = $('<tbody>').appendTo($table);
    
            $theadRow.append($('<th>').text('Время'));
    
            data.days.forEach(function(day) {
                $theadRow.append(
                    $('<th>').html(`${day.info}<br>${day.date.split('-').reverse().join('-')}`)
                );
            });
    
            data.times.forEach(function(time, timeIndex) {
                const $row = $('<tr>').appendTo($tbody);
                $row.append($('<td>').html(time.replace('\n', '<br>')));
    
                data.days.forEach(function(day) {
                    const $cell = $('<td>').appendTo($row);
                    const lesson = day.lessons[timeIndex];
    
                    if (lesson && lesson.lesson_units && lesson.lesson_units.length > 0) {
                        lesson.lesson_units.forEach(function(unit) {
                            let lessonTypeClass = '';
                            switch (unit.type.toLowerCase()) {
                                case 'лекция': lessonTypeClass = 'lecture'; break;
                                case 'практика': lessonTypeClass = 'practice'; break;
                                case 'лабораторная': lessonTypeClass = 'laboratory'; break;
                                case 'экзамен': lessonTypeClass = 'exam'; break;
                                case 'зачёт': lessonTypeClass = 'credit'; break;
                                case 'консультация': lessonTypeClass = 'consultation'; break;
                                case 'другое': lessonTypeClass = 'other'; break;
                            }
    
                            const $lessonDiv = $('<div>').appendTo($cell);
    
                            $lessonDiv.append(
                                $('<strong>').addClass(lessonTypeClass).text(unit.type)
                            ).append('<br>');
    
                            $lessonDiv.append(unit.name).append('<br>');
    
                            $lessonDiv.append(unit.location).append('<br>');
    
                            if (unit.teacher && unit.teacher.name) {
                                if (unit.teacher.id) {
                                    $lessonDiv.append(
                                        $('<a>')
                                            .attr('href', `schedule.html?teacher_id=${unit.teacher.id}`)
                                            .text(unit.teacher.name)
                                    ).append('<br>');
                                } else {
                                    $lessonDiv.append(unit.teacher.name).append('<br>');
                                }
                            }
    
                            if (unit.sub_group) {
                                $lessonDiv.append(`Подгруппа ${unit.sub_group}`).append('<br>');
                            }
    
                            if (unit.groups && unit.groups.length > 0) {
                                $lessonDiv.append('Группы: ');
                                unit.groups.forEach(function(group, index) {
                                    $lessonDiv.append(
                                        $('<a>')
                                            .attr('href', `schedule.html?group_id=${group.id}`)
                                            .text(group.name)
                                    );
                                    if (index < unit.groups.length - 1) {
                                        $lessonDiv.append(', ');
                                    }
                                });
                                $lessonDiv.append('<br>');
                            }
    
                            $lessonDiv.append('<br>');
                        });
    
                        $cell.find('br:last').remove();
                    } else {
                        $cell.text('-');
                    }
                });
            });
    
            $("#scheduleContent").append($table);
        }).fail(function(xhr) {
            $("#scheduleContent").html(`<p class="text-danger">${JSON.parse(xhr.responseText).error || "Ошибка загрузки"}</p>`);
        });
    }

});