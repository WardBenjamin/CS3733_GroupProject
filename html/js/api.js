// TODO: Remove this; it is for debugging purposes only
// window.addEventListener("beforeunload", function () {
//     debugger;
// }, false);

// All access driven through base_url
var base_url = "https://jmb0yovy9c.execute-api.us-east-1.amazonaws.com/Beta/";

var api_schedule_url = base_url + "schedule"; // GET, PUT, DELETE, TODO: POST
var api_timeslot_url = base_url + "timeslot"; // POST, TODO: GET
var api_meeting_url = base_url + "meeting"; // PUT, DELETE
var api_admin_url = base_url + "admin"; // GET, DELETE

var html_admin_url = "https://cs3733-scheduler.s3-external-1.amazonaws.com/static/admin.html";
var html_schedule_view_url = "https://cs3733-scheduler.s3-external-1.amazonaws.com/static/schedule_view.html";
var html_scheduler_url = "https://cs3733-scheduler.s3-external-1.amazonaws.com/static/scheduler.html";
