# Traceable

# locally

docker-compose up --build -d
docker-compose down

# Build new docker image and push
do mvn clean install!!!
delete old image
docker build -t andrii111333/takejob:latest .
docker push andrii111333/takejob:latest

# On VM

sudo docker-compose down
sudo docker image pull andrii111333/takejob:amd64
sudo docker-compose up -d





# Frontend 
explanation how built


#  Spring Boot & UI
You don't need specific code to make it work. Just put ```index.html``` under ```public/``` directory

Note that we've created sub-folders to manage our html and js files (pages, elements)

###  How to run & debug
![](images/Run.png)

just use IDEA buttons. Preferred way is to run in 'Debug' mode, in this case you can put breakpoins in your Java code, e.g. to see what comes from UI to your controllers

Note: Build button (F9) is very useful when you make changes to ```js files```. In most cases you don't need to stop and start your app to apply your changes.

###  Debug JS
![](images/Debug1.png)

You can open Sources, find your .js file and put breakpoint. Also you can evaluate variables and even functions using Console:

![](images/java/Debug2.png)
#  HTML and CSS guidelines

I assume that you already familiar with HTML and CSS basics. In this example we use existing CSS library that makes elements more handsome

https://www.w3schools.com/w3css/default.asp

As stated in the documentation, you just need to add 'dependency' on this library in your html file:

``` <link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">```

Now you can use w3 classes to beatify elements:
```$xslt
<div class="w3-container w3-center">
    <h2 class="w3-blue">Traceability matrix</h2>
</div>
```

Refer to documentation by link above to get more details about this library

#  JQuery guidelines
jQuery is a fast, small, and feature-rich JavaScript library. It makes things like HTML document traversal and manipulation, event handling, animation, and Ajax much simpler with an easy-to-use API that works across a multitude of browsers. With a combination of versatility and extensibility, jQuery has changed the way that millions of people write JavaScript.

https://jquery.com/

To make your js scripts use jquery you should add to HTML page:
``` 
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.1/jquery.min.js"></script>
```

#  How to show data on page (GET request)

Let's see how our Stories page is implemented. Firstly, we need to define our HTML

```$xslt
 <div class="w3-container">
            <table id="stories" class="w3-table-all w3-card-4 w3-hoverable">
                <tr>
                    <th>Choose</th>
                    <th>JIRA ID</th>
                    <th>Summary</th>
                </tr>
            </table>
        </div>
```

This is a table with just header (```th``` tag). We will add content to this table dynamically based on API response

Firstly we need to tell our HMTL which JS file we are going to use:
```$xslt
 <script src="story.js"></script>
```

Let's look to ```story.js```

We want to show our data immediately once page is opened, so all related code we should write inside method
```$xslt
$(document).ready(function() {
   //code to be executed once page opened
});
```

So how to get data? using getJSON method
```$xslt
$.getJSON("/api/story", function(data, status) {
   //here we can work with response
});
```

where status - status code and data - response body

/api/story returns array of user stories. Let's iterate through data:
```$xslt
$.each(data, function(i, item) {
            $tdForCheckbox = $('<td>').append(
                $('<input>', {
                    class: "w3-check",
                    type: "checkbox"
                }));
            var $tr = $('<tr class="story-row story-row-' + item.jiraId + '">').append(
                $tdForCheckbox,
                $('<td id="jiraId">').text(item.jiraId),
                $('<td id="summary">').text(item.summary)
            ).appendTo('#stories');
        });
```
This is a loop where on each iteration we have index (i) and item (user story). For each story we create row ($tr)

each $object has method ```append``` that allows to add child elements. So our first column will have a checkbox insde td:
```$xslt
 $tdForCheckbox = $('<td>').append(
                $('<input>', {
                    class: "w3-check",
                    type: "checkbox"
                }));
```

Next 2 columns represents JIRA ID and summary, we need just to fill td with item data:
```$xslt
$('<td id="jiraId">').text(item.jiraId),
$('<td id="summary">').text(item.summary)
```

Finally, we should add our tr (row) to table. We can do it using method appendTo
```$tr.appendTo('#stories');```
where ```#stories``` means CSS selector (element with id='stories')

Alternatively you could declare $table var and call append method on it:
```$xslt
   $myTable = $('#stories');
   $myTable.append($tr);
```
Just a reminder how final result looks like (table only):
![](images/StoriesPage.png)

#  How to send data  (POST request)
Let's continue with Story page and implement possibility to add stories. Let's add HTML markup:

```$xslt
       <div class="w3-container">
            <label class="w3-text-teal"><b>JIRA ID</b></label>
            <input id="jiraId-input" class="w3-input w3-border w3-light-grey" type="text">

            <label class="w3-text-teal"><b>Summary</b></label>
            <input id="summary-input" class="w3-input w3-border w3-light-grey" type="text">

            <br>
        </div>
        <div class="w3-container">
            <button class="w3-btn w3-white w3-border w3-round-large" id="add-story">Add story</button>
        </div>
        <div id='validation-success' class="w3-panel w3-green" style="display: none">
            <button class="w3-button">Success!</button>
        </div>
```

story.js:
Let's add event handler on click event for Add button:
```$xslt
$("#add-story").click(function() {
```

firstly, we should get data from inputs. Let's do it:
```$xslt
function validateForm() {
    var body = {
        jiraId: $("#jiraId-input").val(),
        summary: $("#summary-input").val()
    };
    if (body.jiraId == "" || body.summary == "") {
        showError('Please fill all fields', 'All fields must pe populated');
        return false;
    }
    return body;
}
```

Let's ignore now method ```showError```. We will come back to it later. Now just enough that we formed and returned our request body

Let's use it:
```$xslt
var body = validateForm();

        if (body) {
            return $.ajax({
                type: 'POST',
                url: '/api/story',
                data: JSON.stringify(body),
                success: function(data) {
                    localStorage.setItem('status', 'success');
                    location.reload();

                },
                error: function(jqXHR, textStatus, errorThrown) {
                    showError(jqXHR.responseJSON.message, jqXHR.responseText);
                },
                contentType: "application/json",
                dataType: 'json'
            });
        }
```

Note that inside
```success: function(data) {```
and
```error: function(jqXHR, textStatus, errorThrown) {```
we can implement different behavior based on response status

#  How to send data  (DELETE request)
Let's continue with story.js
When we need to delete stories, the 1st step is to understand which stories are marked to be deleted

```$xslt
 $('#delete-story').on('click', function() {
        var stories = getSelectedStories();

...
function getSelectedStories() {
    var selected = [];
    $("tr.story-row").each(function() {
        $this = $(this);
        var isSelected = $this.find(".w3-check").is(':checked');
        if (isSelected) {
            var body = {
                jiraId: $this.find("#jiraId").text(),
                summary: $this.find("#summary").text()
             };
            selected.push(body);
        }
    });
    return selected;
}
```

So here we iterate through each ```tr``` with class equalt to ```story-row``` (Remember that when we dynamically created this row, we've added classes there)
```$xslt
$tr = $('<tr class="story-row story-row-' + item.jiraId + '">')
```
so as a result it looks like this:
![](images/HTML.png)

So, we iterate through our tr's and check if it's checkbox selected:
```$xslt
$this = $(this); //'this' is tr object
var isSelected = $this.find(".w3-check").is(':checked'); // element with class .w3-check
```
if tr is checked, we form body and add it to our array:
```$xslt
             var body = {
                jiraId: $this.find("#jiraId").text(),
                summary: $this.find("#summary").text()
             };
            selected.push(body);
```

Once we have our array, just send DELETE request whenever delete button clicked:
```$xslt
$('#delete-story').on('click', function() {
        var stories = getSelectedStories();

        $.ajax({
                type: 'DELETE',
                url: '/api/story',
                data: JSON.stringify(stories),
                success: function(data) {
                    $("#validation-success").fadeIn(3000).fadeOut(3000);
                    $.each(stories, function(i, row) {
                        var rowToDelete = $(".story-row-" + row.jiraId);
                        rowToDelete.remove();
                    });
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    showError(jqXHR.responseJSON.message, jqXHR.responseText);
                },
                contentType: "application/json",
                dataType: 'json'
            });
    });
```

Note that we also want to delete our rows from table immediately. Of course if you delete data on backend and refresh page - records will disappear, but this is not really nice

Let's just use 'remove' method to delete element from HTML
```$xslt
                    $.each(stories, function(i, row) {
                        var rowToDelete = $(".story-row-" + row.jiraId);
                        rowToDelete.remove();
                    });
```
Again, remember we've added class with 'ID' to each row? ```
$tr = $('<tr class="story-row story-row-' + item.jiraId + '">')
```
![](images/HTML.png)

Alternatively we could use ID's, this is just an example

# Validation

What if we received bad response? Or what if we want to see some message once we have success? Let's do it
```$xslt
                error: function(jqXHR, textStatus, errorThrown) {
                    showError(jqXHR.responseJSON.message, jqXHR.responseText);
                },
```

```$xslt
function showError(shortMessage, detailedMessage) {
            $("#error-message").text(shortMessage)
			$("<p>").text(detailedMessage).appendTo($('#show-error-details'))
			$("#validation-error").show();
}
```

So, we have this error message in our DOM structure, but by default it is invisible. we change text and make it vi HTML for our validation block:

```$xslt
<div id="validation-block">
    <div class="w3-container w3-margin">
        <div id='validation-error' class="w3-panel w3-red w3-display-container" style="display: none">
        <span onclick="this.parentElement.style.display='none'"
              class="w3-button w3-large w3-display-topright">&times;</span>
            <h5>Validation error!</h5>
            <p id="error-message">.</p>
            <button id='show-error-details-btn' onclick="expandCollapse('show-error-details', 'Error detailed message')"
                    class="w3-btn w3-block w3-red w3-left-align">Show Error detailed message
            </button>
            <div id="show-error-details" class="w3-container w3-hide"></div>
        </div>
    </div>
</div>
```

# Re-usable components
Did you notice that we have same header on each page? I don't think this is good idea to copy paste it. let's declate it in separate file

![](images/Header.png)

header.html:
```$xslt
<div class="w3-bar w3-light-blue">
    <a href="/" class="w3-bar-item w3-button">Main</a>
    <a href="/pages/story.html" class="w3-bar-item w3-button">Stories</a>
    <a href="/pages/scenario.html" class="w3-bar-item w3-button">Scenarios</a>
    <a href="/pages/feature.html" class="w3-bar-item w3-button">Feature files</a>
</div>
```

header.js
```$xslt
 window.onload = function() {
    fetch("/elements/header.html")
        .then(response => {
             return response.text()
     })
     .then(data => {
         document.querySelector("#header").innerHTML = data;
     });
  }
```

Now on each page where we need header we should do 2 things:
1. Add reference to header.js
   ``` <script src="elements/header.js"></script>```
2. Add header placeholder ```<div id="header"></div>```

Script will find element with id=header and fullfil it with html from file header.html

Same way we can put some re-usable JS/JQuery methods inside one file and re-use them (in out example common methods placed in the ```util_methods.js```)


# More examples
Explore more exampels in another html and js files. For instance, you can find there how to load data from backend to dropdown, how to dynamically create checkboxes with data etc
