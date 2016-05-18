function AjaxUtils(div) {

	$(document).ajaxStart(function () {
		div.show();
	}).ajaxStop(function () {
		div.hide();
	});
	
	this.show = function() {
		div.show();
	}
	
	this.hide = function() {
		div.hide();
	}		
	
	return this;
}
