(function () {
    const panel = document.querySelector('.panel-view');
    if (!panel) return;

    let previousIds = new Set();
    let initialized = false;

    function showRequest(request) {
        let toast = document.querySelector('.live-admin-request-toast');
        if (!toast) {
            toast = document.createElement('a');
            toast.className = 'live-admin-request-toast';
            toast.href = '#solicitudes';
            document.body.appendChild(toast);
        }
        toast.innerHTML =
            '<strong>Nueva solicitud de ingreso</strong>' +
            '<span><b></b> solicita entrar a <b></b></span>' +
            '<small></small>';
        const bold = toast.querySelectorAll('b');
        bold[0].textContent = request.usuario;
        bold[1].textContent = request.club;
        toast.querySelector('small').textContent = request.creadaEn;
        toast.classList.add('is-visible');
        window.setTimeout(() => toast.classList.remove('is-visible'), 7000);
    }

    async function refresh() {
        try {
            const response = await fetch('/api/admin/solicitudes/pendientes', {
                headers: { Accept: 'application/json' },
                cache: 'no-store'
            });
            if (!response.ok) return;
            const requests = await response.json();
            const ids = new Set(requests.map(request => String(request.id)));
            if (initialized) {
                const newRequest = requests.find(request => !previousIds.has(String(request.id)));
                if (newRequest) showRequest(newRequest);
            }
            previousIds = ids;
            initialized = true;
        } catch (_) {
            // Una comprobación fallida no debe interrumpir el panel.
        }
    }

    refresh();
    window.setInterval(refresh, 3000);
})();
