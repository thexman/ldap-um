$(function() {
	var ajaxUtils = new AjaxUtils($('#loading-div'));
	var menuUtils = new MenuUtils();
	
	function showDialog(title, text) {
		$("<div title='" + title + "'>" + text + "</div>").dialog({
			modal: true,
			resizable: false,
			buttons: {
				"OK": function () {
					$(this).dialog("close");
				}
			}
		});
	}
	
	
	var div = $("#user-profile");
	var userUtils = new UserUtils(div);
	$.get("ws/users/currentUser", function(data) {
		//console.log(data);
		if (data.status === "success") {
			userUtils.setUser(data.currentUser);
		}
	});
	
	$("#save-user").click(function(e) {
		e.preventDefault();				
		var oldPassword = div.find("#old-password").val();
		var newPassword1 = div.find("#new-password-1").val();
		var newPassword2 = div.find("#new-password-2").val();
		if (newPassword1 === newPassword2) {
			var frm = $("#hidden-form");
			frm.find("#hidden-old-password").val(oldPassword);
			frm.find("#hidden-new-password").val(newPassword1);					
			$.post("ws/users/password", frm.serialize(), function(data) {
				if (data.status == "success" ) {
					showDialog("Info", "Password changed successfully");
				} else {
					showDialog("Error", data.message);
				}
			}, "json");				
		} else {
			showDialog("Warning", "New password doesn't match");					
		}
	});			
});
