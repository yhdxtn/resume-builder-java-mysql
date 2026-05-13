document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('#resumeForm');
    const liveFrame = document.querySelector('#livePreviewFrame');
    const previewStatus = document.querySelector('#previewStatus');
    let previewTimer = null;
    let previewAbort = null;

    function setPreviewStatus(text, state) {
        if (!previewStatus) return;
        previewStatus.textContent = text;
        previewStatus.classList.toggle('is-loading', state === 'loading');
        previewStatus.classList.toggle('is-error', state === 'error');
    }

    function schedulePreview() {
        if (!form || !liveFrame) return;
        clearTimeout(previewTimer);
        setPreviewStatus('正在同步…', 'loading');
        previewTimer = setTimeout(updateLivePreview, 360);
    }

    function updateLivePreview() {
        if (!form || !liveFrame) return;
        const url = form.dataset.livePreviewUrl;
        if (!url) return;
        if (previewAbort) previewAbort.abort();
        previewAbort = new AbortController();

        const fd = new FormData(form);
        // 实时预览不反复上传头像文件；选择头像时会先写入压缩后的 data URI，用于右侧立即显示。
        fd.delete('avatarFile');

        fetch(url, {
            method: 'POST',
            body: fd,
            credentials: 'same-origin',
            signal: previewAbort.signal,
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
            .then(res => {
                if (!res.ok) throw new Error('preview failed');
                return res.text();
            })
            .then(html => {
                liveFrame.srcdoc = html;
                setPreviewStatus('已同步', 'ok');
            })
            .catch(err => {
                if (err.name === 'AbortError') return;
                setPreviewStatus('预览失败', 'error');
            });
    }

    document.querySelectorAll('[data-optional-block]').forEach(block => {
        const toggle = block.querySelector('[data-section-toggle]');
        const body = block.querySelector('[data-section-body]');
        if (!toggle || !body) return;
        const refresh = () => block.classList.toggle('is-disabled', !toggle.checked);
        toggle.addEventListener('change', () => {
            refresh();
            schedulePreview();
        });
        refresh();
    });

    if (form) {
        form.querySelectorAll('input, textarea, select').forEach(el => {
            const eventName = (el.type === 'radio' || el.type === 'checkbox' || el.tagName === 'SELECT') ? 'change' : 'input';
            el.addEventListener(eventName, schedulePreview);
        });
    }

    const avatarInput = document.querySelector('#avatarInput');
    let avatarPreview = document.querySelector('#avatarPreview');
    const previewBox = document.querySelector('.avatar-preview');
    const previewAvatarDataUri = document.querySelector('#previewAvatarDataUri');

    function putAvatarIntoPreview(dataUrl) {
        if (!previewBox || !dataUrl) return;
        let img = avatarPreview;
        if (!img) {
            img = document.createElement('img');
            img.id = 'avatarPreview';
            img.alt = '头像预览';
            previewBox.innerHTML = '';
            previewBox.appendChild(img);
            avatarPreview = img;
        }
        img.src = dataUrl;
        previewBox.classList.remove('empty');
    }


    function resizeImageToDataUrl(file, callback, onError) {
        const reader = new FileReader();
        reader.onload = () => {
            const img = new Image();
            img.onload = () => {
                try {
                    const maxSide = 420;
                    const ratio = Math.min(1, maxSide / Math.max(img.width, img.height));
                    const width = Math.max(1, Math.round(img.width * ratio));
                    const height = Math.max(1, Math.round(img.height * ratio));
                    const canvas = document.createElement('canvas');
                    canvas.width = width;
                    canvas.height = height;
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(img, 0, 0, width, height);
                    callback(canvas.toDataURL('image/jpeg', 0.86));
                } catch (e) {
                    callback(typeof reader.result === 'string' ? reader.result : '');
                }
            };
            img.onerror = () => callback(typeof reader.result === 'string' ? reader.result : '');
            img.src = typeof reader.result === 'string' ? reader.result : '';
        };
        reader.onerror = onError;
        reader.readAsDataURL(file);
    }

    if (avatarInput && previewBox) {
        avatarInput.addEventListener('change', () => {
            const file = avatarInput.files && avatarInput.files[0];
            if (!file) return;
            if (!file.type || !file.type.startsWith('image/')) {
                setPreviewStatus('请选择图片文件', 'error');
                return;
            }

            const localUrl = URL.createObjectURL(file);
            putAvatarIntoPreview(localUrl);
            setPreviewStatus('正在读取头像…', 'loading');

            resizeImageToDataUrl(file, (dataUrl) => {
                if (previewAvatarDataUri) {
                    previewAvatarDataUri.value = dataUrl;
                }
                putAvatarIntoPreview(dataUrl || localUrl);
                schedulePreview();
            }, () => {
                setPreviewStatus('头像读取失败', 'error');
            });
        });
    }
});
