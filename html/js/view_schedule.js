var days = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];
var hash;
var secret_code;

var last_day;
var current_start_date = "1970-01-01";

function getProperDate(dateString) {
    var userTimezoneOffset = new Date(Date.now()).getTimezoneOffset() * 60000;
    return new Date(new Date(dateString).getTime() + userTimezoneOffset);
}

function addTime(timeString, increment) {
    var time = timeString.split(':');
    var hour = parseInt(time[0], 10);
    var min = parseInt(time[1], 10);
    if (min + increment === 60) {
        hour++;
        min = 0;
    }
    else {
        min += increment;
    }
    if (String(min) === "0")
        min = "00";
    return hour + ":" + min;
}

function getActualDayCount(schedule) {
    let actual_schedule_days = 0;

    let end_date = getProperDate(schedule.end_date);
    for (let date = getProperDate(schedule.start_date); date.getTime() <= end_date.getTime(); date.setTime(date.getTime() + (24 * 60 * 60 * 1000))) {
        // console.log(date + ", Interpreted: " + days[date.getDay()]);
        if (date.getDay() !== 0 && date.getDay() !== 6) {
            // console.log("Schedulable! " + days[date.getDay()]);
            actual_schedule_days++;
        }
    }

    // console.log("Actual schedulable days: " + actual_schedule_days);

    return actual_schedule_days < 5 ? actual_schedule_days : 5;
}

function populateTimeSlots(timeSlots) {
    for (let i = 0; i < timeSlots.length; i++) {
        let slot = timeSlots[i];
        let meeting = slot.meeting;

        let control_button_selector = "#btn_ts" + slot.id;
        let create_meeting_button_selector = "#btn_cm_ts" + slot.id;
        let delete_meeting_button_selector = "#btn_dm_ts" + slot.id;

        $("#ts" + slot.id).data(slot);
        $(control_button_selector).data(slot);
        $(create_meeting_button_selector).data(slot);
        if (meeting) {
            meeting.ts_id = slot.id;
            $(delete_meeting_button_selector).data(meeting);
        }

        $(control_button_selector).click(function () {
            console.log("Clicked button to toggle time slot:");
            console.log($(this).data());
            toggleTimeSlot($(this).data());
        });
        $(create_meeting_button_selector).click(function () {
            createMeeting($(this).data());
        });
        if (meeting) {
            $(delete_meeting_button_selector).click(function () {
                deleteMeeting($(this).data());
            });
        }
    }
}

function populateDays() {
    $(".open-day").click(function() {
       openDay($(this).attr("data-date"));
    });
    $(".close-day").click(function() {
        closeDay($(this).attr("data-date"));
    });
}

function populateTimes() {
    $(".open-time").click(function() {
        openTime($(this).attr("data-time"));
    });
    $(".close-time").click(function() {
        closeTime($(this).attr("data-time"));
    });
}

function constructSwitchTab(i, date) {
    return '<li class="page-item">\n'
        + '<a class="page-link" onclick="setWeek(' + date + ');">' + i + '</a>\n'
        + '</li>'
}

function populateWeekSwitcher(num_weeks, start_date) {
    let selector = $(".pagination");
    selector.empty();
    selector.append(constructSwitchTab(1, "1970-01-01"));
    for(let i = 2, iDate = new Date(new Date(last_day).getTime() + (3 * 24 * 60 * 60 * 1000));
        i <= num_weeks;
        i++, iDate.setTime(iDate.getTime() + (3 * 24 * 60 * 60 * 1000))) {
        let year = iDate.getFullYear();
        let month = iDate.getMonth() + 1;
        let day = iDate.getDate();

        let full_date = "" + year + '-' + month + '-' + day;
        selector.append(constructSwitchTab(i, full_date));
    }
}

function constructDayHeaderDiv(date) {
    let year = date.getFullYear();
    let month = date.getMonth() + 1;
    let day = date.getDate();

    let full_date = "" + year + '-' + month + '-' + day;

    last_day = full_date;

    return '<div style="order:1;" class="Rtable-cell Rtable-cell-tall">'
        + '<h3>' + days[date.getDay()] + " " + month + "/" + day + '</h3>'
        + '<button type="button" class="small anchor-bottom edit-item open-day" data-date="'
        + full_date + '" style="margin-left: -10px;">Open</button>'
        + '<button type="button" class="small anchor-bottom anchor-right edit-item close-day" data-date="'
        + full_date + '">Close</button>'
        + '</div>\n';
}

function constructTimeSlotDiv(timeSlot, k) {
    // console.log(timeSlot);
    // console.log("k=" + k);

    let innerhtml;
    let class_ending = '"';

    if (timeSlot.meeting && timeSlot.is_open) {
        innerhtml = timeSlot.meeting.name;
    } else {
        innerhtml = '<button type="button" class="small vertical-center edit-item" id="btn_ts' + timeSlot.id
            + '" style="margin-left: -10px;">' + (timeSlot.is_open ? 'Close' : 'Open') + '</button>';
    }

    if (timeSlot.is_open && !timeSlot.meeting) {
        innerhtml += '<button type="button" class="small vertical-center anchor-right" id="btn_cm_ts' + timeSlot.id + '">+</button>';
    } else {
        class_ending = ' closed"';
        if (timeSlot.is_open && timeSlot.meeting) {
            innerhtml += '<button type="button" class="small vertical-center anchor-right" id="btn_dm_ts' + timeSlot.id + '">-</button>';
        }
    }

    return '<div style="order:' + (k + 2) + ';" class="Rtable-cell' + class_ending + ' id="ts' + timeSlot.id + '">'
        + innerhtml + '</div>\n';
}

function constructTimeRowDiv(time, k) {
    return '<div style="order:' + (k + 2) + ';" class="Rtable-cell">'
        + '<div>' + time + '</div>'
        + '<div class="vertical-center anchor-right">'
        + '<button type="button" class="small edit-item open-time" data-time="'
        + time + '" style="margin-left: -10px;">Open</button>'
        + '<button type="button" class="small edit-item close-time" data-time="'
        + time + '">Close</button></div>'
        + '</div>\n'
}

function generateTable(schedule, timeSlots) {
    $("#schedule_title").text(schedule.name);

    let table_innerHTML = '<div style="order:1;" class="Rtable-cell"></div>\n';

    let day_count = getActualDayCount(schedule);

    document.getElementById("schedule_table").className = "Rtable Rtable--" + (day_count + 1) + "cols";

    let time_slots_per_day = timeSlots.length / day_count;

    console.log("Day count: " + day_count);
    console.log("Time slots: " + timeSlots.length);
    console.log("Slots/day: " + time_slots_per_day);

    for (let i = 0, time = addTime(schedule.start_time, 0); i < time_slots_per_day;
         i++, time = addTime(time, schedule.meeting_duration)) {

        table_innerHTML += constructTimeRowDiv(time, i);
    }

    for (let j = 0, jDate = getProperDate(schedule.start_date); j < day_count; j++, jDate.setTime(jDate.getTime() + (24 * 60 * 60 * 1000))) {
        // console.log("j=" + j);
        // console.log("jDate=" + jDate);
        if (jDate.getDay() !== 0 && jDate.getDay() !== 6) {
            table_innerHTML += constructDayHeaderDiv(jDate);

            for (let k = 0; k < time_slots_per_day; k++) {
                table_innerHTML += constructTimeSlotDiv(timeSlots[j * time_slots_per_day + k], k);
            }
        }
    }

    // console.log(table_innerHTML);

    document.getElementById("schedule_table").innerHTML = table_innerHTML;

    $("#schedule_table").data(schedule);

    populateTimeSlots(timeSlots);
    populateDays();
    populateTimes();

    populateWeekSwitcher(schedule.num_weeks, schedule.start_date);

    $('#secret_code').on('input', function () {
        secret_code = $(this).val(); // Get the current value of the input field.
        if (secret_code)
            enableEditView();
        else
            disableEditView();
    });
}

function regenerateSchedule() {
    // var data = {};
    // data["id"] = hash;
    // data["week_start_date"] = "1970-01-01";
    //
    // var js = JSON.stringify(data);
    // console.log("JS:" + js);

    $.get(api_schedule_url, {id: hash, week_start_date: current_start_date}).done(function (data) {
        console.log(data);
        generateTable(data.schedule, data.time_slots);
        ensureCorrectEditView();
    });
}

function loadPage() {
    hash = window.location.hash.substring(1); // Puts hash in variable, and removes the # character
    secret_code = new URLSearchParams(window.location.search).get("secret_code");

    regenerateSchedule();
}

function setWeek(week_number) {
    if(page_number === 1)
        current_start_date = "1970-01-01";
    regenerateSchedule();
}