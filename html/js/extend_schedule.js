function populateExtendForm(start_date, end_date) {
    $("#start_date").val(start_date);
    $("#end_date").val(end_date);
}

function processExtendResponse(data) {
    console.log("Extend response result: " + data);
    js = JSON.parse(data);

    var httpResult = js["httpCode"];

    if (parseInt(httpResult) !== 200) {
        alert("Failed to extend schedule: " + js["error"]);
    } else {
        alert("Extended schedule");

        location.reload(true);
    }
}

function handleExtendClick() {
    console.log("Handling extend click");

    let start_date = $("#start_date").val();
    let end_date = $("#end_date").val();
    let secret_code = $("#secret_code").val();
    let schedule_id = $("#schedule_table").data().id;
    console.log("Extending schedule: " + start_date + " to " + end_date);

    var data = {};
    data["id"] = schedule_id;
    data["secret_code"] = secret_code;
    data["start_date"] = start_date;
    data["end_date"] = end_date;

    var js = JSON.stringify(data);
    console.log("JS:" + js);

    var xhr = new XMLHttpRequest();
    xhr.open("POST", api_schedule_url, true);

    // Send the collected data as JSON
    xhr.send(js);

    // This will process results and update HTML as appropriate.
    xhr.onloadend = function () {
        console.log(xhr);
        console.log(xhr.request);
        if (xhr.readyState === XMLHttpRequest.DONE) {
            console.log("XHR: " + xhr.responseText);
            processExtendResponse(xhr.responseText);
        } else {
            processExtendResponse("N/A");
        }
    };

    return false;
}