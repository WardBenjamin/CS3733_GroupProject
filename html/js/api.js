// TODO: Remove this; it is for debugging purposes only
window.addEventListener("beforeunload", function () {
    debugger;
}, false);

// All access driven through base_url
var base_url = "https://jmb0yovy9c.execute-api.us-east-1.amazonaws.com/Alpha/";

var schedule_url = base_url + "schedule"; // GET, POST, PUT, DELETE
