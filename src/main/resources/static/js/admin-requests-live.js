(function () {
    const panel = document.querySelector('.panel-view');
    if (!panel) return;

    let previousIds = new Set();
    let initialized = false;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

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

    function renderRequests(requests, puedeAprobarTodas) {
        let section = document.querySelector('[data-admin-requests]');
        if (!requests.length) {
            if (section) section.hidden = true;
            return;
        }

        if (!section) {
            section = document.createElement('section');
            section.id = 'solicitudes';
            section.className = 'requests-content';
            section.dataset.adminRequests = '';
            section.innerHTML =
                '<p class="eyebrow">Gestión de clubes</p>' +
                '<h2>Solicitudes de ingreso</h2>' +
                '<div data-approve-all></div>' +
                '<div class="requests-list" data-admin-requests-list></div>';
            const main = document.querySelector('.panel-view main');
            if (main) main.appendChild(section);
        }

        section.hidden = false;
        const approveAll = section.querySelector('[data-approve-all]');
        if (approveAll) {
            approveAll.replaceChildren();
            if (puedeAprobarTodas) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/admin/solicitudes/aprobar-todas';
                if (csrfToken) {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = '_csrf';
                    input.value = csrfToken;
                    form.appendChild(input);
                }
                const button = document.createElement('button');
                button.type = 'submit';
                button.textContent = 'Aprobar todas las solicitudes';
                form.appendChild(button);
                approveAll.appendChild(form);
            }
        }
        const list = section.querySelector('[data-admin-requests-list]');
        if (!list) return;
        list.replaceChildren(...requests.map(request => {
            const article = document.createElement('article');
            article.className = 'request-item';
            article.innerHTML =
                '<div><strong></strong><span>Solicitud pendiente</span>' +
                '<span>Solicita ingresar a <b></b></span></div>' +
                '<div class="request-actions">' +
                '<form method="post" action="/club-admin/solicitudes/' + request.id + '/aprobar">' +
                '<button type="submit">Aprobar</button></form>' +
                '<form method="post" action="/club-admin/solicitudes/' + request.id + '/rechazar">' +
                '<button type="submit" class="danger-button">Rechazar</button></form></div>';
            article.querySelector('strong').textContent = request.usuario;
            article.querySelector('b').textContent = request.club;
            return article;
        }));
    }

    async function refresh() {
        try {
            const response = await fetch('/api/admin/solicitudes/pendientes', {
                headers: { Accept: 'application/json' },
                cache: 'no-store'
            });
            if (!response.ok) return;
            const data = await response.json();
            const requests = data.solicitudes || [];
            const ids = new Set(requests.map(request => String(request.id)));
            renderRequests(requests, data.puedeAprobarTodas);
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
    window.setInterval(refresh, 1000);
})();
