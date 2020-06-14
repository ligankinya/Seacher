$(document).ready(function () {

    $("#search-form").submit(function (event) {
        //stop submit the form, we will post it manually.
        event.preventDefault();
        fire_ajax_submit();
    });
});

function fire_ajax_submit() {

    var search = $("#keywords").val().split(" ");

    $("#btn-search").prop("disabled", true);

    $.ajax({
        type: "GET",
        url: "/search",
        data: { query: search },
        traditional : true,
        cache: false,
        timeout: 600000,
        success: function (data) {
            var col = ['Domain', 'Count'];
            // Create a table.
            var table = document.createElement("table");
            table.classList.add("table");
            table.classList.add("table-striped");

            // Create table header row using the extracted headers above.
            var tr = table.insertRow(-1);                   // table row.
            for (var i = 0; i < col.length; i++) {
                var th = document.createElement("th");      // table header.
                th.innerHTML = col[i];
                tr.appendChild(th);
            }

            // add json data to the table as rows.
            for (var key in data) {
                tr = table.insertRow(-1);
                var keyCell = tr.insertCell(-1);
                keyCell.innerHTML = key;
                var valCell = tr.insertCell(-1);
                valCell.innerHTML = data[key];
            }

            // Now, add the newly created table with json data, to a container.
            var divResTable = document.getElementById('resultTable');
            divResTable.innerHTML = "";
            divResTable.appendChild(table);

            console.log("SUCCESS : ", data);
            $("#btn-search").prop("disabled", false);

        },
        error: function (e) {

            var json = "<h4>Response</h4><pre>"
                + e.responseText + "</pre>";
            $('#feedback').html(json);

            console.log("ERROR : ", e);
            $("#btn-search").prop("disabled", false);
        }
    });
}