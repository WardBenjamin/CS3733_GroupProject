// Validates that the input string is formatted as "mm/dd/yyyy".
// No actual validation on if the date exists and is proper.
function validateDate(dateString) {
    // Check for the correct pattern
    return /^\d{4}-\d{1,2}-\d{1,2}$/.test(dateString);
}

function validateTime(timeString) {
    return /^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$/.test(timeString);
}

function dateToRequiredFormat(dateString) {
    let date = new Date(dateString);
    return date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate();
}

function time12hTo24h(timeString) {
    var PM = timeString.match('PM');

    time = timeString.split(':');
    var min;
    var hour;

    if (PM) {
        hour = parseInt(time[0], 10);
        if (hour !== 12)
            hour = 12 + parseInt(time[0], 10);
        min = time[1].replace('PM', '').trim();
    } else {
        hour = parseInt(time[0], 10);
        if (hour === 12)
            hour = 0;
        min = time[1].replace('AM', '').trim();
    }

    console.log(hour + ':' + min);

    return hour + ":" + min;
}
