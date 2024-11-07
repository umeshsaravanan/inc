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

//function to send a file to server
async function handleSubmit(uploadTypeId, fileInputId, checkId, uploadingId, folderDataVar) {
    document.getElementById('folder1').innerHTML = '';
    document.getElementById('folder2').innerHTML = '';
    document.getElementById('changes').innerHTML = '';

    document.getElementById(checkId).style.display = 'none';
    document.getElementById(uploadingId).innerText = 'Uploading...';

    const select = document.getElementById(uploadTypeId);
    const input = document.getElementById(fileInputId);

    let result = null;
    const formData = new FormData();

    if (select.value === 'folder') {
        const zip = new JSZip();
        for (const file of input.files) {
            zip.file(file.webkitRelativePath || file.name, file);
        }

        const content = await zip.generateAsync({ type: 'blob' });
        formData.append('zipFile', content, input.files[0].webkitRelativePath.split('/')[0] + '.zip');
    } else {
        if (input.files.length === 0) {
            console.log("No files selected.");
            return;
        }
        formData.append('zipFile', input.files[0]);
    }

    const response = await fetch('/mycompiler/uploadFoldersToCompare', {
        method: 'POST',
        body: formData
    });

    result = await response.json();

    if (result.Error) {
        document.getElementById(uploadingId).innerText = result.Error;
    } else {
        if (folderDataVar === 'folder1Data')
            folder1Data = result;
        else
            folder2Data = result;

        document.getElementById(checkId).style.display = 'inline-block';
        document.getElementById(uploadingId).innerText = '';
    }
}

//function to change the attributes of the input tag
function updateInputType(inputId, selectId) {
    const input = document.getElementById(inputId);
    const select = document.getElementById(selectId);

    if (select.value === 'folder') {
        input.setAttribute('webkitdirectory', '');
        input.setAttribute('multiple', '');
        input.removeAttribute('accept');
    } else {
        input.removeAttribute('webkitdirectory');
        input.removeAttribute('multiple');
        input.setAttribute('accept', '.zip');
    }
}

//displays the folder structure
function displayResult(data, name) {
    const resultContainer = document.getElementById(name);
    // resultContainer.innerHTML = '';
    const ul = document.createElement('ul');
    createList(name === 'folder2', data, ul);
    resultContainer.appendChild(ul);
}

// comparing 2 folders
function compareFolders() {

    document.getElementById('folder1').innerHTML = '';
    document.getElementById('folder2').innerHTML = '';
    document.getElementById('changes').innerHTML = '';

    document.getElementById('overlayUp').style.display = 'block';
    document.getElementById('uploading').innerText = 'Comparing Folders...';
    document.getElementById('uploadBtn').disabled = false;
    if (!folder1Data || !folder2Data) {
        document.getElementById('uploading').innerText = "Both folders must be uploaded to compare.";
        return;
    }

    setTimeout(() => {
        const changesContainer = document.getElementById('changes');
        changesContainer.innerHTML = '';

        const result = compareTrees(folder1Data, folder2Data);
        folder2Data = result.comparisonResult;

        console.log(folder2Data);

        folder1Data.directories.forEach((directory) => displayResult(directory, 'folder1'));
        folder2Data.directories.forEach((directory) => displayResult(directory, 'folder2'));
        folder1Data.files.forEach((file) => displayResult(file, 'folder1'));
        folder2Data.files.forEach((file) => displayResult(file, 'folder2'));


        const sizeChanges = [];

        if (result.changes.length > 0)
            result.changes.forEach(change => {
                if (change.includes('Directory')) {
                    changesContainer.innerHTML += `<br>${change}<br><br>`;
                } else if (change.includes('size changed')) {
                    sizeChanges.push(change);
                } else {
                    changesContainer.innerHTML += `${change}<br>`;
                }
            });
        else
            changesContainer.innerHTML = 'No Changes Detected';

        if (sizeChanges.length > 0) {
            changesContainer.innerHTML += `<br><h4>Size Changes :</h4><br>`
            sizeChanges.forEach(change => changesContainer.innerHTML += `${change}<br>`);
        }

        document.getElementById('uploading').innerText = 'Compared Successfully';
        analyseSize(min);
        saveHistory(folder2Data);
    }, 10);
}

//variable to hold min size value
let min = Number.MAX_VALUE;
//create html li elemets respective to folder structure
function createList(isFolder2, node, parentUl, currentPath = '', depth = 0) {
    const li = document.createElement('li');
    li.style.listStyle = 'none';
    let status = node.status;
    let colorApplied = false;

    if (!isFolder2 && status === 'new') {
        return;
    }

    if (isFolder2 && status) {
        if (status === 'new') {
            li.style.backgroundColor = 'rgb(10,160,100)';
            colorApplied = true;
        }
        else if (status === 'deleted') {
            li.style.backgroundColor = 'rgb(180,80,70)';
            colorApplied = true;
        }
    }
    const rowContainer = document.createElement('div');
    rowContainer.style.display = 'flex';
    rowContainer.style.alignItems = 'center';
    rowContainer.style.padding = '4px 0';

    const columnWidths = {
        name: 'calc(400px - ' + (depth * 20) + 'px)',
        type: '100px',
        size: '150px'
    };

    const caret = document.createElement('i');
    caret.className = 'fa fa-caret-right';
    caret.style.marginRight = '8px';
    caret.style.width = '16px';

    const nameSpan = document.createElement('span');
    nameSpan.textContent = node.name;
    nameSpan.style.flexBasis = columnWidths.name;
    if (!colorApplied && node.isDirectory && status === 'size changed') {
        rowContainer.style.color = 'rgb(200, 190, 60)';
    }

    const newPath = currentPath + node.name;
    const safeClassName = (newPath).trim().replace(/\s+/g, '_').replace(/[^a-zA-Z0-9_]/g, '_');
    nameSpan.classList.add(safeClassName);

    const typeSpan = document.createElement('span');
    typeSpan.textContent = node.isDirectory ? "Folder" : "File";
    typeSpan.style.flexBasis = columnWidths.type;

    const sizeSpan = document.createElement('span');
    sizeSpan.textContent = node.size - (node.sizeDifference && !isFolder2 ? node.sizeDifference : 0);
    sizeSpan.style.flexBasis = columnWidths.size;
    sizeSpan.classList.add('sizeSpan');

    const diffSizeSpan = document.createElement('span');
    diffSizeSpan.textContent = node.sizeDifference ? node.sizeDifference : '0';
    diffSizeSpan.classList.add('sizeSpan');
    diffSizeSpan.style.flexBasis = '100px';
    if (node.size < min) {
        min = node.size;
    }

    if (node.sizeDifference !== 0 && Math.abs(node.sizeDifference) < min) {
        min = node.sizeDifference;
    }

    if (node.isDirectory) {
        const childUl = document.createElement('ul');
        childUl.style.display = 'none';
        childUl.style.paddingLeft = '20px';

        nameSpan.onclick = (event) => clickSync(event);
        caret.onclick = (event) => clickSync(event);
        nameSpan.style.cursor = 'pointer';
        caret.style.cursor = 'pointer';
        function clickSync(event) {
            event.stopPropagation();

            const isVisible = childUl.style.display === 'block';
            const newDisplay = isVisible ? 'none' : 'block';

            const folderSpans = document.querySelectorAll('.' + safeClassName);
            folderSpans.forEach(fSpan => {
                const correspondingChildUl = fSpan.parentElement.nextElementSibling;
                if (correspondingChildUl && correspondingChildUl.tagName === 'UL') {
                    correspondingChildUl.style.display = newDisplay;
                    fSpan.previousElementSibling.className = newDisplay === 'block' ? 'fa fa-caret-down' : 'fa fa-caret-right';
                }
            });
        };

        node.directories?.sort((a, b) => a.name.localeCompare(b.name));
        node.directories.forEach(directory => {
            createList(isFolder2, directory, childUl, newPath, depth + 1);
        });

        node.files?.sort((a, b) => a.name.localeCompare(b.name));
        node.files?.forEach(file => {
            status = file.status;
            const fileLi = document.createElement('li');
            fileLi.style.listStyle = 'none';

            if (!isFolder2 && status === 'new') {
                return;
            }

            if (status) {
                if (isFolder2 && status === 'new') {
                    fileLi.style.backgroundColor = 'rgb(10,160,100)';
                    colorApplied = true;
                }
                else if (isFolder2 && status === 'deleted') {
                    fileLi.style.backgroundColor = 'rgb(180,80,70)';
                    colorApplied = true;
                }
                else if (status === 'size changed') {
                    fileLi.style.color = 'rgb(200, 190, 60)';
                }
            }

            const fileRowContainer = document.createElement('div');
            fileRowContainer.style.display = 'flex';
            fileRowContainer.style.alignItems = 'center';
            fileRowContainer.style.padding = '4px 0';

            const fileNameSpan = document.createElement('span');
            fileNameSpan.textContent = file.name;
            fileNameSpan.style.width = columnWidths.name;

            const fileTypeSpan = document.createElement('span');
            fileTypeSpan.textContent = "File";
            fileTypeSpan.style.width = columnWidths.type;
            fileTypeSpan.style.marginLeft = '4px';

            const fileSizeSpan = document.createElement('span');
            fileSizeSpan.textContent = file.size - (file.sizeDifference && !isFolder2 ? file.sizeDifference : 0);
            fileSizeSpan.style.width = columnWidths.size;
            fileSizeSpan.classList.add('sizeSpan');

            const diffFileSizeSpan = document.createElement('span');
            diffFileSizeSpan.textContent = file.sizeDifference ? file.sizeDifference : '0';
            diffFileSizeSpan.classList.add('sizeSpan');
            diffFileSizeSpan.style.flexBasis = '100px';

            if (file.size < min) {
                min = file.size;
            }

            if (file.sizeDifference !== 0 && Math.abs(file.sizeDifference) < min) {
                min = file.sizeDifference;
            }

            fileRowContainer.appendChild(fileNameSpan);
            fileRowContainer.appendChild(fileTypeSpan);
            fileRowContainer.appendChild(fileSizeSpan);
            if (isFolder2)
                fileRowContainer.appendChild(diffFileSizeSpan);

            fileLi.appendChild(fileRowContainer);
            childUl.appendChild(fileLi);
        });

        rowContainer.appendChild(caret);
        rowContainer.appendChild(nameSpan);
        rowContainer.appendChild(typeSpan);
        rowContainer.appendChild(sizeSpan);
        if (isFolder2)
            rowContainer.appendChild(diffSizeSpan);

        li.appendChild(rowContainer);
        li.appendChild(childUl);
    } else {
        rowContainer.appendChild(nameSpan);
        rowContainer.appendChild(typeSpan);
        rowContainer.appendChild(sizeSpan);
        li.appendChild(rowContainer);
    }

    parentUl.appendChild(li);
}

//comparing function
function compareTrees(data1, data2) {
    const changes = [];

    const comparisonResult = compareDirectories(data1, data2, '', changes);
    return { comparisonResult, changes };
}

//recursive function to compare
function compareDirectories(dir1, dir2, currentPath, changes) {

    let dir1Path = currentPath ? `${currentPath}/${dir1.name}` : dir1.name;
    let dir2Path = currentPath ? `${currentPath}/${dir2.name}` : dir2.name;
    dir1Path = dir1Path.endsWith('.zip') ? '' : dir1Path;
    dir2Path = dir2Path.endsWith('.zip') ? '' : dir2Path;

    if (dir1.name !== dir2.name) {
        // changes.push(`Directory ${dir1Path} is deleted`);
        collectPaths(dir1, dir1Path, 'deleted', changes);

        // changes.push(`Directory ${dir2Path} is added`);
        collectPaths(dir2, dir2Path, 'added', changes);

        const modifiedDir1 = JSON.parse(JSON.stringify(dir1));
        modifiedDir1.directories.forEach(innerDir => {
            innerDir.status = 'deleted';
        });
        modifiedDir1.files.forEach(file => {
            file.status = 'deleted';
        });

        dir2.directories.forEach(innerDir => {
            innerDir.status = 'new';
        });
        dir2.files.forEach(file => {
            file.status = 'new';
        });

        return {
            name: dir2.name,
            size: dir2.size,
            isDirectory: true,
            directories: [...modifiedDir1.directories, ...dir2.directories],
            files: [...modifiedDir1.files, ...dir2.files],
            sizeDifference: 0,
        };
    }

    const dirStatus = (dir1.size === dir2.size) ? 'common' : 'size changed';
    if (dirStatus === 'size changed') {
        dir1.status = 'size changed';
    }

    const result = {
        name: dir2.name,
        size: dir2.size,
        isDirectory: true,
        directories: [],
        files: [],
        sizeDifference: dir2.size - dir1.size,
        status: dirStatus
    };

    const dirMap1 = new Map(dir1.directories.map(dir => [dir.name, dir]));
    const dirMap2 = new Map(dir2.directories.map(dir => [dir.name, dir]));
    const fileMap1 = new Map(dir1.files.map(file => [file.name, file]));
    const fileMap2 = new Map(dir2.files.map(file => [file.name, file]));

    for (const name of dirMap1.keys()) {
        if (!dirMap2.has(name)) {
            let deletedPath = `${dir1Path}/${name}`;
            deletedPath = deletedPath.endsWith('.zip') ? '' : deletedPath;
            result.directories.push({ ...dirMap1.get(name), status: 'deleted' });
            changes.push(`Directory ${deletedPath} is deleted`);
            collectPaths(dirMap1.get(name), deletedPath, 'deleted', changes);
        } else {
            result.directories.push(compareDirectories(dirMap1.get(name), dirMap2.get(name), dir1Path, changes));
        }
    }

    for (const name of dirMap2.keys()) {
        if (!dirMap1.has(name)) {
            let newPath = `${dir2Path}/${name}`;
            newPath = newPath.endsWith('.zip') ? '' : newPath;
            result.directories.push({ ...dirMap2.get(name), status: 'new' });
            changes.push(`Directory ${newPath} is added`);
            collectPaths(dirMap2.get(name), newPath, 'added', changes);
        }
    }

    for (const name of fileMap1.keys()) {
        let filePath = `${dir1Path}/${name}`;
        filePath = filePath.endsWith('.zip') ? '' : filePath;
        if (!fileMap2.has(name)) {
            result.files.push({ ...fileMap1.get(name), status: 'deleted' });
            changes.push(`File ${filePath} is deleted`);
        } else {
            const fileStatus = (fileMap1.get(name).size === fileMap2.get(name).size) ? 'common' : 'size changed';
            const sizeDifference = fileMap2.get(name).size - fileMap1.get(name).size;
            result.files.push({
                ...fileMap2.get(name),
                status: fileStatus,
                sizeDifference
            });
            if (fileStatus === 'size changed') {
                fileMap1.get(name).status = 'size changed';
                changes.push(`File ${filePath} size changed by ${sizeDifference} bytes`);
            }
        }
    }

    for (const name of fileMap2.keys()) {
        let newFilePath = `${dir2Path}/${name}`;
        newFilePath = newFilePath.endsWith('.zip') ? '' : newFilePath;
        if (!fileMap1.has(name)) {
            result.files.push({ ...fileMap2.get(name), status: 'new' });
            changes.push(`File ${newFilePath} is added`);
        }
    }

    return result;
}

//collect one folders subfolders and files
function collectPaths(dir, path, msg, changes) {
    dir.directories.forEach(subDir => {
        let subDirPath = `${path}${path.length > 0 ? '/' : ''}${subDir.name}`;
        subDirPath = subDirPath.endsWith('.zip') ? '' : subDirPath;
        changes.push(`Directory ${subDirPath} is ${msg}`);
        collectPaths(subDir, subDirPath, msg, changes);
    });
    dir.files.forEach(file => {
        changes.push(`File ${path}/${file.name} is ${msg}`);
    });
}

// signout function
async function handleSignOut() {
    const response = await fetch("/mycompiler/logout", {
        method: 'POST'
    });
    window.location.href = "/mycompiler/index.html";
}

//close status module
function closeUploading() {
    document.getElementById('overlayUp').style.display = 'none';
    document.getElementById('uploading').innerText = 'Comparing Folders...';
    // document.getElementById('changes').scrollIntoView({ behavior: 'smooth' });
    document.getElementById('uploadBtn').disabled = true;
}

//function to change the size of the folders and files
function handleUnitChange(source, target) {
    const previousUnit = document.getElementById(target).value;
    const currentUnit = document.getElementById(source).value;
    document.getElementById(target).value = currentUnit;

    const sizeSpans = document.querySelectorAll('.sizeSpan');
    for (const span of sizeSpans) {
        if (currentUnit === '0') {
            let size = span.innerText;
            size = (size * Math.pow(1024, previousUnit)).toFixed(0);
            span.innerText = size;
        } else {
            let size = span.innerText;
            size = ((size * Math.pow(1024, previousUnit)) / (Math.pow(1024, currentUnit))).toFixed(2);
            span.innerText = size;
        }
    }
}

//function to decide the visibility of units in dropdown
function analyseSize(min) {

    if ((min / 1024).toFixed(2) == 0.00) {
        document.getElementById('kb1').style.display = 'none';
        document.getElementById('mb1').style.display = 'none';
        document.getElementById('gb1').style.display = 'none';
        document.getElementById('kb2').style.display = 'none';
        document.getElementById('mb2').style.display = 'none';
        document.getElementById('gb2').style.display = 'none';
    }
    else if ((min / (1024 * 1024)).toFixed(2) == 0.00) {
        document.getElementById('mb1').style.display = 'none';
        document.getElementById('mb2').style.display = 'none';
        document.getElementById('gb1').style.display = 'none';
        document.getElementById('gb2').style.display = 'none';
    }
    else if ((min / (1024 * 1024 * 1024)).toFixed(2) == 0.00) {
        document.getElementById('gb1').style.display = 'none';
        document.getElementById('gb2').style.display = 'none';
    }
}

async function saveHistory(d2) {
    const response = await fetch('/mycompiler/history', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            data2: JSON.stringify(d2),
            fName: d2.name
        }).toString()
    });
    console.log(await response.text());
}

async function history() {
    if (document.getElementById('historySection').style.display === 'block') {
        document.getElementById('historySection').style.display = 'none';
    }
    else {
        document.getElementById('historySection').style.display = 'block';

        const getResponse = await fetch('/mycompiler/history');
        const array = await getResponse.json();
        if (array.status === 'success') {
            document.getElementById('historyContent').innerHTML = '';
            for (const his of array.history) {
                const folderName = his.f_name;
                const hisId = his.h_id;
                const time = his.ct;
                const singleHistoryDiv = document.createElement('div');
                singleHistoryDiv.classList.add('singleDiv')

                singleHistoryDiv.onclick = () => handleSingleHistory(hisId);
                const p1 = document.createElement('P');
                p1.innerText = folderName;
                const p2 = document.createElement('P');
                p2.innerText = time;

                singleHistoryDiv.appendChild(p1);
                singleHistoryDiv.appendChild(p2);

                document.getElementById('historyContent').appendChild(singleHistoryDiv);
            }
        }

        if(array.history.length === 0)
            document.getElementById('historyContent').innerHTML = '<p style="text-align:center; margin-top:1rem;">Empty</p>';
    }
}

async function handleSingleHistory(hisId) {

    document.getElementById('folder1').innerHTML = '';
    document.getElementById('folder2').innerHTML = '';
    document.getElementById('changes').innerHTML = '';

    const response = await fetch('/mycompiler/getHistory', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            hisId: hisId
        }).toString()
    })

    const data = await response.json();
    console.log(data);
    data[0].directories.forEach((directory) => displayResult(directory, 'folder1'));
    data[0].directories.forEach((directory) => displayResult(directory, 'folder2'));
    data[0].files.forEach((file) => displayResult(file, 'folder1'));
    data[0].files.forEach((file) => displayResult(file, 'folder2'));
    analyseSize(min);
    document.getElementById('historySection').style.display = 'none';
}