$(function() {
	var ajaxUtils = new AjaxUtils($('#loading-div'));
	
	$.get("ws/users/isAdmin", function(data) {
		if (data.status === "success" && data.isAdmin) {
			$(".admin-role-required").show();
		} else {
			window.location.href = "profile.html";
		}
	});
});
