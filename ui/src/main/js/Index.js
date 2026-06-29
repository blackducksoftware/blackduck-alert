import React from 'react';
import { createRoot } from 'react-dom/client';
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

const container = document.getElementById('react');
const root = createRoot(container);
root.render(
    <Provider store={store}>
        <ThemeProvider theme={theme}>
            <App />
        </ThemeProvider>
    </Provider>
);
