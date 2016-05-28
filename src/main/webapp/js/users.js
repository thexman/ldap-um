var userUtils = null;
var selectedUser = null;

function editUser(uid) {
	var user = $("#users-grid").jqGrid('getRowData', uid);
	selectedUser = user;
	console.log(user);
	userUtils.setUser(user);	
	$("#user-dialog").dialog("open"); 
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
	
	
	var dialog = $("#user-dialog");
	userUtils = new UserUtils(dialog);
	
	
	var w = $(window).width() - 50;
	var h = $(window).height() - 150;
	var rowNum = 20;
	if (h > 1024) {
		rowNum = 100;
	} else if (h > 700) {
		rowNum = 50;
	}
	$("#users-grid").jqGrid({
		url: 'ws/users/rows',
		datatype: "json",
		colModel: [
			{ label: ' ', name: 'add', width: 16, sortable: false, search: false,
				formatter:function(cellvalue, options, rowObject) {
  					return "<span class='ui-icon ui-icon-pencil edit-user-button' data-uid=\"" + rowObject.uid.replace(/"/g, '\\\"') + "\"></span>";
				}	
			},				           
			{ label: 'ID', name: 'uid', width: 150, key:true },
			{ label: 'First name', name: 'firstName', width: 150 },
			{ label: 'Last name', name: 'lastName', width: 150 },
			{ label: 'Full name', name: 'fullName', width: 150 },
			{ label: 'Display name', name: 'displayName', width: 150 },
			{ label: 'Email', name: 'email', width: 250 },
			{ label: 'Groups', name: 'groups', width: 250 }
		],
		viewrecords: true,
		shrinkToFit: true,
		//autowidth: true,
		width: w, 
		height: h,
		rowNum: rowNum,
		rowList: [10, 20, 50, 100, 500],
		loadonce: true,
		pager: "#users-grid-pager"
	}).on("click", ".edit-user-button", function() {
		var uid = $(this).attr("data-uid");
		editUser(uid);
	});
	
	dialog.dialog({
		autoOpen: false,
		height: 500,
		width: 350,
		modal: true,
		close: function() { userUtils.reset(); },
		buttons: { 
			"Save": function() { userUtils.submit(); },
			"Cancel": function() { userUtils.closeDialog(); }
		}
	});
	 
	dialog.find("form").on("submit", function( event ) {				
		event.preventDefault();
		var isNew = (selectedUser == null);
		var user = userUtils.validate(isNew);
		if (user != null) {
			var form = $(this);					
			var url;
			if (isNew) {
				url = "ws/users/create";	
			} else {
				url = "ws/users/update";
			}
			 
			// Send the data using post
			var posting = $.post( url, { "user": JSON.stringify(user) } );
		 
			// Put the results in a div
			posting.done(function( data ) {					
				console.log(data);
				$("#users-grid").jqGrid('setGridParam',{datatype:'json'}).trigger('reloadGrid');
			});
		}
	});
	
	$("#create-user").click( function() {
		selectedUser = null;
		userUtils.reset();
		$("#user-dialog").dialog("open"); 
	} );
	
	$("#groups")
		// don't navigate away from the field on tab when selecting an item
		.bind( "keydown", function( event ) {
			if (event.keyCode === $.ui.keyCode.TAB && $(this).autocomplete("instance").menu.active) {
				event.preventDefault();
			}
		})
		.autocomplete({
			source: function(request, response) {
				$.getJSON("ws/groups/searchGroups", { search : extractLast(request.term) }, function(data) {
					response(data.groups);
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