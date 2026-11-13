(function () {
    const list = document.getElementById('friend-requests-list');
    const empty = document.getElementById('friend-requests-empty');
    if (!list && !empty) return;

    function render(items) {
        const count = document.getElementById('friend-requests-count');
        if (count) count.textContent = items.length;
        if (!items.length) {
            if (list) list.replaceChildren();
            if (empty) empty.hidden = false;
            return;
        }

        if (empty) empty.hidden = true;
        const target = list || document.createElement('div');
        target.id = 'friend-requests-list';
        target.className = 'community-friend-list';
        target.replaceChildren();

        items.forEach(item => {
            const article = document.createElement('article');
            article.className = 'community-friend-request';
            article.innerHTML =
                '<div class="community-friend-copy">' +
                '<h3></h3><p>Quiere conectar contigo.</p><time></time></div>' +
                '<div class="community-friend-actions">' +
                '<form method="post" action="/amistades/' + item.id + '/aprobar">' +
                '<button type="submit">Aceptar</button></form>' +
                '<form method="post" action="/amistades/' + item.id + '/rechazar">' +
                '<button type="submit" class="community-friend-reject">Rechazar</button></form>' +
                '</div>';
            article.querySelector('h3').textContent = item.nombre;
            article.querySelector('time').textContent = item.creadaEn;
            target.appendChild(article);
        });

        if (!list) document.querySelector('.community-friend-requests').appendChild(target);
    }

    async function refresh() {
        try {
            const response = await fetch('/api/amistades/pendientes', {
                headers: { Accept: 'application/json' },
                cache: 'no-store'
            });
            if (response.ok) render(await response.json());
        } catch (_) {
            // Una consulta fallida no debe interrumpir la navegación.
        }
    }

    refresh();
    window.setInterval(refresh, 3000);
})();
