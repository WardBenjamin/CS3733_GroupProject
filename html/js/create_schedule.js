console.log("Loaded schedule creation plugin");

function processCreateResponse(result) {
    // Can grab any DIV or SPAN HTML element and can then manipulate its
    // contents dynamically via javascript
    console.log("Create response result: " + result);
    var js = JSON.parse(result);

    var httpResult = js["response"];
    var scheduleId = js["id"];

    // TODO: If result is good, show a green alert

    let url = location.href;
    var to = url.lastIndexOf('/');
    to = to === -1 ? url.length : to + 1;
    url = url.substring(0, to);

    setTimeout("location.href = url + 'schedule_view.html?id=' + scheduleId;", 5000);
}

function handleCreateClick(e) {
    console.log("Handling create click");

    var form = document.createForm;
    var startDate = form.startDate.value;
    var endDate = form.endDate.value;
    var startTime = form.startTime.value;
    var endTime = form.endTime.value;

    if (!validateDate(startDate)) {
        // Alert that start date is invalid
        return;
    }

    if (!validateDate(endDate)) {
        // Alert that end date is invalid
        return;
    }

    if (!validateTime(startTime)) {
        // Alert that start time is invalid
        return;
    }

    if (!validateTime(endTime)) {
        // Alert that end time is invalid
        return;
    }

    var data = {};
    data["startDate"] = startDate;
    data["endDate"] = endDate;
    data["startTime"] = startTime;
    data["endTime"] = endTime;

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
