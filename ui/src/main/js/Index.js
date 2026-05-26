import React from 'react';
import ReactDOM from 'react-dom';
import { applyMiddleware, createStore } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';
import App from 'application/App';
import rootReducer from 'store/reducers';
import { ThemeProvider } from 'react-jss';
import theme from '_theme';
import { dom } from '@fortawesome/fontawesome-svg-core';
import HttpInterceptor from 'common/util/HttpInterceptor';

const initialState = {};

// Configure store with redux and thunk
const store = createStore(rootReducer(), initialState, applyMiddleware(thunk));

// Set up HTTP interceptor for 401 handling
HttpInterceptor.setupGlobalInterceptor(store);

dom.watch({
    autoReplaceSvgRoot: document
});

ReactDOM.render(
    <Provider store={store}>
        <ThemeProvider theme={theme}>
            <App />
        </ThemeProvider>
    </Provider>,
    document.getElementById('react')
);
