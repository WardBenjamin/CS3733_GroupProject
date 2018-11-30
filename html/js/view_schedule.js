var days = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];

function getProperDate(dateString) {
    var userTimezoneOffset = new Date(Date.now()).getTimezoneOffset() * 60000;
    return new Date(new Date(dateString).getTime() + userTimezoneOffset);
}

function addTime(timeString, increment) {
    var time = timeString.split(':');
    var hour = parseInt(time[0], 10);
    var min = parseInt(time[1], 10);
    if(min + increment === 60) {
        hour++;
        min = 0;
    }
    else {
        min += increment;
    }
    if(String(min) === "0")
        min = "00";
    return hour + ":" + min;
}

function loadPage() {
    if (sessionStorage.getItem("schedule") == null) {
        console.log("No schedule in session storage");
        return;
    }
    let schedule = JSON.parse(sessionStorage.schedule);
    console.log(schedule);

    /*{
        "organizer": "22542b39-97be-4dba-a291-23fa173a5fc5",
        "httpCode": 200,
        "start_date": "2018-10-29",
        "end_date": "2018-12-29",
        "start_time": "08:00",
        "end_time": "20:00",
        "meeting_duration": 15,
        "days": 61,
        "timeSlotsPerDay": 48
    }*/
    alert("Secret code: " + schedule.organizer);

    let table_innerHTML = '<div style="order:1;" class="Rtable-cell"></div>\n';

    for (let i = 0, time = schedule.start_time; i < schedule.timeSlotsPerDay;
         i++, time = addTime(time, schedule.meeting_duration)) {

        table_innerHTML += '<div style="order:' + (i + 2) + ';" class="Rtable-cell">'
            + time
            + '</div>\n';
        console.log(table_innerHTML);
    }

    let actual_schedule_days = 0;

    let end_date = getProperDate(schedule.end_date);
    for(let date = getProperDate(schedule.start_date); date.getTime() < end_date.getTime(); date.setTime(date.getTime() + (24 * 60 * 60 * 1000))) {
        console.log(date + ", Interpreted: " + days[date.getDay()]);
        if(date.getDay() !== 0 && date.getDay() !== 6) {
            console.log("Schedulable! " + days[date.getDay()]);
            actual_schedule_days++;
        }
    }

    console.log("Actual schedulable days: " + actual_schedule_days);

    let day_count = actual_schedule_days < 5 ? actual_schedule_days : 5;

    document.getElementById("schedule_table").className = "Rtable Rtable--" + (day_count + 1) + "cols";

    for (let j = 0, jDate = getProperDate(schedule.start_date); j < day_count; j++, jDate.setTime(jDate.getTime() + (24 * 60 * 60 * 1000))) {
        if (jDate.getDay() !== 0 && jDate.getDay() !== 6) {
            table_innerHTML += '<div style="order:1;" class="Rtable-cell"><h3>'
                + days[jDate.getDay()]
                + " "
                + (jDate.getMonth() + 1)
                + "/"
                + jDate.getDate()
                +  '</h3></div>\n';

            for (let k = 0; k < schedule.timeSlotsPerDay; k++) {
                table_innerHTML += '<div style="order:' + (k + 2) + ';" class="Rtable-cell"></div>\n';
            }
        }
    }

    console.log(table_innerHTML);

    document.getElementById("schedule_table").innerHTML = table_innerHTML;
}
