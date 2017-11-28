/**
 * User Profile Change Password Update method
 * 
 * @method POST
 */

function main()
{
	const PASS_MIN_LENGTH = 8;
   var oldpass = null;
   var newpass1 = null;
   var newpass2 = null;
   
   var names = json.names();
   for (var i=0; i<names.length(); i++)
   {
      var field = names.get(i);
      
      // look and set simple text input values
      if (field.indexOf("-oldpassword") != -1)
      {
         oldpass = new String(json.get(field));
      }
      else if (field.indexOf("-newpassword1") != -1)
      {
         newpass1 = new String(json.get(field));
      }
      else if (field.indexOf("-newpassword2") != -1)
      {
         newpass2 = new String(json.get(field));
      }
   }
   
   function validatePassword(newpass1){
		var values = /^(?=.*[\d])(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*])[\w!@#$%^&*]{8,}$/;	
		return values.test(newpass1);
	}

	function checkValid(newpass1, newpass2){
	    if(PASS_MIN_LENGTH > newpass1.length){
	        return false;
	    }
	    return newpass1.equals(newpass2);
	}
   // ensure we have valid values and that the new passwords match
   //if (newpass1.equals(newpass2))
   if (validatePassword(newpass1) && checkValid(newpass1, newpass2))
   {
      // perform the REST API to change the user password
      var params = new Array(2);
      params["oldpw"] = oldpass;
      params["newpw"] = newpass1;
      var connector = remote.connect("alfresco");
      var result = connector.post(
            "/api/person/changepassword/" + encodeURIComponent(user.name),
            jsonUtils.toJSONString(params),
            "application/json");
      if (result.status.code == 401) 
      {
         model.success = false;
         if (result.status.message != "")
         {
            model.errormsg = result.status.message;
         }
         else
         {
            // The proxy has eaten the message
            // This is caused by some SSO compatibility code somewhere...
            model.errormsg = msg.get("message.passwordchangeauthfailed");
         }
      }
      else
      {
         var repoJSON = JSON.parse(result);
         if (repoJSON.success !== undefined)
         {
            model.success = repoJSON.success;
         }
         else
         {
            model.success = false;
            model.errormsg = repoJSON.message;
         }
      }
   }
   else
   {
      model.success = false;
      model.errormsg = msg.get("Please enter a valid Password. Password must have 8 or more characters and at least one uppercase, one lowercase, one integer and one special character");
   }
}
main();