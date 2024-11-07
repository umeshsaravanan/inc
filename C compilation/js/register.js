window.onload = () =>{
	document.getElementById('username').focus();
}

async function handleRegister(event) {
	event.preventDefault();
	const name = document.getElementById('username').value;
	const password = document.getElementById('password').value;
	const confirmPassword = document.getElementById('confirm-password').value;

	if(name != "" && password != "" && confirmPassword != "")
		if(password === confirmPassword){
			try {
				const response = await fetch('/mycompiler/register', {
					method: 'POST',
					headers: {
						'Content-Type': 'application/x-www-form-urlencoded',
					},
					body: new URLSearchParams({
						name: name,
						password: password
					}).toString()
				}).then(response => {
					if (response.redirected) {
						alert('Registeration Successful');
						window.location.href = response.url;
					} else {
						alert("UserName Already exists ;("); 
					}
				})
			} catch (error) {
				console.error('Error:', error);
			}
		}else
			alert("Both Password Must Match");
	
	else	
		alert("All fields are required");
}

function checkMatch(){
	const password = document.getElementById('password').value;
	const confirmPassword = document.getElementById('confirm-password').value;
	const errorTag = document.getElementById('errorMsg');
    if(password !== confirmPassword){
        errorTag.innerText = "Password should Match";
    }else{
        errorTag.innerText = "";
    }
}