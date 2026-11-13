(function () {
    const root = document.documentElement;
    const savedTheme = localStorage.getItem('uvg-theme');
    if (savedTheme === 'dark') root.dataset.theme = 'dark';

    document.addEventListener('click', function (event) {
        const themeButton = event.target.closest('[data-theme-toggle]');
        if (themeButton) {
            const dark = root.dataset.theme !== 'dark';
            root.dataset.theme = dark ? 'dark' : 'light';
            localStorage.setItem('uvg-theme', dark ? 'dark' : 'light');
            themeButton.setAttribute('aria-pressed', String(dark));
        }
    });
})();
