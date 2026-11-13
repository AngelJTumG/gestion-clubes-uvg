(function () {
    function createModal() {
        const modal = document.createElement('div');
        modal.className = 'confirm-modal';
        modal.setAttribute('role', 'dialog');
        modal.setAttribute('aria-modal', 'true');
        modal.hidden = true;
        modal.innerHTML =
            '<div class="confirm-modal-backdrop"></div>' +
            '<section class="confirm-modal-card" aria-labelledby="confirm-modal-title">' +
            '<span class="confirm-modal-icon" aria-hidden="true">!</span>' +
            '<h2 id="confirm-modal-title">Confirmar acción</h2>' +
            '<p class="confirm-modal-message"></p>' +
            '<div class="confirm-modal-actions">' +
            '<button type="button" class="confirm-modal-cancel">Cancelar</button>' +
            '<button type="button" class="confirm-modal-submit">Salir del club</button>' +
            '</div></section>';
        document.body.appendChild(modal);
        return modal;
    }

    document.addEventListener('submit', function (event) {
        const form = event.target.closest('form[data-confirm-message]');
        if (!form || form.dataset.confirmed === 'true') return;

        event.preventDefault();
        const modal = createModal();
        modal.querySelector('.confirm-modal-message').textContent =
            form.dataset.confirmMessage;
        modal.querySelector('.confirm-modal-submit').textContent =
            form.dataset.confirmAction || 'Confirmar';
        modal.hidden = false;
        modal.querySelector('.confirm-modal-cancel').focus();

        function close() {
            modal.hidden = true;
            modal.remove();
        }

        modal.querySelector('.confirm-modal-cancel').onclick = close;
        modal.querySelector('.confirm-modal-backdrop').onclick = close;
        modal.querySelector('.confirm-modal-submit').onclick = function () {
            form.dataset.confirmed = 'true';
            close();
            form.requestSubmit();
        };
        modal.onkeydown = function (keyEvent) {
            if (keyEvent.key === 'Escape') close();
        };
    });
})();
