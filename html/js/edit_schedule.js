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

function closeTimeSlot(timeSlot) {
    console.log("Closing time slot: " + timeSlot.id
        + " in schedule: " + $("#schedule_table").data().id
        + " with secret code: " + $("#secret_code").val()
    );

    sendTimeSlotUpdate(
        {
            mode: "indiv", time_slot_id: timeSlot.id, open: 0, secret_code: $("#secret_code").val(),
            schedule_id: $("#schedule_table").data().id
        },
        function (data) {
            if (parseInt(data.httpCode) !== 200) {
                console.log("Failed to close time slot: " + timeSlot.id + "; " + data.error);
                alert(data.error);
            } else {
                console.log("Successfully closed time slot: " + timeSlot.id)
            }
        },
        function () {
            console.log("Failed to close time slot: " + timeSlot.id)
        }
    )
}

function openTimeSlot(timeSlot) {
    console.log("Opening time slot: " + timeSlot.id
        + " in schedule: " + $("#schedule_table").data().id
        + " with secret code: " + $("#secret_code").val()
    );

    sendTimeSlotUpdate(
        {
            mode: "indiv", time_slot_id: timeSlot.id, open: 1, secret_code: $("#secret_code").val(),
            schedule_id: $("#schedule_table").data().id
        },
        function (data) {
            if (parseInt(data.httpCode) !== 200) {
                console.log("Failed to open time slot: " + timeSlot.id + "; " + data.error);
                alert(data.error);
            } else {
                console.log("Successfully opened time slot: " + timeSlot.id)
            }
        },
        function () {
            console.log("Failed to open time slot: " + timeSlot.id)
        }
    )
}

function toggleTimeSlot(timeSlot) {
    console.log("Toggling time slot: " + timeSlot.id);
    if (timeSlot.is_open)
        closeTimeSlot(timeSlot);
    else
        openTimeSlot(timeSlot);
    setTimeout(regenerateSchedule, 2000);
}
