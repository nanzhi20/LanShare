const form = document.getElementById('upload-form');
const fileInput = document.getElementById('file-input');
const uploadButton = document.getElementById('upload-button');
const message = document.getElementById('message');
const fileList = document.getElementById('file-list');
const empty = document.getElementById('empty');
const addressList = document.getElementById('address-list');
const addressMessage = document.getElementById('address-message');
const textForm = document.getElementById('text-form');
const textInput = document.getElementById('text-input');
const textSubmitButton = document.getElementById('text-submit-button');
const textMessage = document.getElementById('text-message');
const textList = document.getElementById('text-list');
const textEmpty = document.getElementById('text-empty');
const textCount = document.getElementById('text-count');
let maxFileSizeBytes = null;

function showMessage(text, kind = '') {
  message.textContent = text;
  message.className = kind;
}

async function errorMessage(response) {
  try {
    const body = await response.json();
    return body.error || `请求失败（${response.status}）`;
  } catch (_) {
    return `请求失败（${response.status}）`;
  }
}

function formatSize(bytes) {
  return bytes < 1024 * 1024
    ? `${Math.ceil(bytes / 1024)} KB`
    : `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

async function loadConfig() {
  try {
    const response = await fetch('/api/files/config');
    if (!response.ok) throw new Error(await errorMessage(response));
    const config = await response.json();
    maxFileSizeBytes = config.maxFileSizeBytes;
    document.getElementById('max-file-size').textContent = formatSize(maxFileSizeBytes);
    document.getElementById('storage-path').textContent = config.storageDirectory;
  } catch (_) {
    document.getElementById('max-file-size').textContent = '服务端设定的大小';
    document.getElementById('storage-path').textContent = '暂时无法读取';
  }
}

async function showLanAddresses() {
  try {
    const response = await fetch('/api/network/addresses');
    if (!response.ok) throw new Error('无法读取地址');
    const addresses = await response.json();
    addressList.replaceChildren();
    const port = window.location.port || '80';
    for (const address of addresses) {
      const row = document.createElement('li');
      const link = document.createElement('a');
      link.href = `http://${address.ip}:${port}/`;
      link.textContent = link.href;
      const label = document.createElement('div');
      label.className = 'file-meta';
      label.textContent = address.interfaceName;
      row.append(link, label);
      addressList.append(row);
    }
    addressMessage.textContent = addresses.length
      ? '这些是电脑当前的局域网地址；虚拟网卡地址可能无法从手机访问。'
      : '未找到可用的局域网 IPv4 地址，请确认电脑已连接 Wi-Fi 或有线网络。';
  } catch (_) {
    addressMessage.textContent = '读取地址失败，请检查电脑的网络连接。';
  }
}

function renderFile(file) {
  const row = document.createElement('li');
  const info = document.createElement('div');
  info.className = 'file-info';

  const name = document.createElement('div');
  name.className = 'file-name';
  name.textContent = file.name;
  const meta = document.createElement('div');
  meta.className = 'file-meta';
  meta.textContent = `${formatSize(file.size)} · ${new Date(file.uploadedAt).toLocaleString()}`;
  info.append(name, meta);

  const actions = document.createElement('div');
  actions.className = 'actions';
  const download = document.createElement('a');
  download.className = 'download';
  download.textContent = '下载';
  download.href = `/api/files/${encodeURIComponent(file.id)}/download`;
  const remove = document.createElement('button');
  remove.className = 'danger';
  remove.type = 'button';
  remove.textContent = '删除';
  remove.addEventListener('click', () => deleteFile(file, remove));
  actions.append(download, remove);
  row.append(info, actions);
  return row;
}

async function refreshFiles() {
  try {
    const response = await fetch('/api/files');
    if (!response.ok) throw new Error(await errorMessage(response));
    const files = await response.json();
    fileList.replaceChildren(...files.map(renderFile));
    empty.hidden = files.length !== 0;
  } catch (error) {
    showMessage(`读取文件列表失败：${error.message}`, 'error');
  }
}

async function deleteFile(file, button) {
  if (!confirm(`删除“${file.name}”？此操作无法撤销。`)) return;
  button.disabled = true;
  try {
    const response = await fetch(`/api/files/${encodeURIComponent(file.id)}`, { method: 'DELETE' });
    if (!response.ok) throw new Error(await errorMessage(response));
    showMessage('文件已删除。', 'success');
    await refreshFiles();
  } catch (error) {
    showMessage(error.message, 'error');
    button.disabled = false;
  }
}

function showTextMessage(text, kind = '') {
  textMessage.textContent = text;
  textMessage.className = kind;
}

function renderText(item) {
  const row = document.createElement('li');
  const info = document.createElement('div');
  info.className = 'text-info';

  const content = document.createElement('pre');
  content.className = 'text-content';
  content.textContent = item.content;
  const meta = document.createElement('div');
  meta.className = 'file-meta';
  meta.textContent = new Date(item.createdAt).toLocaleString();
  info.append(content, meta);

  const actions = document.createElement('div');
  actions.className = 'actions';
  const copy = document.createElement('button');
  copy.className = 'copy';
  copy.type = 'button';
  copy.textContent = '复制';
  copy.addEventListener('click', () => copySharedText(item.content));
  const remove = document.createElement('button');
  remove.className = 'danger';
  remove.type = 'button';
  remove.textContent = '删除';
  remove.addEventListener('click', () => deleteText(item, remove));
  actions.append(copy, remove);
  row.append(info, actions);
  return row;
}

async function refreshTexts() {
  try {
    const response = await fetch('/api/texts');
    if (!response.ok) throw new Error(await errorMessage(response));
    const texts = await response.json();
    textList.replaceChildren(...texts.map(renderText));
    textEmpty.hidden = texts.length !== 0;
  } catch (error) {
    showTextMessage(`读取文字列表失败：${error.message}`, 'error');
  }
}

async function copySharedText(text) {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
    } else {
      const temporary = document.createElement('textarea');
      temporary.value = text;
      temporary.style.position = 'fixed';
      temporary.style.opacity = '0';
      document.body.append(temporary);
      temporary.focus();
      temporary.select();
      const copied = document.execCommand('copy');
      temporary.remove();
      if (!copied) throw new Error('浏览器不允许访问剪贴板');
    }
    showTextMessage('文字已复制。', 'success');
  } catch (_) {
    showTextMessage('复制失败，请手动选择文字复制。', 'error');
  }
}

async function deleteText(item, button) {
  if (!confirm('删除这条文字？此操作会对所有设备生效。')) return;
  button.disabled = true;
  try {
    const response = await fetch(`/api/texts/${encodeURIComponent(item.id)}`, { method: 'DELETE' });
    if (!response.ok) throw new Error(await errorMessage(response));
    showTextMessage('文字已删除。', 'success');
    await refreshTexts();
  } catch (error) {
    showTextMessage(error.message, 'error');
    button.disabled = false;
  }
}

textInput.addEventListener('input', () => {
  textCount.textContent = `${textInput.value.length} / 20000`;
});

textForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const content = textInput.value;
  if (!content.trim()) return showTextMessage('请输入非空文字。', 'error');

  textSubmitButton.disabled = true;
  showTextMessage('正在发送…');
  try {
    const response = await fetch('/api/texts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content })
    });
    if (!response.ok) throw new Error(await errorMessage(response));
    textInput.value = '';
    textCount.textContent = '0 / 20000';
    showTextMessage('文字已保存。', 'success');
    await refreshTexts();
  } catch (error) {
    showTextMessage(`发送失败：${error.message}`, 'error');
  } finally {
    textSubmitButton.disabled = false;
  }
});

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  const file = fileInput.files[0];
  if (!file) return;
  if (file.size === 0) return showMessage('请选择非空文件。', 'error');
  if (maxFileSizeBytes !== null && file.size > maxFileSizeBytes) {
    return showMessage(`单个文件不能超过 ${formatSize(maxFileSizeBytes)}。`, 'error');
  }

  uploadButton.disabled = true;
  showMessage('正在上传，请保持页面打开…');
  const data = new FormData();
  data.append('file', file);
  try {
    const response = await fetch('/api/files', { method: 'POST', body: data });
    if (!response.ok) throw new Error(await errorMessage(response));
    fileInput.value = '';
    showMessage('上传成功。', 'success');
    await refreshFiles();
  } catch (error) {
    showMessage(`上传失败：${error.message}`, 'error');
  } finally {
    uploadButton.disabled = false;
  }
});

document.getElementById('refresh-button').addEventListener('click', refreshFiles);
document.getElementById('text-refresh-button').addEventListener('click', refreshTexts);
loadConfig();
showLanAddresses();
refreshFiles();
refreshTexts();
