 $(document).ready(function() {
    updateStatusTable();
    setInterval(updateStatusTable, 10000);
  });

function updateStatusTable() {
    $.ajax({
      type: "GET",
      url: "/api/status",
      dataType: "json",
      success: function(response) {
        // Clear previous data
        $("#jobTableBody").empty();
    var jobTableBody = $("#jobTableBody");
    response.foundJobs.forEach(function(job) {
      var newRow = $("<tr>");
      newRow.append("<td>" + job.title + "</td>");
      newRow.append("<td>" + job.dueDate + "</td>");
      newRow.append("<td>" + job.price + "</td>");
      newRow.append("<td>" + job.wordsCount + "</td>");
      newRow.append("<td>" + job.customer + "</td>");
      newRow.append("<td>" + job.appeared + "</td>");
      jobTableBody.append(newRow);
    });

     $("#statusTableBody").empty();

            // Update table with new data
            appendRow("Is Running", response.running);
            appendRow("Refreshed Page Times", response.refreshedPageTimes);
            appendRow("Started At", response.startedAt);
            appendRow("Stopped At", response.stoppedAt);
            appendRow("Jobs that were available", response.foundJobs.length);
            appendRow("Jobs that were taken (accepted)", response.takenJobs);
      },
      error: function(error) {
        console.log("Error:", error);
      }
    });
  }

  function appendRow(item, value) {
    var newRow = $("<tr>");
    newRow.append("<td>" + item + "</td>");
    newRow.append("<td>" + (value !== null ? value : "") + "</td>");
    $("#statusTableBody").append(newRow);
  }