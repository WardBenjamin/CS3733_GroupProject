console.log("Loaded admin plugin");

function processDeleteOldResponse(result) {
    // Can grab any DIV or SPAN HTML element and can then manipulate its
    // contents dynamically via javascript
    console.log("Delete response result: " + JSON.stringify(result));

    var httpResult = result.httpCode;

    console.log("Processing response: " + httpResult);

    if (parseInt(httpResult) === 200) {
        let innerhtml = '<div style="padding-top: 15px;">Deleted ';

        innerhtml += result.num_deleted + ' old schedules<br></div>';

        $("#delete_old_results").html(innerhtml);
    } else {
        console.log("Failed to delete old schedules: " + result.error);
    }
}

function handleDeleteOldClick(e) {
    console.log("Handling delete old click");

    let days_old = $("#days_old").val();

    console.log("Deleting schedules more than: " + days_old + " days old");

    let data = {days: days_old};

    $.ajax({
        url: api_admin_url,
        type: "DELETE",
        data: JSON.stringify(data),
        contentType: "application/json",
    }).done(processDeleteOldResponse).fail(function() {
        console.log("Failed to delete old schedules")
    });

    return false;
}

function processGetRecentRequest(result, hours) {
    // Can grab any DIV or SPAN HTML element and can then manipulate its
    // contents dynamically via javascript
    console.log("Get recent response result: " + JSON.stringify(result));

    var httpResult = result.httpCode;

    console.log("Processing response: " + httpResult);

    if (parseInt(httpResult) === 200) {
        let schedules = result.schedules;
        let innerhtml = '<div style="padding-top: 15px;">Schedules created in the last ' + hours + ' hours:<br>';

        for(let i = 0; i < schedules.length; i++) {
            let schedule = schedules[i];
            innerhtml += schedule.id
                + ': ' + schedule.name
                + ', ' + schedule.start_date
                + ' to ' + schedule.end_date
                + '<br>'
        }

        innerhtml += '</div>';

        $("#get_recent_results").html(innerhtml);
    } else {
        console.log("Failed to delete old schedules: " + result.error);
    }
}

function handleGetRecentClick(e) {
    console.log("Handling get recent click");

    let hours_since = $("#hours_since").val();

    console.log("Getting schedules created more recently than: " + hours_since+ " hours");

    let data = {hours: hours_since};

    $.get(api_admin_url, data).done(function (data) {
        console.log(JSON.stringify(data));
        processGetRecentRequest(data, hours_since);
    });

    return false;
}
