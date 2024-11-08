window.onload = function () {
    const codeSection = document.getElementById("codeSection");
    codeSection.innerHTML = '';
    codeSection.focus();
};

let prevCurPos = null;
codeSection.addEventListener('click', function () {
    const cursorPos = saveCursorPosition(codeSection);
    if (lastSuggested && lastWord && lastWordd) {
        if (cursorPos <= prevCurPos + lastSuggested.length + 1 && cursorPos >= prevCurPos) {
            restoreCursorPosition(codeSection, cursorPos);
            highlightKeywords();
        } else {
            lastWord = lastWordd.replace(lastSuggested, '');
            codeSection.innerText = codeSection.innerText.substring(0, prevCurPos) + codeSection.innerText.substring(prevCurPos).replace(lastSuggested, '');
            highlightKeywords();
            restoreCursorPosition(codeSection, prevCurPos + 1);
        }
        lastSuggested = null;
        lastWord = null;
        lastWordd = null;
    }
});

function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
}

function unEscapeHtml(safe) {
    return safe
        .replace(/&amp;/g, "&")
        .replace(/&lt;/g, "<")
        .replace(/&gt;/g, ">");
}

const dataTypes = [
    "int", "float", "double", "const", "void", "struct", "union",
    "char", "short", "long", "unsigned", "signed", "bool", "enum",
    "size_t", "int8_t", "int16_t", "int32_t", "int64_t",
    "uint8_t", "uint16_t", "uint32_t", "uint64_t", "wchar_t",
    "std::string", "std::vector", "std::map", "std::set", "auto", "decltype", "string"
];

const keywords = [
    "if", "else", "for", "while", "do", "#", "include", "return",
    "switch", "case", "default", "break", "continue", "goto",
    "static", "extern", "typedef", "volatile", "inline",
    "asm", "restrict", "sizeof", "nullptr", "try", "catch", "throw",
    "namespace", "using", "class", "public", "private", "protected",
    "virtual", "friend", "const_cast", "dynamic_cast", "static_cast",
    "reinterpret_cast", "new", "delete", "new[]", "delete[]", "this", "define",
    "sizeof", "alignof", "alignas", "noexcept", "printf", "scanf", "cin", "cout", "fprint", "fscan"
];

const cBuiltInFunctions = ["abs", "acos", "asctime", "atoi", "atof", "calloc", "clock", "cos", "exp", "fclose", "fgets", "fprintf",
    "fscanf", "free", "fwrite", "log", "malloc", "memcpy", "memset", "pow", "printf", "scanf", "sin", "sqrt", "strcat", "strcmp",
    "strcpy", "strlen", "time", "min", "max"];

let allSuggestions = [...dataTypes, ...keywords, ...cBuiltInFunctions].sort();

function highlightKeywords() {
    const codeSection = document.getElementById("codeSection");
    const originalText = codeSection.innerText;
    const cursorPos = saveCursorPosition(codeSection);
    codeSection.innerHTML = highlight(originalText, true);

    const resultDiv = document.getElementById("resultSec");
    if (document.activeElement !== resultDiv) {
        restoreCursorPosition(codeSection, cursorPos);
    }
}

function highlight(text, shouldEscape) {
    // alert(text)
    if (shouldEscape) {
        text = escapeHtml(text);
    }
    // alert(text)
    let multiLineComment = false;

    const lines = text.split('\n').map(line => {
        if (line.includes('<span')) {
            return line;
        }
        if (multiLineComment) {
            if (line.endsWith('*/')) {
                multiLineComment = false;
                return `<span class="comment">${line}</span>`;
            }
            else if (line.trim().includes('*/')) {
                multiLineComment = false;
                const array = line.split('*/');
                return `<span class='comment'>${array[0] + '*/'}</span>` + highlight(array[1], false);
            } else
                return `<span class="comment">${line}</span>`;
        }

        if (line.trim().startsWith('/*')) {
            multiLineComment = true;
            return `<span class="comment">${line}</span>`;
        }
        if (line.trim().includes('/*')) {
            multiLineComment = true;
            const array = line.split('/*');
            if (line.trim().includes('*/')) {
                const array2 = array[1].split('*/');
                return highlight(array[0], false) + `<span class='comment'>${"/*" + array2[0] + "*/"}</span>` + highlight(array2[1, false]);
            } else {
                return highlight(array[0], false) + `<span class='comment'>${"/*" + array[1]}</span>`;
            }
        }
        if (line.trim().startsWith('//')) {
            return `<span class="comment">${line}</span>`;
        }

        if (line.includes('//')) {
            const array = line.split('//');
            const array2 = array.slice(1).join('//').split('\n');
            if (array2.length > 1)
                return highlight(array[0], false) + `<span class='comment'>${"//" + array2[0]}</span>\n` + (array2.length >= 3 ? highlight(array2.slice(1).join('\n'), false) : highlight(array2[1], false));
            else
                return highlight(array[0], false) + `<span class='comment'>${"//" + array[1]}</span>`;
        }

        if (line.includes('#include')) {
            const match = line.match(/#include\s*(["'])(.*?)\1/);

            if (match) {
                const quote = (match[1]);
                const content = (match[2]);

                return `<span class='include'>${(match[0].slice(0, match.index + 8))}</span>` +
                    `<span class='keyword'>${quote}${content}${quote}</span>`;
            } else {
                let array = line.split(escapeHtml('<'));
                if (array.length > 1) {
                    return `<span class='include'>${array[0]}</span>` +
                        `<span class='keyword'>${"&lt;" + array[1]}</span>`;
                } else {
                    return `<span class='include'>${(line)}</span>`;
                }
            }

        }

        const keywordsToHighlight = ["printf", "scanf", "cin", "cout"];

        if (keywordsToHighlight.some(keyword => line.includes(keyword))) {
            const wordRegex = new RegExp(`\\b(${keywordsToHighlight.join('|')})\\b`, 'g');
            line = unEscapeHtml(line);
            let segments = line.split(/(\);)/);
            if (line.includes('cin') || line.includes('cout'))
                segments = line.split(';');

            const highlightedSegments = segments.map(segment => {
                segment = escapeHtml(segment)
                const seenKeyword = { highlighted: false };
                return segment.replace(wordRegex, (match) => {
                    if (!seenKeyword.highlighted) {
                        seenKeyword.highlighted = true;
                        return `<span class="keyword">${match}</span>`;
                    }
                    return match;
                });
            });
            if (line.includes('cin') || line.includes('cout'))
                return highlightedSegments.join(';');
            else
                return highlightedSegments.join('');
        }

        wordRegex = new RegExp(`(\\b(${keywords.join('|')})\\b|\\b(${dataTypes.join('|')})\\b)`, 'g');

        line = line.replace(wordRegex, (match) => {
            if (keywords.includes(match)) {
                return `<span class='keyword'>${match}</span>`;
            } else if (dataTypes.includes(match)) {
                return `<span class='data-type'>${match}</span>`;
            }
            return match;
        });

        if (line.includes('(')) {
            const array = line.split(' ');
            const newArray = array.map(word => {
                if (word.includes('(')) {
                    const array2 = word.split('(');
                    if (array2.length >= 2) {
                        for (let i = 0; i < array2.length - 1; i++) {
                            if (array2[i].includes('>')) {
                                continue;
                            }
                            if (array2[i].includes(')')) {
                                let index = array2[i].indexOf(')');
                                if (index !== -1 && [',', '.', ';'].includes(array2[i][index + 1])) {
                                    index++;
                                }
                                array2[i] = array2[i].substring(0, index + 1) + `<span class="function">${array2[i].substring(index + 1)}</span>`;
                            } else
                                array2[i] = `<span class="function">${array2[i]}</span>`;
                        }
                        return array2.join('(');
                    } else {
                        if (word.includes('>'))
                            return word;

                        const index = word.indexOf('(');
                        const openindex = word.indexOf('(');
                        let closeindex = word.indexOf(')');

                        if (closeindex != -1 && closeindex < openindex) {
                            if ([',', '.', ';'].includes(word.charAt(closeindex + 1)))
                                closeindex++;
                            return word.substring(0, closeindex + 1) + `<span class="function">${word.substring(closeindex + 1, openindex)}</span>` + word.substring(openindex);
                        }
                        else
                            return `<span class="function">${word.substring(0, index)}</span>` + word.substring(index);
                    }
                } else {
                    return word;
                }
            })

            return newArray.join(' ');
        } else {
            return line;
        }
    });

    return lines.join('\n');
}

function saveCursorPosition(element) {
    const selection = window.getSelection();
    if (selection.rangeCount === 0) return 0;

    const range = selection.getRangeAt(0);
    if (!element.contains(range.endContainer)) {
        return 0;
    }
    const preRange = range.cloneRange();
    preRange.selectNodeContents(element);
    preRange.setEnd(range.endContainer, range.endOffset);
    return preRange.toString().length;
}

function restoreCursorPosition(element, offset) {
    const range = document.createRange();
    const selection = window.getSelection();
    selection.removeAllRanges();

    let charIndex = 0;
    let node = element;
    let found = false;

    function traverseNodes(currentNode) {
        for (let i = 0; i < currentNode.childNodes.length; i++) {
            const child = currentNode.childNodes[i];
            if (child.nodeType === Node.TEXT_NODE) {
                const textLength = child.textContent.length;

                if (charIndex + textLength >= offset) {
                    range.setStart(child, offset - charIndex);
                    found = true;
                    break;
                } else {
                    charIndex += textLength;
                }
            } else {
                traverseNodes(child);
                if (found) break;
            }
        }
    }

    traverseNodes(node);

    range.collapse(true);
    selection.addRange(range);
    element.focus();
}

let lastSuggested = null;
let lastWordd = null;
let lastWord = null;
let isbackspacePressed = false;
let notToSuggest = false;
let isTabPressed = false;
function suggestion() {
    const codeArea = document.getElementById('codeSection');
    const cursorPos = saveCursorPosition(codeArea);

    if (isbackspacePressed) {
        isbackspacePressed = false;
        highlightKeywords();
        return;
    }
    if (notToSuggest) {
        notToSuggest = false;
        highlightKeywords();
        return;
    }
    if (isTabPressed) {
        isTabPressed = false;
        highlightKeywords();
        return;
    }
    const text = codeArea.innerText;
    const beforeCursor = text.substring(0, cursorPos);
    let = afterCursor = text.substring(cursorPos);
    let spaceIndexBefore = beforeCursor.lastIndexOf(' ') + 1;
    spaceIndexBefore = spaceIndexBefore === 0 || spaceIndexBefore < beforeCursor.lastIndexOf('\n') ? beforeCursor.lastIndexOf('\n') + 1 : spaceIndexBefore;
    lastWord = beforeCursor.substring(spaceIndexBefore);

    let spaceIndex = afterCursor.search(/[ \n]/);
    spaceIndex = spaceIndex === -1 ? afterCursor.length : spaceIndex;

    let matched = false;

    if (lastWord) {
        for (const word of allSuggestions) {
            if (word.startsWith(lastWord)) {
                matched = true;
                lastSuggested = word.slice(lastWord.length);
                lastWordd = lastWord + word.slice(lastWord.length);
                lastWord += `<span class="suggestion">${word.slice(lastWord.length)}</span>`;
                break;
            }
        }
    }
    const highlightedBefore = highlight(beforeCursor.substring(0, spaceIndexBefore), true);

    if (matched) {
        const highlightedAfter = highlight(afterCursor.substring(spaceIndex), true);
        codeArea.innerHTML = highlightedBefore + lastWord + highlightedAfter;
    } else {
        if (lastSuggested) {
            afterCursor = afterCursor.substring(spaceIndex);
        }
        lastSuggested = null;
        lastWord = null;
        lastWordd = null;
        codeArea.innerHTML = escapeHtml(beforeCursor + afterCursor);
        highlightKeywords();
    }

    restoreCursorPosition(codeArea, cursorPos);
}

function handleKeyDown(event) {
    const codeSection = document.getElementById("codeSection");
    const cursorPos = saveCursorPosition(codeSection);
    const nextChar = codeSection.innerText[cursorPos] || '';
    const prevChar = codeSection.innerText[cursorPos - 1] || '';
    prevCurPos = cursorPos;

    if (event.ctrlKey && (event.key === 'a' || event.key === 'A')) {
        if (lastSuggested && lastWord && lastWordd) {
            lastWord = lastWordd.replace(lastSuggested, '');
            codeSection.innerText = codeSection.innerText.substring(0, prevCurPos) + codeSection.innerText.substring(prevCurPos).replace(lastSuggested, '');
            highlightKeywords();
            restoreCursorPosition(codeSection, prevCurPos + 1);
            lastSuggested = null;
            lastWord = null;
            lastWordd = null;
            isbackspacePressed = true;
        }
    }

    if (/./.test(nextChar) && nextChar !== ' ')
        notToSuggest = true;

    if (lastSuggested && lastSuggested.charAt(0) === nextChar)
        notToSuggest = false;

    if (prevChar === '')
        notToSuggest = false;

    if (event.key === 'Delete') {
        notToSuggest = true;
    }

    if (event.key === 'Backspace') {
        isbackspacePressed = true;
        setTimeout(() => {
            if (codeSection.innerText.length === 1) {
                codeSection.innerText = '';
            }
        }, 10)
    }

    if ((event.key === 'Backspace' || event.key === 'Delete') && lastSuggested && lastWord && lastWordd) {
        event.preventDefault();
        lastWord = lastWordd.replace(lastSuggested, '');
        codeSection.innerText = codeSection.innerText.substring(0, cursorPos) + codeSection.innerText.substring(cursorPos).replace(lastSuggested, '');
        highlightKeywords();
        restoreCursorPosition(codeSection, cursorPos);
        lastSuggested = null;
        lastWord = null;
        lastWordd = null;
        isbackspacePressed = true;
    }

    if (event.key === 'Tab' && lastWord && lastSuggested && lastWordd) {
        event.preventDefault();
        restoreCursorPosition(codeSection, cursorPos + lastSuggested.length);
        highlightKeywords();
        lastSuggested = null;
        lastWord = null;
        lastWordd = null;
        return;
    }
    if (event.key === 'Tab') {
        event.preventDefault();
        isTabPressed = true;
        document.execCommand("insertText", false, '    ');
        return;
    }
    if ((event.key === 'ArrowRight' || event.key === 'ArrowLeft') && lastWord && lastSuggested && lastWordd) {
        event.preventDefault();
        if (event.key === 'ArrowRight')
            restoreCursorPosition(codeSection, cursorPos + lastSuggested.length);
        else
            restoreCursorPosition(codeSection, cursorPos - 1);
        lastSuggested = null;
        lastWord = null;
        lastWordd = null;
        highlightKeywords();
        return;
    }

    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
        if (lastWord && lastSuggested && lastWordd) {
            lastWord = lastWordd.replace(lastSuggested, '');
            codeSection.innerText = codeSection.innerText.substring(0, cursorPos) + codeSection.innerText.substring(cursorPos).replace(lastSuggested, '');
            highlightKeywords();
            restoreCursorPosition(codeSection, cursorPos);
        }
        lastSuggested = null;
        lastWord = null;
        lastWordd = null;
    }

    if (event.key === ']') {
        if (nextChar === ']') {
            event.preventDefault();
            restoreCursorPosition(codeSection, cursorPos + 1);
        } else {
            event.preventDefault();
            document.execCommand('insertText', false, ']');
            restoreCursorPosition(codeSection, cursorPos + 1);
        }
    }
    if (event.key === ')') {
        if (nextChar === ')') {
            event.preventDefault();
            restoreCursorPosition(codeSection, cursorPos + 1);
        } else {
            event.preventDefault();
            document.execCommand('insertText', false, ')');
            restoreCursorPosition(codeSection, cursorPos + 1);
        }
    }

    if (event.key === '"') {
        event.preventDefault();
        if (prevChar === '"') {
            restoreCursorPosition(codeSection, cursorPos + 1);
        } else {
            document.execCommand('insertText', false, '""');
            restoreCursorPosition(codeSection, cursorPos + 1);
        }
    }
    if (event.key === '}') {
        event.preventDefault();
        subString = codeSection.innerText.substring(0, cursorPos);
        if (subString.lastIndexOf("    ") === subString.length - 4) {
            restoreCursorPosition(codeSection, cursorPos - 4);
        }
        if (prevChar != '{')
            document.execCommand('insertText', false, '}');
        else
            if (prevChar === '{' && nextChar !== '}') {
                document.execCommand('insertText', false, '}');
                restoreCursorPosition(codeSection, cursorPos + 1);
            } else {
                restoreCursorPosition(codeSection, cursorPos + 1);
            }
    }
    if (event.key === "'") {
        event.preventDefault();
        if (prevChar === "'") {
            restoreCursorPosition(codeSection, cursorPos + 1);
        } else {
            document.execCommand('insertText', false, "''");
            restoreCursorPosition(codeSection, cursorPos + 1);
        }
    }

    if (event.key === '{') {
        event.preventDefault();
        document.execCommand("insertText", false, "{}");
        restoreCursorPosition(codeSection, cursorPos + 1);
    }
    if (event.key === '(') {
        event.preventDefault();
        document.execCommand("insertText", false, "()");
        restoreCursorPosition(codeSection, cursorPos + 1);
    }
    if (event.key === '[') {
        event.preventDefault();
        document.execCommand("insertText", false, "[]");
        restoreCursorPosition(codeSection, cursorPos + 1);
    }

    if (event.key === 'Enter') {
        // codeSection.scrollTop = codeSection.scrollHeight;
        event.preventDefault();
        const openBraces = countBraces(codeSection.innerText.substring(0, cursorPos));
        if (nextChar === '}') {
            // alert(1);
            const spaces1 = ' '.repeat(openBraces * 4);
            const spaces2 = ' '.repeat(((openBraces == 0 ? 0 : openBraces - 1)) * 4);
            document.execCommand('insertHTML', false, '<br>' + spaces1 + '<br>' + spaces2);
            highlightKeywords();
            restoreCursorPosition(codeSection, cursorPos + 1 + openBraces * 4);
        } else if (cursorPos === 0 || prevChar === '\n' || codeSection.innerText.length === cursorPos) {
            // alert(2);
            event.preventDefault();
            document.execCommand('insertHTML', false, '<br>\n');
            restoreCursorPosition(codeSection, cursorPos + 1);
            return;
        }
        else {
            // alert(3);
            if (lastWord && lastSuggested && lastWordd) {
                lastWord = lastWordd.replace(lastSuggested, '');
                codeSection.innerText = codeSection.innerText.substring(0, cursorPos) + codeSection.innerText.substring(cursorPos).replace(lastSuggested, '');
                highlightKeywords();
                restoreCursorPosition(codeSection, cursorPos);
            }
            lastSuggested = null;
            lastWord = null;
            lastWordd = null;
            const spaces = ' '.repeat(openBraces * 4);
            document.execCommand('insertHTML', false, '<br>' + spaces);
            highlightKeywords();
            restoreCursorPosition(codeSection, cursorPos + spaces.length + 1);
        }
    }
}

function countBraces(string) {
    let count = 0;

    for (const char of string) {
        if (char === '{')
            count++;
        if (char === '}')
            count--;
    }
    return count >= 0 ? count : 0;
}
