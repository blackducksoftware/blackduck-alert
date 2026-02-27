class HttpInterceptor {
    static AUTH_ENDPOINTS = [
        '/alert/api/login',
        '/alert/api/verify',
        '/alert/api/csrf',
        '/alert/api/verify/saml',
        '/alert/api/logout'
    ];

    static isAuthEndpoint(url) {
        return this.AUTH_ENDPOINTS.some(endpoint => url.includes(endpoint));
    }

    static isAlertApiCall(url) {
        return url && url.includes('/alert/api/');
    }

    static shouldHandleUnauthorized(response, url) {
        return (response.status === 401 || response.status === 0) && 
               this.isAlertApiCall(url) &&
               !this.isAuthEndpoint(url);
    }

    /**
     * 
     * @param {*} store - Redux store to dispatch unauthorized action
     * Sets up a global fetch interceptor to handle 401 & 0 Unauthorized responses.
     * If a 401 or 0 response is detected from a protected endpoint, it dispatches
     * the unauthorized action to log out the user.
     */
    static setupGlobalInterceptor(store) {
        const { unauthorized } = require('../../store/actions/session');

        // Necessary to capture the original fetch coming from the browser since below this
        // we are overwriting window.fetch IF there is a 401 or 0 response status code.. else we are just 
        // returning the original request/response unmodified. Without this, we would get
        // an infinite loop.
        const originalFetch = window.fetch;

        // This window.fetch acts as a wrapper around the original fetch
        window.fetch = async (...args) => {
            try {
                // Wait for the original fetch to complete
                const response = await originalFetch(...args);
                const url = typeof args[0] === 'string' ? args[0] : args[0].url;

                if (this.shouldHandleUnauthorized(response, url)) {
                    console.log('System Message: Unauthorized access detected, logging out user.');
                    store.dispatch(unauthorized());
                }

                return response;
            } catch (error) {
                console.log('Error', error);
                // Don't interfere with network errors
                throw error;
            }
        };
    }
}

export default HttpInterceptor;