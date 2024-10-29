window.onload = () => checkUser();

async function checkUser() {
    try {
        const response = await fetch('/mycompiler/login');
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        if (data.status === 'success') {
            const user = data.user.username;
            sessionStorage.setItem('userId', data.user.userId);
            document.getElementById('username').innerText += (' ' + user);
        } else {
            console.log(data.message);
            window.location.href = '/mycompiler/index.html';
        }
    } catch (error) {
        console.error('Error fetching user data:', error);
    }
}

//scroll syncer
const folder1 = document.getElementById('folder1');
const folder2 = document.getElementById('folder2');

function syncScroll(source, target) {
    target.scrollTop = source.scrollTop;
}

folder1.addEventListener('scroll', () => syncScroll(folder1, folder2));
folder2.addEventListener('scroll', () => syncScroll(folder2, folder1));

//uploadFolder/zip and get the results
let folder1Data = null;
let folder2Data = null;

async function handleSubmit1() {
    document.getElementById('folder1').innerHTML = '';
    document.getElementById('folder2').innerHTML = '';
    document.getElementById('changes').innerHTML = '';


    document.getElementById('check1').style.display = 'none';
    document.getElementById('uploading1').innerText = 'Uploading...';

    const select = document.getElementById('uploadType1');
    const input = document.getElementById('fileInput1');

    if (select.value === 'folder') {
        const zip = new JSZip();
        for (const file of input.files) {
            zip.file(file.webkitRelativePath || file.name, file);
        }

        const content = await zip.generateAsync({ type: 'blob' });
        const formData = new FormData();
        formData.append('zipFile', content, input.files[0].webkitRelativePath.split('/')[0] + '.zip');

        const response = await fetch('/mycompiler/uploadFoldersToCompare', {
            method: 'POST',
            body: formData
        });

        folder1Data = await response.json();
    } else {
        if (input.files.length === 0) {
            console.log("No files selected.");
            return;
        }

        const formData = new FormData();
        formData.append('zipFile', input.files[0]);

        const response = await fetch('/mycompiler/uploadFoldersToCompare', {
            method: 'POST',
            body: formData
        });
        folder1Data = await response.json();
    }

    document.getElementById('check1').style.display = 'inline-block';
    document.getElementById('uploading1').innerText = '';
}

async function handleSubmit2() {
    document.getElementById('folder1').innerHTML = '';
    document.getElementById('folder2').innerHTML = '';
    document.getElementById('changes').innerHTML = '';


    document.getElementById('check2').style.display = 'none';
    document.getElementById('uploading2').innerText = 'Uploading...';

    const select = document.getElementById('uploadType2');
    const input = document.getElementById('fileInput2');

    if (select.value === 'folder') {
        const zip = new JSZip();
        for (const file of input.files) {
            zip.file(file.webkitRelativePath || file.name, file);
        }

        const content = await zip.generateAsync({ type: 'blob' });
        const formData = new FormData();
        formData.append('zipFile', content, input.files[0].webkitRelativePath.split('/')[0] + '.zip');

        const response = await fetch('/mycompiler/uploadFoldersToCompare', {
            method: 'POST',
            body: formData
        });
        folder2Data = await response.json();
    } else {
        if (input.files.length === 0) {
            console.log("No files selected.");
            return;
        }

        const formData = new FormData();
        formData.append('zipFile', input.files[0]);

        const response = await fetch('/mycompiler/uploadFoldersToCompare', {
            method: 'POST',
            body: formData
        });
        folder2Data = await response.json();
    }

    document.getElementById('check2').style.display = 'inline-block';
    document.getElementById('uploading2').innerText = '';
}

function updateInputType(inputId, selectId) {
    const input = document.getElementById(inputId);
    const select = document.getElementById(selectId);

    if (select.value === 'folder') {
        input.setAttribute('webkitdirectory', '');
        input.setAttribute('directory', '');
        input.removeAttribute('accept');
    } else {
        input.removeAttribute('webkitdirectory');
        input.removeAttribute('directory');
        input.setAttribute('accept', '.zip');
    }
}

//displays the folder structure
function displayResult(data, name) {
    const resultContainer = document.getElementById(name);
    resultContainer.innerHTML = '';
    const ul = document.createElement('ul');
    createList(data, ul);
    resultContainer.appendChild(ul);
}

//create html li elemets respective to folder structure
function createList(node, parentUl, currentPath = '') {
    const li = document.createElement('li');

    const span = document.createElement('span');
    span.textContent = node.name;

    const safeClassName = (currentPath + node.name).trim().replace(/\s+/g, '_').replace(/[^a-zA-Z0-9_]/g, '_');
    span.classList.add(safeClassName);

    const caret = document.createElement('i');
    caret.className = 'fa fa-caret-right';
    caret.style.marginRight = '8px';

    if (node.isDirectory) {
        const childUl = document.createElement('ul');
        childUl.style.display = 'none';

        span.onclick = function (event) {
            event.stopPropagation();

            const folderSpans = document.querySelectorAll('.' + safeClassName);
            folderSpans.forEach(fSpan => {
                const correspondingChildUl = fSpan.nextElementSibling;
                const isVisible = correspondingChildUl.style.display === 'block';
                correspondingChildUl.style.display = isVisible ? 'none' : 'block';
                fSpan.previousElementSibling.className = isVisible ? 'fa fa-caret-right' : 'fa fa-caret-down';
            });
        };

        const newPath = currentPath + node.name;

        node.directories.forEach(directory => {
            createList(directory, childUl, newPath);
        });

        node.files.forEach(file => {
            const fileLi = document.createElement('li');
            fileLi.textContent = `${file.name} (${file.size} bytes)`;
            childUl.appendChild(fileLi);
        });
        li.appendChild(caret);
        li.appendChild(span);
        li.appendChild(childUl);
    } else {
        li.appendChild(span);
    }

    parentUl.appendChild(li);
}

// comparing 2 folders
function compareFolders() {
    document.getElementById('overlayUp').style.display = 'block';
    if (!folder1Data || !folder2Data) {
        document.getElementById('uploading').innerText = "Both folders must be uploaded to compare.";
        document.getElementById('uploadBtn').disabled = false;
        return;
    }

    const changesContainer = document.getElementById('changes');
    changesContainer.innerHTML = '';

    const folder1Changes = [];
    const folder2Changes = [];
    const bothChanges = [];
    const sizeChanges = [];

    compareData(folder1Data, folder2Data, changesContainer, folder1Changes, folder2Changes, sizeChanges, bothChanges);

    if (folder1Changes.length === 0 && folder2Changes.length === 0) {
        changesContainer.innerHTML += "No changes detected.";
    } else {
        if (folder2Changes.length > 0)
            changesContainer.innerHTML += '<h3>Missing in Folder 1</h3><br>';
        folder2Changes.forEach(change => changesContainer.innerHTML += change);

        if (folder1Changes.length > 0)
            changesContainer.innerHTML += '<br><h3>Missing in Folder 2</h3><br>';
        folder1Changes.forEach(change => changesContainer.innerHTML += change);

        if (sizeChanges.length > 0)
            changesContainer.innerHTML += '<br><h3>size changes</h3><br>';
        sizeChanges.forEach(change => changesContainer.innerHTML += change);
    }

    displayResult(folder1Data, 'folder1');
    displayResult(folder2Data, 'folder2');
    document.getElementById('uploading').innerText = 'Compared Successfully';
    document.getElementById('uploadBtn').disabled = false;
}

function compareData(data1, data2, changesContainer, folder1Changes, folder2Changes, sizeChanges, bothChanges) {

    function compareDirectories(dir1, dir2, path) {
        const dirMap1 = Object.fromEntries(dir1.directories.map(dir => [dir.name, dir]));
        const dirMap2 = Object.fromEntries(dir2.directories.map(dir => [dir.name, dir]));

        Object.keys(dirMap1).forEach(name => {
            if (!(name in dirMap2)) {
                const message = `Directory ${path}/${name} is missing in Folder 2.<br><br>`;
                if (!bothChanges.includes(message)) {
                    folder1Changes.push(message);
                    bothChanges.push(message);
                }

                const missingDir = dirMap1[name];
                if (missingDir) {
                    missingDir.files.forEach(file => {
                        folder1Changes.push(`File ${path}/${name}/${file.name} is missing in Folder 2.<br>`);
                        bothChanges.push(`File ${path}/${name}/${file.name} is missing in Folder 2.<br>`);
                    });
                    compareDirectories(missingDir, { directories: [], files: [] }, `${path}/${name}`);
                }
            } else {
                compareDirectories(dirMap1[name], dirMap2[name], `${path}/${name}`);
            }
        });

        Object.keys(dirMap2).forEach(name => {
            if (!(name in dirMap1)) {
                const message = `Directory ${path}/${name} is missing in Folder 1.<br><br>`;
                if (!bothChanges.includes(message)) {
                    folder2Changes.push(message);
                    bothChanges.push(message);
                }

                const missingDir = dirMap2[name];
                if (missingDir) {
                    missingDir.files.forEach(file => {
                        folder2Changes.push(`File ${path}/${name}/${file.name} is missing in Folder 1.<br>`);
                        bothChanges.push(`File ${path}/${name}/${file.name} is missing in Folder 1.<br>`);
                    });

                    compareDirectories({ directories: [], files: [] }, missingDir, `${path}/${name}`);
                }
            }
        });

        const files1 = Object.fromEntries(dir1.files.map(file => [file.name, file.size]));
        const files2 = Object.fromEntries(dir2.files.map(file => [file.name, file.size]));

        Object.keys(files1).forEach(fileName => {
            if (!(fileName in files2)) {
                const message = `File ${path}/${fileName} is missing in Folder 2.<br>`;
                if (!bothChanges.includes(message)) {
                    folder2Changes.push(message);
                    bothChanges.push(message);
                }
            } else {
                if (files1[fileName] && files2[fileName] && files1[fileName] !== files2[fileName]) {
                    const message = `File ${path}/${fileName} size differs: Folder 1 = ${files1[fileName]} bytes, Folder 2 = ${files2[fileName]} bytes.<br>`;
                    if (!bothChanges.includes(message)) {
                        sizeChanges.push(message);
                    }
                }
            }
        });

        Object.keys(files2).forEach(fileName => {
            if (!(fileName in files1)) {
                const message = `File ${path}/${fileName} is missing in Folder 1.<br>`;
                if (!bothChanges.includes(message)) {
                    folder2Changes.push(message);
                    bothChanges.push(message);
                }
            }
        });
    }

    compareDirectories(data1, data2, data1.name);
}

//sign out function
async function handleSignOut() {
    const response = await fetch("/mycompiler/logout", {
        method: 'POST'
    });
    window.location.href = "/mycompiler/index.html";
}

function closeUploading() {
    document.getElementById('overlayUp').style.display = 'none';
    document.getElementById('uploading').innerText = 'Comparing Folders...';
    document.getElementById('changes').scrollIntoView({ behavior: 'smooth' });
    document.getElementById('uploadBtn').disabled = true;
}
