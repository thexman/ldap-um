var groupUtils = null;
var selectedGroup = null;

function editGroup(uid) {
	var group = $("#groups-grid").jqGrid('getRowData', uid);
	selectedGroup = group;
	console.log(group);
	groupUtils.setGroup(group);
	$("#group-dialog").dialog("open"); 
}

function split( val ) {
	return val.split( /;\s*/ );
}

function extractLast( term ) {
	return split( term ).pop();
}

$(document).ready(function () {
	var ajaxUtils = new AjaxUtils($('#loading-div'));
	var menuUtils = new MenuUtils();
	
	var dialog = $("#group-dialog");
	groupUtils = new GroupUtils(dialog);
	
	
	var w = $(window).width() - 50;
	var h = $(window).height() - 150;
	var rowNum = 20;
	if (h > 1024) {
		rowNum = 100;
	} else if (h > 700) {
		rowNum = 50;
	}
	$("#groups-grid").jqGrid({
		url: 'ws/groups/rows',
		datatype: "json",
		colModel: [
			{ label: ' ', name: 'add', width: 16, sortable: false, search: false,
				formatter:function(cellvalue, options, rowObject) {
  					return "<span class='ui-icon ui-icon-pencil group-edit-button' data-group-id=\"" + rowObject.name.replace(/"/g, '\\\"') + "\"></span>";
				}	
			},				           
			{ label: 'Group name', name: 'name', width: 150, key:true },
			{ label: 'Users', name: 'users', width: 250, 
				formatter:function(cellValue, options, rowObject) {
					var s = "";
					$.each(rowObject.users, function(ind, val) {
						s += groupUtils.formatUser(val, true);
					});	
					return s;
				}
			}					
		],
		viewrecords: true,
		shrinkToFit: true,
		//autowidth: true,
		width: w, 
		height: h,
		rowNum: rowNum,
		rowList: [10, 20, 50, 100, 500],
		loadonce: true,
		pager: "#groups-grid-pager"
	}).on("click", ".group-edit-button", function() {
		var gid = $(this).attr("data-group-id");
		editGroup(gid);
	});
	
	dialog.dialog({
		autoOpen: false,
		height: 500,
		width: 350,
		modal: true,
		close: function() { groupUtils.reset(); },
		buttons: { 
			"Save": function() { groupUtils.submit(); },
			"Cancel": function() { groupUtils.closeDialog(); }
		}
	});
	 
	dialog.find("form").on("submit", function( event ) {				
		event.preventDefault();
		var isNew = (selectedGroup == null);
		var group = groupUtils.validate(isNew);
		if (group != null) {
			var form = $(this);					
			var url;
			if (isNew) {
				url = "ws/groups/create";	
			} else {
				url = "ws/groups/update";
			}
			 
			// Send the data using post
			var posting = $.post( url, { "group": JSON.stringify(group) } );
		 
			// Put the results in a div
			posting.done(function( data ) {					
				console.log(data);
				$("#groups-grid").jqGrid('setGridParam',{datatype:'json'}).trigger('reloadGrid');
			});
		}
	});
	
	$("#create-group").click( function() {
		selectedGroup = null;
		groupUtils.reset();
		$("#group-dialog").dialog("open"); 
	} );
	
	$("#users")
		// don't navigate away from the field on tab when selecting an item
		.bind( "keydown", function( event ) {
			if (event.keyCode === $.ui.keyCode.TAB && $(this).autocomplete("instance").menu.active) {
				event.preventDefault();
			}
		})
		.autocomplete({
			source: function(request, response) {
				$.getJSON("ws/users/searchUsers", { search : extractLast(request.term) }, function(data) {
					var j = [];
					$.each(data.users, function(ind, val) {
						j.push(groupUtils.formatUser(val, false));
					});	
					response(j);
				});
			},
			search: function() {
				// custom minLength
				var term = extractLast(this.value);
				if (term.length < 2) {
					return false;
				}
			},
			focus: function() {
				// prevent value inserted on focus
				return false;
			},
			select: function(event, ui) {
				var terms = split( this.value );
				// remove the current input
				terms.pop();
				// add the selected item
				terms.push(ui.item.value);
				// add placeholder to get the comma-and-space at the end
				terms.push("");
				this.value = terms.join("; ");
				return false;
			}
      });
	
});