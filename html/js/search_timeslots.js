console.log("Loaded timeslot search plugin");

function processSearchResponse(result, schedule_id) {
    // Can grab any DIV or SPAN HTML element and can then manipulate its
    // contents dynamically via javascript
    console.log("Search response result: " + result);

    var httpResult = result.httpCode;

    console.log("Processing response: " + httpResult);

    if (parseInt(httpResult) === 200) {
        let time_slots = result.time_slots;
        let innerhtml = '<div style="padding-top: 15px;">Open time slots that fit query:<br>';

        for(let i = 0; i < time_slots.length; i++) {
            let time_slot = time_slots[i];
            innerhtml += 'Date: ' + time_slot.date
                + ', Start: ' + time_slot.start_time
                + ', End: ' + time_slot.end_time
                + '<br>'
        }

        innerhtml += '</div>';

        $("#search_results").html(innerhtml);
    } else {
    }
}

function handleSearchClick(e) {
    console.log("Handling search click");

    let form = document.searchForm;
    let schedule_id = $("#search_schedule_id").val();
    let year = $("#search_year").val();
    let month = $("#search_month").val();
    let day = $("#search_day").val();
    let day_of_week = $("#search_day_of_week").val();
    let time_slot = $("#search_time").val();

    if (!year)
        year = -1;
    if (!month)
        month = -1;
    if (!day)
        day = -1;

    if (time_slot && !validateTime(time_slot)) {
        alert("Time slot invalid");
        return false;
    }

    data = {
        schedule_id: schedule_id,
        year: year,
        month: month,
        day_of_month: day,
        day_of_week: day_of_week,
        time_slot: time_slot
    };

    console.log("Timeslot search request:" + JSON.stringify(data));

    $.get(api_timeslot_url, data).done(function (data) {
        console.log(JSON.stringify(data));
        processSearchResponse(data);
    });

    return false;
}
