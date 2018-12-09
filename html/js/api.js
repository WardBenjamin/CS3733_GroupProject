// TODO: Remove this; it is for debugging purposes only
// window.addEventListener("beforeunload", function () {
//     debugger;
// }, false);

// All access driven through base_url
var base_url = "https://jmb0yovy9c.execute-api.us-east-1.amazonaws.com/Alpha/";

var schedule_url = base_url + "schedule"; // GET, POST, PUT, DELETE

var html_admin = "https://cs3733-scheduler.s3-external-1.amazonaws.com/static/admin.html";
var html_schedule_view = "https://cs3733-scheduler.s3-external-1.amazonaws.com/static/schedule_view.html";
var html_scheduler = "https://cs3733-scheduler.s3-external-1.amazonaws.com/static/scheduler.html";
