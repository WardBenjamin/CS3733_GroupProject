var edit_enabled = false;

function disableEditView() {
    console.log("Edit view disabled");
    edit_enabled = false;
    ensureCorrectEditView();
}

function enableEditView() {
    if (!edit_enabled)
        console.log("Edit view enabled");
    edit_enabled = true;
    ensureCorrectEditView();
}

function ensureCorrectEditView() {
    if (edit_enabled)
        $(".edit-item").show();
    else
        $(".edit-item").hide();
}

/*
updatetimeslots
OPTION1
mode: indiv
time_slot_id: 1234
open: 1
OPTION2
mode: day
schedule_id: 1234
day: "2018-08-08"
open: 1
OPTION3
mode: slot
schedule_id: 1234
timeslot: "14:00"
open: 1
 */

function sendTimeSlotUpdate(data, success_callback, fail_callback) {
    console.log(data);
    $.ajax({
        url: api_timeslot_url,
        type: "POST",
        data: JSON.stringify(data),
        contentType: "application/json",
    }).done(success_callback).fail(fail_callback);
}

function openDay(date) {
    setDay(date, 1);
}

function closeDay(date) {
    setDay(date, 0);
}

function setDay(date, should_be_open) {
    console.log((should_be_open === 1 ? "Opening" : "Closing") + " time slots on date: " + date
        + " in schedule: " + $("#schedule_table").data().id
        + " with secret code: " + $("#secret_code").val()
    );

    sendTimeSlotUpdate(
        {
            mode: "day", day: date, open: should_be_open, secret_code: $("#secret_code").val(),
            schedule_id: $("#schedule_table").data().id
        },
        function (data) {
            if (parseInt(data.httpCode) !== 200) {
                console.log("Failed to " + (should_be_open ? "open" : "close") + "  time slots on day: " + date + "; " + data.error);
                alert(data.error);
            } else {
                console.log("Successfully " + (should_be_open ? "opened" : "closed") + " time slots on day: " + date);
                setTimeout(regenerateSchedule, 1000);
            }
        },
        function () {
            console.log("Failed to " + (should_be_open ? "open" : "close") + " time slots on day: " + date);
        }
    )
}

function openTime(time) {
    setTime(time, 1);
}

function closeTime(time) {
    setTime(time, 0);
}

function setTime(time, should_be_open) {
    console.log((should_be_open === 1 ? "Opening" : "Closing") + " time slots at time: " + time
        + " in schedule: " + $("#schedule_table").data().id
        + " with secret code: " + $("#secret_code").val()
    );

    sendTimeSlotUpdate(
        {
            mode: "slot", timeslot: time, open: should_be_open, secret_code: $("#secret_code").val(),
            schedule_id: $("#schedule_table").data().id
        },
        function (data) {
            if (parseInt(data.httpCode) !== 200) {
                console.log("Failed to " + (should_be_open ? "open" : "close") + "  time slots at time: " + time + "; " + data.error);
                alert(data.error);
            } else {
                console.log("Successfully " + (should_be_open ? "opened" : "closed") + " time slots at time: " + time);
                setTimeout(regenerateSchedule, 1000);
            }
        },
        function () {
            console.log("Failed to " + (should_be_open ? "open" : "close") + " time slots at time: " + time);
        }
    )
}

function toggleTimeSlot(timeSlot) {
    console.log("Toggling time slot: " + timeSlot.id);
    let should_be_open = timeSlot.is_open ? 0 : 1;

    console.log((should_be_open === 1 ? "Opening" : "Closing") + " time slot: " + timeSlot.id
        + " in schedule: " + $("#schedule_table").data().id
        + " with secret code: " + $("#secret_code").val()
    );

    sendTimeSlotUpdate(
        {
            mode: "indiv", time_slot_id: timeSlot.id, open: should_be_open, secret_code: $("#secret_code").val(),
            schedule_id: $("#schedule_table").data().id
        },
        function (data) {
            if (parseInt(data.httpCode) !== 200) {
                console.log("Failed to " + (should_be_open === 1 ? "open" : "close") + "  time slot: " + timeSlot.id + "; " + data.error);
                alert(data.error);
            } else {
                console.log("Successfully " + (should_be_open === 1? "opened" : "closed") + " time slot: " + timeSlot.id);
                setTimeout(regenerateSchedule, 1000);
            }
        },
        function () {
            console.log("Failed to " + (should_be_open === 1 ? "open" : "close") + " time slot: " + timeSlot.id)
        }
    )

}
