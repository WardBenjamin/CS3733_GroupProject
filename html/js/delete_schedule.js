console.log("Loaded schedule deletion plugin");


function processDeleteResponse(result, schedule_id) {
    // Can grab any DIV or SPAN HTML element and can then manipulate its
    // contents dynamically via javascript
    console.log("Delete response result: " + result);
    let js = JSON.parse(result);

    var httpResult = js["httpCode"];

    console.log("Processing response: " + httpResult);

    if (parseInt(httpResult) === 200) {
        // TODO: If result is good, show a green alert

        console.log(js);

        $("#delete_results").text("Deleted schedule: " + schedule_id);
    }
}

function handleDeleteClick(e) {
    console.log("Handling delete click");

    var form = document.deleteForm;
    var schedule_id = form.schedule_id.value;
    var secret_code = form.secret_code.value;

    var data = {};
    data["id"] = schedule_id;
    data["secret_code"] = secret_code;

    let js = JSON.stringify(data);
    console.log("JS:" + js);

    var xhr = new XMLHttpRequest();
    xhr.open("DELETE", api_schedule_url, true);

    // Send the collected data as JSON
    xhr.send(js);

    // This will process results and update HTML as appropriate.
    xhr.onloadend = function () {
        console.log(xhr);
        console.log(xhr.request);
        if (xhr.readyState === XMLHttpRequest.DONE) {
            console.log("XHR: " + xhr.responseText);
            processDeleteResponse(xhr.responseText, schedule_id);
        } else {
            processDeleteResponse("N/A");
        }
    };

    return false;
}
