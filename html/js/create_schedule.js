console.log("Loaded schedule creation plugin");

function processCreateResponse(result) {
    // Can grab any DIV or SPAN HTML element and can then manipulate its
    // contents dynamically via javascript
    console.log("Create response result: " + result);
    var js = JSON.parse(result);

    var httpResult = js["httpCode"];

    console.log("Processing response: " + httpResult);

    if (parseInt(httpResult) === 200) {
        // TODO: If result is good, show a green alert

        sessionStorage.schedule = result;

        console.log("Current location: " + location.href);

        // TODO: This doesn't work because you need a pre-signed key. We're just going to embed that here...
        // let url = location.href;
        // let to = url.lastIndexOf('/');
        // to = to === -1 ? url.length : to + 1;
        // let url_prefix = url.substring(0, to);
        //
        // let from = url.lastIndexOf("?");
        // let url_suffix = url.substring(from);
        //
        // console.log("Redirect url: " + url);
        //
        // location.href = url_prefix + 'schedule_view.html' + url_suffix;

        location.href = "https://cs3733-scheduler.s3-external-1.amazonaws.com/static/schedule_view.html?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20181130T042002Z&X-Amz-SignedHeaders=host&X-Amz-Expires=604796&X-Amz-Credential=AKIAIW6M4JLUHVYJR6UQ%2F20181130%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=8ad54b73a1c48054f6e8eaed6b99c19770eb1f66363d4027447ec18f57c2874d";
    } else {
        // TODO: show red alert
        sessionStorage.schedule = null;
    }
}

function handleCreateClick(e) {
    console.log("Handling create click");

    var form = document.createForm;
    var startDate = form.startDate.value;
    var endDate = form.endDate.value;
    var startTime = form.startTime.value;
    var endTime = form.endTime.value;
    var meetingDuration = form.meetingDuration.value;

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
    data["start_date"] = startDate;
    data["end_date"] = endDate;
    data["start_time"] = time12hTo24h(startTime);
    data["end_time"] = time12hTo24h(endTime);
    data["meeting_duration"] = meetingDuration;

    var js = JSON.stringify(data);
    console.log("JS:" + js);

    var xhr = new XMLHttpRequest();
    xhr.open("PUT", schedule_url, true);

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
