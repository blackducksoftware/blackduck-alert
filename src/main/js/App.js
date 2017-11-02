'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import GlobalConfiguration from './components/GlobalConfiguration';

class App extends React.Component {
    render() {
        return (
            <div>
                <GlobalConfiguration />
            </div>
        )
    }
}

ReactDOM.render(
    <App />, document.getElementById('react')
);