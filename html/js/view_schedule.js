function loadPage() {
    if (sessionStorage.getItem("schedule") == null) {
        console.log("No schedule in session storage");
        return;
    }
    let schedule = JSON.parse(sessionStorage.schedule);
    console.log(schedule);
}
