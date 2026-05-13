(function () {
    const input = document.querySelector('#avatarInput');
    const hidden = document.querySelector('#defaultAvatarDataUri');
    const preview = document.querySelector('#avatarPreview');
    const previewBox = document.querySelector('.avatar-preview');
    const modal = document.querySelector('#avatarCropModal');
    const cropImage = document.querySelector('#cropImage');
    const cropArea = document.querySelector('.crop-area');
    const zoomInput = document.querySelector('#cropZoom');
    const confirmBtn = document.querySelector('#cropConfirm');
    const cancelBtn = document.querySelector('#cropCancel');

    if (!input || !hidden || !modal || !cropImage || !cropArea || !zoomInput || !confirmBtn || !cancelBtn) return;

    let img = new Image();
    let dataUrl = '';
    let baseScale = 1;
    let zoom = 1;
    let offsetX = 0;
    let offsetY = 0;
    let dragging = false;
    let startX = 0;
    let startY = 0;
    let startOffsetX = 0;
    let startOffsetY = 0;

    function openModal() {
        modal.hidden = false;
        document.body.classList.add('crop-open');
    }

    function closeModal() {
        modal.hidden = true;
        document.body.classList.remove('crop-open');
    }

    function areaSize() {
        const rect = cropArea.getBoundingClientRect();
        return Math.max(260, Math.round(Math.min(rect.width, rect.height)));
    }

    function resetState() {
        const size = areaSize();
        baseScale = Math.max(size / img.naturalWidth, size / img.naturalHeight);
        zoom = 1;
        offsetX = 0;
        offsetY = 0;
        zoomInput.value = '1';
        cropImage.style.width = (img.naturalWidth * baseScale) + 'px';
        cropImage.style.height = (img.naturalHeight * baseScale) + 'px';
        render();
    }

    function render() {
        cropImage.style.transform = `translate(-50%, -50%) translate(${offsetX}px, ${offsetY}px) scale(${zoom})`;
    }

    function getPoint(e) {
        const t = e.touches && e.touches[0];
        return { x: t ? t.clientX : e.clientX, y: t ? t.clientY : e.clientY };
    }

    function setPreviewImage(src) {
        if (!previewBox || !src) return;
        let previewImg = preview || document.querySelector('#avatarPreview');
        if (!previewImg) {
            previewImg = document.createElement('img');
            previewImg.id = 'avatarPreview';
            previewImg.alt = '头像预览';
            previewBox.innerHTML = '';
            previewBox.appendChild(previewImg);
        }
        previewImg.src = src;
        previewBox.classList.remove('empty');
    }

    input.addEventListener('change', function () {
        const file = input.files && input.files[0];
        if (!file) return;
        if (!file.type || !file.type.startsWith('image/')) {
            alert('请选择图片文件。');
            input.value = '';
            return;
        }
        const reader = new FileReader();
        reader.onload = function () {
            dataUrl = String(reader.result || '');
            img = new Image();
            img.onload = function () {
                cropImage.src = dataUrl;
                openModal();
                setTimeout(resetState, 30);
            };
            img.onerror = function () {
                alert('图片读取失败，请重新选择。');
            };
            img.src = dataUrl;
        };
        reader.readAsDataURL(file);
    });

    zoomInput.addEventListener('input', function () {
        zoom = parseFloat(zoomInput.value || '1');
        render();
    });

    cropArea.addEventListener('mousedown', startDrag);
    cropArea.addEventListener('touchstart', startDrag, { passive: false });
    window.addEventListener('mousemove', moveDrag);
    window.addEventListener('touchmove', moveDrag, { passive: false });
    window.addEventListener('mouseup', endDrag);
    window.addEventListener('touchend', endDrag);

    function startDrag(e) {
        e.preventDefault();
        dragging = true;
        const p = getPoint(e);
        startX = p.x;
        startY = p.y;
        startOffsetX = offsetX;
        startOffsetY = offsetY;
    }

    function moveDrag(e) {
        if (!dragging) return;
        e.preventDefault();
        const p = getPoint(e);
        offsetX = startOffsetX + (p.x - startX);
        offsetY = startOffsetY + (p.y - startY);
        render();
    }

    function endDrag() {
        dragging = false;
    }

    confirmBtn.addEventListener('click', function () {
        const size = areaSize();
        const out = 480;
        const canvas = document.createElement('canvas');
        canvas.width = out;
        canvas.height = out;
        const ctx = canvas.getContext('2d');
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, out, out);

        const displayW = img.naturalWidth * baseScale * zoom;
        const displayH = img.naturalHeight * baseScale * zoom;
        const ratio = out / size;
        const dx = out / 2 - (displayW * ratio) / 2 + offsetX * ratio;
        const dy = out / 2 - (displayH * ratio) / 2 + offsetY * ratio;
        ctx.drawImage(img, dx, dy, displayW * ratio, displayH * ratio);

        const cropped = canvas.toDataURL('image/jpeg', 0.88);
        hidden.value = cropped;
        setPreviewImage(cropped);
        closeModal();
    });

    cancelBtn.addEventListener('click', function () {
        input.value = '';
        closeModal();
    });
})();
