var days = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];
var hash;
var secret_code;

var last_day;
var current_start_date = "1970-01-01";

function getFullDate(date) {
    let year = date.getFullYear();
    let month = date.getMonth() + 1;
    let day = date.getDate();

    return "" + year + '-' + month + '-' + day;
}

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

function constructSwitchTab(i, date) {
    let clickFunction = "setWeek('" + date + "')";
    // console.log("Switch tab date: " + clickFunction);

    return '<li class="page-item">\n'
        + '<a class="page-link" onclick="' + clickFunction + ';">' + i + '</a>\n'
        + '</li>'
}

function populateWeekSwitcher(num_weeks, start_date) {
    let selector = $(".pagination");
    selector.empty();
    selector.append(constructSwitchTab(1, "1970-01-01"));
    // console.log("Creating week switcher: " + num_weeks);
    for(let i = 2, iDate = new Date(new Date(last_day).getTime() + (3 * 24 * 60 * 60 * 1000));
        i <= num_weeks;
        i++, iDate.setTime(iDate.getTime() + (7 * 24 * 60 * 60 * 1000))) {

        let year = iDate.getFullYear();
        let month = iDate.getMonth() + 1;
        let day = iDate.getDate();

        console.log(iDate);
        let full_date = "" + year + '-' + month + '-' + day;
        selector.append(constructSwitchTab(i, full_date));
    }
}

function constructDayHeaderDiv(date) {
    last_day = getFullDate(date);

    return '<div style="order:1;" class="Rtable-cell Rtable-cell-tall">'
        + '<h3>' + days[date.getDay()] + " " + (date.getMonth() + 1) + "/" + date.getDate() + '</h3>'
        + '<button type="button" class="small anchor-bottom edit-item open-day" data-date="'
        + last_day + '" style="margin-left: -10px;">Open</button>'
        + '<button type="button" class="small anchor-bottom anchor-right edit-item close-day" data-date="'
        + last_day + '">Close</button>'
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

function generateTable(schedule, timeSlots, num_weeks, time_slots_per_day) {
    $("#schedule_title").text(schedule.name);

    let table_innerHTML = '<div style="order:1;" class="Rtable-cell"></div>\n';

    let calculated_day_count = getActualDayCount(schedule);
    let calculated_time_slots_per_day = timeSlots.length / calculated_day_count;

    let day_count = timeSlots.length / time_slots_per_day;

    document.getElementById("schedule_table").className = "Rtable Rtable--" + (day_count + 1) + "cols";

    console.log("Time slots: " + timeSlots.length);
    console.log("Calculated day count: " + calculated_day_count);
    console.log("Actual day count: " + day_count);
    console.log("Calculated slots/day: " + calculated_time_slots_per_day);
    console.log("Actual slots/day: " + time_slots_per_day);

    for (let i = 0, time = addTime(schedule.start_time, 0); i < time_slots_per_day;
         i++, time = addTime(time, schedule.meeting_duration)) {

        table_innerHTML += constructTimeRowDiv(time, i);
    }

    let start_date = getProperDate(schedule.start_date);

    if(current_start_date !== "1970-01-01") {
        start_date = new Date(new Date(current_start_date).getTime() + (0 * 24 * 60 * 60 * 1000));
    }

    for (let j = 0, jDate = start_date; j < day_count; j++, jDate.setTime(jDate.getTime() + (24 * 60 * 60 * 1000))) {
        // console.log("j=" + j);
        // console.log("jDate=" + jDate);
        if (jDate.getDay() !== 0 && jDate.getDay() !== 6) {
            table_innerHTML += constructDayHeaderDiv(jDate);

            for (let k = 0; k < time_slots_per_day; k++) {
                // console.log("Generating time slot: "  + (j * time_slots_per_day + k) + " on: " + jDate);
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
    populateExtendForm(schedule.start_date, schedule.end_date);

    $('#secret_code').on('input', function () {
        secret_code = $(this).val(); // Get the current value of the input field.
        if (secret_code)
            enableEditView();
        else
            disableEditView();
    });
}

function regenerateSchedule(shouldPopulateSwitcher) {
    // var data = {};
    // data["id"] = hash;
    // data["week_start_date"] = "1970-01-01";
    //
    // var js = JSON.stringify(data);
    // console.log("JS:" + js);

    // console.log("Getting week with start date: " + current_start_date);
    $.get(api_schedule_url, {id: hash, week_start_date: current_start_date}).done(function (data) {
        console.log(data);
        generateTable(data.schedule, data.time_slots, data.num_weeks, data.time_slots_per_day);
        if(shouldPopulateSwitcher)
            populateWeekSwitcher(data.num_weeks, data.schedule.start_date);
        ensureCorrectEditView();
    });
}

function loadPage() {
    hash = window.location.hash.substring(1); // Puts hash in variable, and removes the # character
    secret_code = new URLSearchParams(window.location.search).get("secret_code");

    regenerateSchedule(true);
}

function setWeek(date) {
    // console.log("Setting week to date: " + date);
    if(date === 1)
        current_start_date = "1970-01-01";
    current_start_date = date;
    regenerateSchedule(false);
}