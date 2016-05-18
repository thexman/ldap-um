function MenuUtils(div) {

	$.get("ws/users/isAdmin", function(data) {
		if (data.status === "success" && data.isAdmin) {
			$(".admin-role-required").show();
		} else {
			$(".admin-role-required").hide();
		}
	});
	
	return this;
}