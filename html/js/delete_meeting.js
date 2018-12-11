function deleteMeeting(meeting) {
    console.log("Delete meeting id: " + meeting.id + "in time slot id: " + meeting.ts_id);

    let secret_code = prompt("Secret Code: ");

    var data = {};
    data["secret_code"] = secret_code;
    data["meeting_id"] = meeting.id;
    data["time_slot_id"] = meeting.ts_id;

    var js = JSON.stringify(data);
    console.log("JS:" + js);

    var xhr = new XMLHttpRequest();
    xhr.open("DELETE", api_meeting_url, true);

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
                alert("Deleted meeting");
            else
                alert("Failed to delete meeting: " + response.error);
            setTimeout(regenerateSchedule, 2000);
        } else {
            console.log("Failed to create schedule");
        }
    };

    return false;
}