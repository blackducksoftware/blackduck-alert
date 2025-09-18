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
        return response.status === 401 && 
               this.isAlertApiCall(url) && 
               !this.isAuthEndpoint(url);
    }

    static setupGlobalInterceptor(store) {
        const { unauthorized } = require('../../store/actions/session');
        const originalFetch = window.fetch;
        
        window.fetch = async (...args) => {
            try {
                const response = await originalFetch(...args);
                if (this.shouldHandleUnauthorized(response, args[0])) {
                // if (this.shouldHandleUnauthorized({status: 401, ...response}, args[0])) {
                    console.log('Unauthorized access detected, logging out user');
                    store.dispatch(unauthorized());
                }
                
                return response;
            } catch (error) {
                // Don't interfere with network errors
                throw error;
            }
        };
    }
}

export default HttpInterceptor;