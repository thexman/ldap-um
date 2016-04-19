
function GroupUtils(dialog) {
	
		// From http://www.whatwg.org/specs/web-apps/current-work/multipage/states-of-the-type-attribute.html#e-mail-state-%28type=email%29
	this.idRegEx = /^[a-z]([0-9a-z_\.])+$/i;
	this.frm = dialog.find("form");
	this.name = dialog.find("#name");
	this.users = dialog.find("#users");
	this.tips = dialog.find(".validateTips");

	this.allFields = function() {
		return $([]).add(this.name).add(this.users);
	};
		
	this.updateTips = function (t) {
		var tips = this.tips;
		tips.text(t).addClass("ui-state-highlight");
		setTimeout(function() { 
			tips.removeClass("ui-state-highlight", 1500); 
		}, 500);
	};

	this.checkLength = function(o, n, min, max) {
		if (o.val().length > max || o.val().length < min) {
			o.addClass("ui-state-error");
			this.updateTips("Length of '" + n + "' must be between " + min + " and " + max + ".");
			return false;
		} else {
			return true;
		}
	};

	this.checkRegexp = function(o, regexp, n) {
		if (!(regexp.test(o.val()))) {
			o.addClass("ui-state-error");
			this.updateTips(n);
			return false;
		} else {
			return true;
		}
	};

	this.submit = function() {
		this.frm.submit();
	};
				
	this.reset = function() {
		this.frm[0].reset();				
		this.allFields().val("");
		this.allFields().removeClass("ui-state-error");
	};
		
	this.closeDialog = function() {	
		dialog.dialog("close");
	};
		
	this.setGroup = function(group) {
		this.name.val(group.name);
		this.users.val(group.users); 
	};
	
	this.formatUser = function (user, appendSuffix) {
		var s = user.displayName + " (" + user.uid + ")";
		if (appendSuffix) {
			s += "; ";
		}
		return s;
	}
	
	this.validate = function (isNewUser) {		
		console.log("---validateInternal---");				
		var valid = true;
		this.allFields().removeClass("ui-state-error");
	
		valid = valid && this.checkLength(this.name, "Name", 2, 100);		
		if (valid) {			
			var group = {
					"cn": this.name.val(),
					"users": this.users.val()
			};
			this.closeDialog();
			return group;
		}
	};	
}