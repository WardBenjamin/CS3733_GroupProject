function createMeeting(timeSlot) {
    console.log("Create meeting in time slot id: " + timeSlot.id);

    let name = prompt("Name: ");

    var data = {};
    data["name"] = name;
    data["time_slot_id"] = timeSlot.id;

    var js = JSON.stringify(data);
    // console.log("JS:" + js);

    var xhr = new XMLHttpRequest();
    xhr.open("PUT", api_meeting_url, true);

    // Send the collected data as JSON
    xhr.send(js);

    // This will process results and update HTML as appropriate.
    xhr.onloadend = function () {
        console.log(xhr);
        console.log(xhr.request);
        if (xhr.readyState === XMLHttpRequest.DONE) {
            console.log("XHR: " + xhr.responseText);
            let response = JSON.parse(xhr.responseText);
            if(parseInt(response.httpCode) === 200)
                alert("Secret code: " + response.secret_code);
            else
                alert("Failed to create meeting: " + response.error);
            setTimeout(regenerateSchedule, 2000);
        } else {
            console.log("Failed to create schedule");
        }
    };

    return false;
}