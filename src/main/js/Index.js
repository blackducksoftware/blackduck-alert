import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, compose } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';
import createHistory from 'history/createBrowserHistory';
import { ConnectedRouter, routerMiddleware } from 'react-router-redux';
import reducers from './store/reducers';
import App from './App';

const initialState = {};
// Setup history
const history = createHistory();

// Configure store with redux, thunk and history
const store = createStore(reducers, initialState, applyMiddleware(thunk, routerMiddleware(history)));

ReactDOM.render(
    <Provider store={store}>
        <ConnectedRouter history={history}>
            <App />
        </ConnectedRouter>
    </Provider>,
    document.getElementById('react')
);
