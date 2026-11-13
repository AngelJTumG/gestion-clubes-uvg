(function () {
    const endpoint = '/api/notificaciones/no-leidas';
    let previous = null;

    function updateBadge(selector, value) {
        document.querySelectorAll(selector).forEach(badge => {
            badge.textContent = value;
            badge.hidden = value < 1;
        });
    }

    function showToast() {
        let toast = document.querySelector('.live-notification-toast');
        if (!toast) {
            toast = document.createElement('a');
            toast.className = 'live-notification-toast';
            toast.href = '/notificaciones';
            toast.textContent = 'Tienes mensajes nuevos';
            document.body.appendChild(toast);
        }
        toast.classList.add('is-visible');
        window.setTimeout(() => toast.classList.remove('is-visible'), 4500);
    }

    async function refresh() {
        try {
            const response = await fetch(endpoint, { headers: { Accept: 'application/json' }, cache: 'no-store' });
            if (!response.ok) return;
            const data = await response.json();
            const total = Number(data.mensajes) || 0;
            const solicitudes = Number(data.solicitudesAmistad) || 0;
            updateBadge('[data-chat-badge]', total);
            updateBadge('[data-notification-badge]', total + solicitudes);
            if (previous !== null && total + solicitudes > previous) showToast();
            previous = total + solicitudes;
        } catch (_) {
            // La navegación continúa aunque una consulta periódica falle.
        }
    }

    refresh();
    window.setInterval(refresh, 3000);
})();
