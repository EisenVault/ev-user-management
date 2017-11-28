const PASS_MIN_LENGTH = 8;

function sendCallback(code, message) {
    status.code = code;
    status.message = message;
    status.redirect = true;
}

function checkPassword(pass){
	var values = /^(?=.*[\d])(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*])[\w!@#$%^&*]{8,}$/;	
	return values.test(pass);
}

function isValid(pass, pass2){
    if(PASS_MIN_LENGTH > pass.length){
        return false;
    }

    return pass.equals(pass2);
}