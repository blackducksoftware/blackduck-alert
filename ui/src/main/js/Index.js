import React from 'react';
import ReactDOM from 'react-dom';
import { applyMiddleware, createStore } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';
import { createBrowserHistory } from 'history';
import App from 'application/App';
import { ConnectedRouter, routerMiddleware } from 'connected-react-router';
import rootReducer from 'store/reducers';
import { ThemeProvider } from 'react-jss';
import theme from '_theme';
import { dom } from '@fortawesome/fontawesome-svg-core';

const initialState = {};
// Setup history
const history = createBrowserHistory();

// Configure store with redux, thunk and history
const store = createStore(rootReducer(history), initialState, applyMiddleware(thunk, routerMiddleware(history)));

dom.watch({
    autoReplaceSvgRoot: document
});

ReactDOM.render(
    <Provider store={store}>
        <ConnectedRouter history={history}>
            <ThemeProvider theme={theme}>
                <App />
            </ThemeProvider>
        </ConnectedRouter>
    </Provider>,
    document.getElementById('react')
);
