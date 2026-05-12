document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-optional-block]').forEach(block => {
        const toggle = block.querySelector('[data-section-toggle]');
        const body = block.querySelector('[data-section-body]');
        if (!toggle || !body) return;
        const refresh = () => block.classList.toggle('is-disabled', !toggle.checked);
        toggle.addEventListener('change', refresh);
        refresh();
    });

    const avatarInput = document.querySelector('#avatarInput');
    const avatarPreview = document.querySelector('#avatarPreview');
    const previewBox = document.querySelector('.avatar-preview');
    if (avatarInput && previewBox) {
        avatarInput.addEventListener('change', () => {
            const file = avatarInput.files && avatarInput.files[0];
            if (!file) return;
            const url = URL.createObjectURL(file);
            let img = avatarPreview;
            if (!img) {
                img = document.createElement('img');
                img.id = 'avatarPreview';
                img.alt = '头像预览';
                previewBox.innerHTML = '';
                previewBox.appendChild(img);
            }
            img.src = url;
            previewBox.classList.remove('empty');
        });
    }
});
