window.onload = () => checkUser();
let user = null;
let username = document.getElementById('username');
let userId = null;
async function checkUser() {
    try {
        const response = await fetch('/mycompiler/login');

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        if (data.status === 'success') {
            user = data.user.username;
            sessionStorage.setItem('userId',data.user.userId);
            userId = sessionStorage.getItem('userId');
            username.innerText += (' ' + user);
            getFiles(false);
        } else {
            console.log(data.message);
        }

        if (data.status !== 'success') {
            window.location.href = '/mycompiler/index.html';
        }
    } catch (error) {
        console.error('Error fetching user data:', error);
    }
}

const fileContainer = document.getElementById('fileContainer');
const pfc = document.getElementById('publicFileContainer');

async function getFiles(public) {
    const response = await fetch(`/mycompiler/getFiles?username=${encodeURIComponent(user)}&public=${public}`);
    const files = (await response.text()).split('\n');
    pfc.innerHTML = '';
    if(!public)
    fileContainer.innerHTML = '';

    for (let i = 0; i < files.length; i++) {
        const array = files[i].split(':');
        const id = array[2];
        const c2 = document.createElement('h4');

        if (array[0] !== '') {
            const div = document.createElement('div');
            div.classList.add('file');

            const c1 = document.createElement('h3');
            c1.innerText = array[0].trim();
            c1.style.flex = '1';
            let select = null;
            let del = null;
            if (!public) {
                select = document.createElement('select');
                select.style.flex = '1';
                select.style.backgroundColor = '#333';
                select.style.color = '#fff';
                select.style.border = '1px solid #444';
                select.style.borderRadius = '4px';
                select.style.borderTopRightRadius = '0px'
                select.style.borderBottomRightRadius = '0px'

                const publicOption = document.createElement('option');
                publicOption.value = '1';
                publicOption.innerText = 'private';

                const privateOption = document.createElement('option');
                privateOption.value = '0';
                privateOption.innerText = 'public';

                select.appendChild(publicOption);
                select.appendChild(privateOption);
                select.value = array[1];

                select.onchange = async () => {
                    const selectedValue = select.value;
                    const response = await fetch('/mycompiler/updateAccess', {
                        method: 'POST',
                        body: new URLSearchParams({
                            access: selectedValue,
                            fileId: id
                        })
                    });

                    const result = await response.text();
                    alert(result);
                };

                del = document.createElement('div');
                del.classList.add('delete');
                del.classList.add('btn');
                del.innerText = 'Delete';
                del.onclick = async () =>{
                    const response = await fetch(`/mycompiler/delete?id=${userId}&fileId=${id}`,{
                        method: 'DELETE'
                    });
                    const result = await response.json();

                    if(result.result){
                        alert(result.result);
                        getFiles(false);
                    }else{
                        alert(result.access);
                    }
                }
            } else {
                c2.innerText = 'Created By : ' + array[3].trim();
                c2.style.flex = '1';
            }

            c1.onclick = () => handleNavigate(id, array[0], public);
            div.appendChild(c1);

            if (public) {
                div.appendChild(c2);
                pfc.appendChild(div);
            } else {
                div.appendChild(select);
                div.appendChild(del);
                fileContainer.appendChild(div);
            }
        }

    }
}

async function handleSignOut() {
    const response = await fetch("/mycompiler/logout", {
        method: 'POST'
    });
    window.location.href = "/mycompiler/index.html";
}

function handleNavigate(id, name, public) {
    window.location.href = `/mycompiler/pages/dashboard.html?id=${id}&filename=${name}&publicFile=${public}`;
}

function newfile() {
    const filename = document.getElementById('overLay');
    filename.style.display = "flex";
    document.getElementById("name").focus();
}

function closee() {
    const filename = document.getElementById('overLay');
    filename.style.display = "none";
}

async function closeFileName(event) {
    event.preventDefault();
    const name = document.getElementById("name").value;
    const access = document.getElementById('access').value;
    if (name.endsWith('.c') || name.endsWith('.cpp') || name.endsWith('.c++')) {
        const response = await fetch("/mycompiler/saveFile", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                fileName: name,
                access: access
            }).toString()
        });
        let result = await response.text();
        if (result.includes('fileid')) {
            result = result.replace('fileid', '');
            const nameSec = document.getElementById("overLay");
            nameSec.style.display = "none";
            handleNavigate(result.trim(), name);
        } else {
            alert('FileName already Exists Give another name');
        }
    } else {
        alert('Required .c or cpp extension');
    }
}

function showPublicFiles() {
    const showbtn = document.getElementById('showBtn');
    if (pfc.style.display === 'flex') {
        pfc.style.display = 'none';
        showbtn.innerText = 'Show Public Files';
        document.getElementById('publicHeading').style.display = 'none';
        fileContainer.style.height = '70%';
    } else {
        getFiles(true);
        pfc.style.display = 'flex';
        showbtn.innerText = 'Hide Public Files';
        fileContainer.style.height = '35%';
        document.getElementById('publicHeading').style.display = 'block';
    }
}