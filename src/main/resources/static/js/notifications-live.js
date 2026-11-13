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
            const solicitudesClub = Number(data.solicitudesClub) || 0;
            updateBadge('[data-chat-badge]', total);
            const notificationTotal = total + solicitudes + solicitudesClub;
            updateBadge('[data-notification-badge]', notificationTotal);
            if (previous !== null && notificationTotal > previous) showToast();
            previous = notificationTotal;
        } catch (_) {
            // La navegación continúa aunque una consulta periódica falle.
        }
    }

    refresh();
    window.setInterval(refresh, 3000);
})();
