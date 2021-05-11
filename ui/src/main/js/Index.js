import React from 'react';
import ReactDOM from 'react-dom';
import { applyMiddleware, createStore } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';
import createHistory from 'history/createBrowserHistory';
import App from 'application/App';
import { ConnectedRouter, routerMiddleware } from 'connected-react-router';
import rootReducer from 'store/reducers';

// export synopsys_black from '../img/synopsys_black.png';
// export synopsys_purple from '../img/synopsys_purple.png';
// export synopsys_white from '../img/synopsys_white.png';

const initialState = {};
// Setup history
const history = createHistory();

// Configure store with redux, thunk and history
const store = createStore(rootReducer(history), initialState, applyMiddleware(thunk, routerMiddleware(history)));

ReactDOM.render(
    <Provider store={store}>
        <ConnectedRouter history={history}>
            <App />
        </ConnectedRouter>
    </Provider>,
    document.getElementById('react')
);
