
function UserUtils(dialog) {
	
		// From http://www.whatwg.org/specs/web-apps/current-work/multipage/states-of-the-type-attribute.html#e-mail-state-%28type=email%29
	this.emailRegex = /^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
	this.idRegEx = /^[a-z]([0-9a-z_\.])+$/i;
	this.frm = dialog.find("form");				
	this.uid = dialog.find("#uid");
	this.firstName = dialog.find("#first-name");
	this.lastName = dialog.find("#last-name");
	this.displayName = dialog.find("#display-name");
	this.fullName = dialog.find("#full-name");
	this.email = dialog.find("#email");
	this.password = dialog.find("#password"); 		
	this.tips = dialog.find(".validateTips");

	this.allFields = function() {
		return $([]).add(this.uid).add(this.firstName).add(this.lastName).add(this.displayName).add(this.fullName).add(this.email).add(this.password);
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
		this.uid.prop("readonly", false);
		this.frm[0].reset();				
		this.allFields().val("");
		this.allFields().removeClass("ui-state-error");
	};
		
	this.closeDialog = function() {	
		dialog.dialog("close");
	};
		
	this.setUser = function(user) {
		this.uid.prop("readonly", true);
		this.uid.val(user.uid);
		this.firstName.val(user.firstName);
		this.lastName.val(user.lastName);
		this.displayName.val(user.displayName);
		this.fullName.val(user.fullName);
		this.email.val(user.email);
		this.password.val("");
	};
	
	this.validate = function (isNewUser) {		
		console.log("---validateInternal---");				
		var valid = true;
		this.allFields().removeClass("ui-state-error");
	
		valid = valid && this.checkLength(this.uid, "ID", 2, 16);
		valid = valid && this.checkLength(this.firstName, "First name", 1, 16);
		valid = valid && this.checkLength(this.lastName, "Last name", 1, 16);
		valid = valid && this.checkLength(this.fullName, "Full name", 2, 16);
		valid = valid && this.checkLength(this.displayName, "Display name", 2, 16);			
		valid = valid && this.checkLength(this.email, "Email", 6, 80);
		
		if (this.password.val().length > 0 || isNewUser) {
			valid = valid && this.checkLength(this.password, "Password", 5, 16);
		}
	
		valid = valid && this.checkRegexp(this.uid, this.idRegEx, "ID may consist of a-z, 0-9, underscores, dot and must begin with a letter.");
		valid = valid && this.checkRegexp(this.email, this.emailRegex, "eg. email@domain.com");
		
		if (valid) {			
			var user = {
					"uid": this.uid.val(),
					"firstName": this.firstName.val(),
					"lastName": this.lastName.val(),
					"fullName": this.fullName.val(),
					"displayName": this.displayName.val(),
					"email": this.email.val(),
					"password": this.password.val()
			};
			this.closeDialog();
			return user;
		}
	};	
}