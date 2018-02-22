import {
    SESSION_INITIALIZING,
    SESSION_LOGGING_IN,
    SESSION_LOGGED_IN,
    SESSION_LOGGED_OUT,
    SESSION_LOGIN_ERROR,
    SESSION_SHOW_ADVANCED,
    SESSION_HIDE_ADVANCED
} from './types';

/**
 * Triggers Logging In Reducer
 * @returns {{type}}
 */
function loggingIn() {
    return {
        type: SESSION_LOGGING_IN
    };
}

function initializing() {
    return {
        type: SESSION_INITIALIZING
    };
}

/**
 * Triggers Logged In Reducer
 * @returns {{type}}
 */
function loggedIn() {
    return {
        type: SESSION_LOGGED_IN
    };
}

/**
 * Triggers Logged Out Reducer
 * @returns {{type}}
 */
function loggedOut() {
    return {
        type: SESSION_LOGGED_OUT
    };
}

function loginError(errorMessage, errors) {
    return {
        type: SESSION_LOGIN_ERROR,
        errorMessage,
        errors: errors || []
    };
}

export function toggleAdvancedOptions(toggle) {
    if(toggle) {
        return { type: SESSION_SHOW_ADVANCED };
    } else {
        return { type: SESSION_HIDE_ADVANCED };
    }
}

export function verifyLogin() {
    return (dispatch) => {
        dispatch(initializing());
        fetch('/api/verify', {
            credentials: 'include'
        }).then(function(response) {
            if (!response.ok) {
                dispatch(loggedOut());
            } else {
                dispatch(loggedIn());
            }
        }).catch(function(error) {
            // TODO: Dispatch Error
            console.log(error);
        });
    };
}

export function login(url, username, password) {
    return (dispatch) => {
        dispatch(loggingIn());

        const body = {
            hubUsername: username,
            hubPassword: password,
            hubUrl: url,
            hubTimeout: 60,
            hubAlwaysTrustCertificate: true,
            hubApiKeyIsSet: false,
            hubProxyHost: '',
            hubProxyPasswordIsSet: false,
            hubProxyPort: '',
            hubProxyUsername: ''
        };

        fetch('/api/login', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        }).then(function(response) {
            if (response.ok) {
                dispatch(loggedIn());
            } else {
                // Need to parse out field errors
                dispatch(loginError(response.statusText, []));

                /*

                console.log('response', response.statusText);
                //response
                debugger;
                return response.json().then(json => {
                    console.log('json.message', json);
                    var message = json.message;

                    debugger;

                    var fieldErrors = {};
                    var showAdvanced = self.state.advancedShown;
                    try {
                        var jsonArray = JSON.parse(message);
                        let responseErrors = jsonArray['errors'];
                        if (responseErrors) {
                            for (var key in responseErrors) {
                                if (responseErrors.hasOwnProperty(key)) {
                                    let name = key.concat('Error');
                                    let value = responseErrors[key];
                                    fieldErrors[name] = value;
                                }
                            }
                        }
                        if (fieldErrors.hubTimeoutError || fieldErrors.hubAlwaysTrustCertificateError  || fieldErrors.hubProxyHostError  || fieldErrors.hubProxyPortError || fieldErrors.hubProxyUsernameError) {
                            showAdvanced = true;
                        }
                        message = jsonArray['message']
                    } catch (e) {
                        console.error(e);
                    }

                    self.setState({
                        advancedShown : showAdvanced,
                        errors: fieldErrors,
                        configurationMessage: message
                    });
                });
                */
            }
        })
        .catch((error) => {
            dispatch(loginError(error.message));
            console.log(error);
        });
    }
};