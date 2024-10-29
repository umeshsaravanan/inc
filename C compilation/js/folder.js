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
            sessionStorage.setItem('userId', data.user.userId);
            userId = sessionStorage.getItem('userId');
            username.innerText += (' ' + user);
            getFolders();
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

async function handleSignOut() {
    const response = await fetch("/mycompiler/logout", {
        method: 'POST'
    });
    window.location.href = "/mycompiler/index.html";
}

function newfile() {
    const filename = document.getElementById('overLay');
    filename.style.display = "flex";
}

function closee() {
    const filename = document.getElementById('overLay');
    filename.style.display = "none";
}

async function closeFileName(event) {
    event.preventDefault();
    document.getElementById('overlayUp').style.display = 'block';
    const folderInput = document.getElementById('folder');
    const files = folderInput.files;
    const topLevelFolder = files[0].webkitRelativePath.split('/')[0];
    const response = await fetch('/mycompiler/uploadFolder', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            folderName: topLevelFolder,
            userId: userId,
            isMain: true
        }).toString()
    })
    const result = await response.text();
    if (result == 1) {
        console.log("Main Folder Uploaded");
    } else {
        if (result == 0)
            document.getElementById('uploading').innerText = 'Error Occured While Uploading the FOlder, Try Again :(';
        else
            document.getElementById('uploading').innerText = 'The folder you are uploading is already available no duplicates are allowed !';
        document.getElementById('uploadBtn').disabled = false;
        return;
    }
    const folderSet = new Set();

    for (const file of files) {
        const relativePath = file.webkitRelativePath;
        const parts = relativePath.split('/');
        for (let i = 0; i < parts.length - 1; i++) {
            const folderPath = parts.slice(0, i + 1).join('/');
            folderSet.add(folderPath);
        }
    }

    const foldersList = Array.from(folderSet);
    foldersList.sort((a, b) => {
        return a.split('/').length - b.split('/').length;
    });


    const folderResponse = await fetch('/mycompiler/uploadFolder', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            folders: JSON.stringify(foldersList),
            userId: userId,
            isMain: false
        }).toString()
    });

    if ((await folderResponse.text()) == 1) {
        console.log("Folder Uploaded: " + folder);
    } else {
        console.log('Error Occurred while Uploading Folder: ' + folder);
    }

    const filesArray = Array.from(files);
    const fileData = filesArray.map(file => ({
        fileName: file.webkitRelativePath,
        size: file.size
    }));

    const responsee = await fetch('/mycompiler/uploadFiles', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            folders: JSON.stringify(foldersList),
            files: JSON.stringify(fileData),
            userId: userId,
            isMain: false
        }).toString()
    })
    if ((await responsee.text()) == 1) {
        console.log("file Uploaded");
    } else {
        console.log('Error Occured while Uploading Folder try again');
    }

    getFolders();
    document.getElementById('uploading').innerText = 'Uploaded Successfully :)';
    document.getElementById('uploadBtn').disabled = false;
}


let currentPath = [];
let totalSize = 0;
let selected = 0;
let items = [];
let backup = [];
async function getFolders(folderId = null) {
    const url = folderId
        ? `/mycompiler/getSubFolders?folderId=${encodeURIComponent(folderId)}`
        : `/mycompiler/getFolders?userId=${encodeURIComponent(userId)}`;

    const response = await fetch(url);
    const result = await response.json();
    console.log(result);
    items = result.names;
    backup = items;

    displayItems(true, 'KB');
}

function updateBreadcrumb(fromUser) {
    totalSize = 0;
    selected = 0;
    document.getElementById('selected').innerText = '';
    if (fromUser)
        document.getElementById('MemoryUnit').value = '1';
    const breadcrumb = document.getElementById('breadcrumb');
    breadcrumb.innerHTML = '';

    const rootLink = createBreadcrumbLink("Root", null);
    rootLink.onclick = () => {
        breadcrumb.innerHTML = '';
        currentPath = [];
        getFolders();
    };
    breadcrumb.appendChild(rootLink);
    breadcrumb.appendChild(document.createTextNode(' > '));

    currentPath.forEach((folder, index) => {
        const link = createBreadcrumbLink(folder.split(':')[0], folder.split(':')[3]);
        link.onclick = () => {
            currentPath = currentPath.slice(0, index + 1);
            getFolders(folder.split(':')[3]);
        };

        breadcrumb.appendChild(link);

        if (index < currentPath.length - 1) {
            breadcrumb.appendChild(document.createTextNode(' > '));
        }
    });
}

function createBreadcrumbLink(name, folderId) {
    const link = document.createElement('a');
    link.innerText = name;
    link.href = '#';
    link.dataset.folderId = folderId;
    return link;
}

function displayItems(fromUser = true, memory = 'KB') {
    const folderSection = document.getElementById('folderSection');
    folderSection.innerHTML = '';

    updateBreadcrumb(fromUser);

    for (const str of items) {
        if (str.trim().length > 0)
            document.getElementById('emptyMsg').style.display = 'none';
        const div = document.createElement('div');
        div.classList.add('folder');

        const checkbox = document.createElement('input');
        checkbox.classList.add('checkBoxBasis');
        checkbox.type = 'checkbox';
        checkbox.style.marginRight = '8px';
        const size = Number(str.split(':')[2]);

        checkbox.addEventListener('change', (event) => {
            let unit = 'bytes';
            if (event.target.checked) {
                totalSize += size;
                selected++;
            } else {
                selected--;
                totalSize -= size;
            }
            // console.log('Total Size:', totalSize);
            if (totalSize > 0) {
                let castedSize = totalSize;
                if (totalSize <= 1024) {
                    unit = 'bytes';
                }
                else if (totalSize > 1024 && totalSize <= (1024 * 1024)) {
                    unit = 'KB';
                    castedSize = (totalSize / 1024).toFixed(2);
                } else if (totalSize > (1024 * 1024) && totalSize <= (1024 * 1024 * 1024)) {
                    unit = 'MB';
                    castedSize = (totalSize / 1048576).toFixed(2);
                }
                else {
                    unit = 'GB';
                    castedSize = (totalSize / (1024 * 1024 * 1024)).toFixed(2);
                }
                document.getElementById('selected').innerText = `| ${selected} item selected ${castedSize} ${unit}`;
            } else {
                document.getElementById('selected').innerText = '';
            }
        });
        div.appendChild(checkbox);

        const isFolder = str.split(':')[1] === 'folder';
        const icon = document.createElement('i');
        icon.className = isFolder ? 'fas fa-folder iconBasis' : 'fas fa-file iconBasis';
        icon.style.marginRight = '8px';
        div.appendChild(icon);

        const p = document.createElement('p');
        p.classList.add('itemNameBasis');
        p.innerText = str.split(':')[0];

        const date = document.createElement('p');
        date.classList.add('dateBasis');
        date.innerText = isFolder ? str.split(':').slice(4).join(':') : str.split(':').slice(3).join(':');

        const type = document.createElement('p');
        type.classList.add('typeBasis');
        if (isFolder) {
            const newFolderId = str.split(':')[3];
            p.onclick = async () => {
                currentPath.push(str);
                await getFolders(newFolderId);
            };
            div.appendChild(p);
            p.style.cursor = 'pointer';
            type.innerText = 'Folder';
            div.appendChild(date);
            div.appendChild(type);
        } else {
            type.innerText = 'File';
            div.appendChild(p);
            div.appendChild(date);
            div.appendChild(type);
        }

        const h5 = document.createElement('h5');
        let round = null;
        if (memory === 'KB') {
            round = Math.round(size / 1024);
            h5.innerText = ((round === 0 ? 1 : round) + ' KB');
        } else if (memory === 'MB') {
            round = (size / (1024 * 1024)).toFixed(8);
            h5.innerText = (round + ' MB');
        } else if (memory === 'GB') {
            round = (size / (1024 * 1024 * 1024)).toFixed(8);
            h5.innerText = (round + ' GB');
        }
        div.appendChild(h5);

        folderSection.appendChild(div);
    }
    document.getElementById('selectionSection').innerText = `items : ${items.length}`;

    if (items.length === 0)
        document.getElementById('emptyMsg').style.display = 'block';
}

let prevUnit = 1;
let isAscending = true;
function sortByName(fromUser = false) {
    items = items
        .map(item => item.split(':'))
        .sort((a, b) => {
            const nameA = a[0].toLowerCase();
            const nameB = b[0].toLowerCase();
            return isAscending ? nameA.localeCompare(nameB) : nameB.localeCompare(nameA);
        })
        .map(item => item.join(':'));

    isAscending = !isAscending;
    if (fromUser) {
        const val = document.getElementById('MemoryUnit').value;
        const unit = val === '1' || val === '3' ? 'KB' : val === '2' ? 'MB' : val === '6' || val === '4' ? 'GB' : 'KB';
        displayItems(false, unit);
    }
}

function changeMemoryUnit(event) {
    // items = backup;
    const val = event.target.value;
    if (val === '1') {
        displayItems(false, 'KB');
    } else if (val === '2') {
        displayItems(false, 'MB');
    } else if (val === '3') {
        if (items.length >= 1)
            backup = items;
        items = items
            .map(item => item.split(':'))
            .filter(item => item[2] <= 16 * 1024)
            .map(item => item.join(':'));
        displayItems(false, 'KB');
    } else if (val === '4') {
        if (items.length >= 1)
            backup = items;
        items = items
            .map(item => item.split(':'))
            .filter(item => item[2] >= (1024 * 1024 * 1024))
            .map(item => item.join(':'));
        displayItems(false, 'GB');
    } else if (val === '5') {
        items = backup;
        displayItems(false, 'KB');
    } else {
        displayItems(false, 'GB');
    }
}

function closeUploading() {
    document.getElementById('overlayUp').style.display = 'none';
    document.getElementById('uploading').innerText = 'Uploading...';
    document.getElementById('uploadBtn').disabled = true;
    closee();
}