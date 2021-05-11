class HeaderUtilities {
    constructor() {
        this.headers = new Headers();
    }

    addAccept() {
        this.headers.append('Accept', 'application/json');
    }

    addApplicationJsonContentType() {
        this.headers.append('Content-Type', 'application/json');
    }

    addXCsrfToken(csrfToken) {
        this.headers.append('X-CSRF-TOKEN', csrfToken);
    }

    addDefaultHeaders(csrfToken) {
        this.addAccept();
        this.addApplicationJsonContentType();
        this.addXCsrfToken(csrfToken);
    }

    getHeaders() {
        return this.headers;
    }
}

export default HeaderUtilities;
