console.log("Loaded schedule creation plugin");

var js;

function processCreateResponse(result) {
    console.log("Create response result: " + result);
    js = JSON.parse(result);

    var httpResult = js["httpCode"];

    console.log("Processing response: " + httpResult);

    if (parseInt(httpResult) === 200) {
        // TODO: If result is good, show a green alert

        console.log(js);

        alert("Secret Code: " + js["secret_code"]);

        console.log("Current location: " + location.href);

        alert(js["id"]);

        location.href = html_schedule_view_url + "#" + js["id"];
    } else {
        // TODO: show red alert
        sessionStorage.schedule = null;
    }
}

function handleCreateClick(e) {
    console.log("Handling create click");

    var form = document.createForm;
    var name = form.name.value;
    var startDate = form.startDate.value;
    var endDate = form.endDate.value;
    var startTime = form.startTime.value;
    var endTime = form.endTime.value;
    var meetingDuration = form.meetingDuration.value;
    var defaultOpen = form.defaultState.value;

    console.log("Start date: " + startDate);

    if (!validateDate(startDate)) {
        // TODO: Alert that start date is invalid
        console.log("Couldn't validate start date");
        return false;
    }

    if (!validateDate(endDate)) {
        // TODO: Alert that end date is invalid
        console.log("Couldn't validate end date");
        return false;
    }

    if (!validateTime(startTime)) {
        // TODO: Alert that start time is invalid
        console.log("Couldn't validate start time");
        return false;
    }

    if (!validateTime(endTime)) {
        // TODO: Alert that end time is invalid
        console.log("Couldn't validate end time");
        return false;
    }

    var data = {};
    data["name"] = name;
    data["start_date"] = startDate;
    data["end_date"] = endDate;
    data["start_time"] = time12hTo24h(startTime);
    data["end_time"] = time12hTo24h(endTime);
    data["meeting_duration"] = meetingDuration;
    data["default_open"] = defaultOpen;

    var js = JSON.stringify(data);
    console.log("JS:" + js);

    var xhr = new XMLHttpRequest();
    xhr.open("PUT", api_schedule_url, true);

    // Send the collected data as JSON
    xhr.send(js);

    // This will process results and update HTML as appropriate.
    xhr.onloadend = function () {
        console.log(xhr);
        console.log(xhr.request);
        if (xhr.readyState === XMLHttpRequest.DONE) {
            console.log("XHR: " + xhr.responseText);
            processCreateResponse(xhr.responseText);
        } else {
            processCreateResponse("N/A");
        }
    };

    return false;
}
