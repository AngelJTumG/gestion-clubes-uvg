document.querySelectorAll('[data-add-day]').forEach(function (button) {
    button.addEventListener('click', function () {
        const group = document.getElementById(button.dataset.addDay);
        const firstRow = group.querySelector('.day-row');
        const newRow = firstRow.cloneNode(true);
        const removeButton = document.createElement('button');

        newRow.querySelectorAll('select, input').forEach(function (field) {
            field.value = '';
        });
        removeButton.type = 'button';
        removeButton.className = 'remove-day-button';
        removeButton.textContent = 'Quitar';
        removeButton.addEventListener('click', function () {
            newRow.remove();
        });
        newRow.append(removeButton);
        group.insertBefore(newRow, button);
    });
});
