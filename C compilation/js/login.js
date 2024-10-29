window.onload = () => checkUser();

async function checkUser() {
    try {
        const response = await fetch('/mycompiler/login');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const data = await response.json();
        console.log(data);
        if (data.status === 'success') {
            if (data.user.leftAt && data.user.leftAt !== "null") {
                sessionStorage.setItem('userId',data.user.userId);
                window.location.href = data.user.leftAt;            
            }else{
                console.log(data.user.leftAt);
            }
        } else {
            document.getElementById('username').focus();
            console.log(data.message);
        }
    } catch (error) {
        console.error('Error:', error);
        console.log('Failed to check user session.');
    }
}

function onTabChange(){
    if(document.visibilityState === 'visible')
        checkUser();
}
document.addEventListener('visibilitychange',onTabChange);

async function handleLogin(event) {
    event.preventDefault();
    const name = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (name && password) {
        try {
            const response = await fetch('/mycompiler/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({
                    name: name,
                    password: password
                }).toString()
            });

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            if (data.status === "success") {
                sessionStorage.setItem('userId',data.userId);
                // alert('Logged in');
                window.location.href = data.leftAt !== 'null' ? data.leftAt : '/mycompiler/pages/FilesPage.html';
            } else {
                alert(data.message);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Login failed.');
        }
    } else {
        alert("All fields are required");
    }
}
