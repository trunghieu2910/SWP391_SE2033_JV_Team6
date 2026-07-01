// ignore-well-known.js
// Bỏ qua lỗi .well-known cho Chrome DevTools
(function() {
    // Kiểm tra nếu là Chrome DevTools request
    if (window.navigator.userAgent.includes('Chrome')) {
        // Intercept fetch và xhr để bỏ qua .well-known
        const originalFetch = window.fetch;
        window.fetch = function(...args) {
            const url = args[0];
            if (typeof url === 'string' && url.includes('.well-known')) {
                return Promise.resolve(new Response('', { status: 200 }));
            }
            return originalFetch.apply(this, args);
        };

        // Intercept XMLHttpRequest
        const originalOpen = XMLHttpRequest.prototype.open;
        XMLHttpRequest.prototype.open = function(method, url, ...args) {
            if (typeof url === 'string' && url.includes('.well-known')) {
                // Trả về response rỗng
                this.status = 200;
                this.responseText = '';
                this.readyState = 4;
                this.onreadystatechange && this.onreadystatechange();
                return;
            }
            return originalOpen.apply(this, [method, url, ...args]);
        };
    }
})();