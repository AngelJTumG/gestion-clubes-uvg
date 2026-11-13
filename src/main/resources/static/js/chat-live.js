(function () {
    const messages = document.getElementById('chat-messages');
    const form = document.getElementById('chat-compose');
    if (!messages || !form) return;

    const usuarioId = messages.dataset.usuarioId;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    let loading = false;

    function nearBottom() {
        return messages.scrollHeight - messages.scrollTop - messages.clientHeight < 60;
    }

    function render(items) {
        const keepAtBottom = nearBottom();
        const known = new Set(
            Array.from(messages.querySelectorAll('[data-mensaje-id]'))
                .map(item => item.dataset.mensajeId)
        );

        items.forEach(item => {
            if (known.has(String(item.id))) return;
            const article = document.createElement('article');
            article.dataset.mensajeId = item.id;
            article.className = item.mio ? 'chat-message is-mine' : 'chat-message';

            const content = document.createElement('p');
            content.textContent = item.contenido;
            const time = document.createElement('time');
            time.textContent = item.enviadoEn;
            article.append(content, time);
            messages.appendChild(article);
        });

        const empty = messages.querySelector('.chat-empty');
        if (items.length && empty) empty.remove();
        if (keepAtBottom) messages.scrollTop = messages.scrollHeight;
    }

    async function refresh() {
        if (loading) return;
        loading = true;
        try {
            const response = await fetch('/api/chat/' + usuarioId + '/mensajes', {
                headers: { Accept: 'application/json' },
                cache: 'no-store'
            });
            if (response.ok) render(await response.json());
        } finally {
            loading = false;
        }
    }

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        const input = form.querySelector('input[name="contenido"]');
        const button = form.querySelector('button');
        const contenido = input.value.trim();
        if (!contenido || loading) return;

        button.disabled = true;
        const headers = { Accept: 'application/json' };
        if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

        try {
            const response = await fetch('/api/chat/' + usuarioId + '/mensajes', {
                method: 'POST',
                headers,
                body: new URLSearchParams({ contenido })
            });
            if (!response.ok) throw new Error('No se pudo enviar el mensaje');
            input.value = '';
            await refresh();
            messages.scrollTop = messages.scrollHeight;
        } catch (error) {
            window.alert(error.message);
        } finally {
            button.disabled = false;
            input.focus();
        }
    });

    messages.scrollTop = messages.scrollHeight;
    window.setInterval(refresh, 2500);
})();
