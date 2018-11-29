// TODO: Remove this; it is for debugging purposes only
window.addEventListener("beforeunload", function() { debugger; }, false)

// All access driven through base_url
var base_url = "https://2usfa7quq6.execute-api.us-east-2.amazonaws.com/Alpha/";

var schedule_url = base_url + "schedule"; // GET, POST, PUT, DELETE
