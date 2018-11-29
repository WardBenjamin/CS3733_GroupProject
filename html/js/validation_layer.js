// Validates that the input string is formatted as "mm/dd/yyyy".
// No actual validation on if the date exists and is proper.
function validateDate(dateString) {
    // Check for the correct pattern
    return /^\d{1,2}\/\d{1,2}\/\d{4}$/.test(dateString);
}

function validateTime(timeString) {
    return /^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$/.test(timeString);
}