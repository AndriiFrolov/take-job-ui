$(document).ready(function () {
     updateLogs();
});

function updateLogs() {
    var logsTextArea = $('#logs');
	$.getJSON("/api/logs", function (data, status) {
		$.each(data, function (i, item) {
			if (item.fullyFilled) {
			   $('<span style="color: CornflowerBlue;">').text(item.date + ' ').appendTo(logsTextArea);
			   $('<span style="color: CornflowerBlue;">').text(item.time + ' ').appendTo(logsTextArea);
			   $('<span style="color: BlueViolet;">').text(item.logLevel + ' ').appendTo(logsTextArea);
			   $('<span style="color: Gold;">').text(item.source+  ' ').appendTo(logsTextArea);
			   $('<span style="color: Brown;">').text(item.thread + ' ').appendTo(logsTextArea);
			}
			$('<span style="color: DarkBlue;">').text(item.message).appendTo(logsTextArea);
			$('<br>').appendTo(logsTextArea);
		});
	});
	scrollSmoothToBottom('logs');
}

function scrollSmoothToBottom (id) {
   var div = document.getElementById(id);
     $('#' + id).animate({
        scrollTop: div.scrollHeight - div.clientHeight
     }, 500);
}