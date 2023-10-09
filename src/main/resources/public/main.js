 $(document).ready(function() {
    loadData();
    $("#saveButton").click(function() {
      saveStorage()
    });
    $("#startButton").click(function() {
      // Retrieve data from input fields
      $('#startButton').prop('disabled', true);
      $('#stopButton').prop('disabled', false);
      const data = getEnteredData();
      saveStorage();
      // Send POST request
      $.ajax({
        type: "POST",
        url: "http://localhost:8080/api/start",
        data: JSON.stringify(data),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function(response) {
          // Handle success response
          console.log("POST request successful:", response);
        },
        error: function(error) {
          // Handle error response
          console.log("Error:", error);
        }
      });
    });
    $("#stopButton").click(function() {
          // Retrieve data from input fields
          $('#startButton').prop('disabled', false);
          $('#stopButton').prop('disabled', true);

          // Send POST request
          $.ajax({
            type: "POST",
            url: "http://localhost:8080/api/stop",
//            data: JSON.stringify(data),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function(response) {
              // Handle success response
              console.log("POST request successful:", response);
            },
            error: function(error) {
              // Handle error response
              console.log("Error:", error);
            }
          });
        });
    updateStatusTable();
    setInterval(updateStatusTable, 10000);
  });

function getEnteredData() {
   var minTotalPrice = $("#minTotalPrice").val();
      var deadlineHoursFromNow = $("#deadlineHoursFromNow").val();
      var maxAttempts = $("#maxAttempts").val();
      var username = $("#username").val();
      var password = $("#password").val();
      var emailToSend = $("#emailToSend").val();
      var secondsWait = $("#secondsWait").val();


      // Create data object
      var data = {
        "minTotalPrice": minTotalPrice,
        "deadlineHoursFromNow": deadlineHoursFromNow,
        "maxAttempts": maxAttempts,
        "username": username,
        "password": password,
        "emailToSend": emailToSend,
        "secondsWait": secondsWait
      };
      return data;
      }

function loadData() {
  const prevData = localStorage.getItem('prevData');
  if (prevData === null) {
     return;
  }
  const localStorageData = JSON.parse(prevData);

  for (var key in localStorageData) {
      if (localStorageData.hasOwnProperty(key)) {
        $("#" + key).val(localStorageData[key]);
      }
    }


}


function saveStorage() {
   localStorage.setItem('prevData', JSON.stringify(getEnteredData()));
}

function updateStatusTable() {
    $.ajax({
      type: "GET",
      url: "/api/status",
      dataType: "json",
      success: function(response) {
        // Clear previous data
        $("#statusTableBody").empty();

        $('#startButton').prop('disabled', response.running);
        $('#stopButton').prop('disabled', !response.running);

        // Update table with new data
        appendRow("Is Running", response.running);
        appendLink("Live Video", "http://localhost:7900/?autoconnect=1&resize=scale&password=secret");
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

   function appendLink(item, value) {
      var newRow = $("<tr>");
      newRow.append("<td>" + item + "</td>");
      newRow.append($("<td>").append($('<a>').attr('href', value).attr('target', '_blank').text('Link')));
      $("#statusTableBody").append(newRow);
    }

  function appendRow(item, value) {
    var newRow = $("<tr>");
    newRow.append("<td>" + item + "</td>");
    newRow.append("<td>" + (value !== null ? value : "") + "</td>");
    $("#statusTableBody").append(newRow);
  }