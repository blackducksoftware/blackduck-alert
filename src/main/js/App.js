'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import GlobalConfiguration from './components/GlobalConfiguration';
import EmailConfiguration from './components/EmailConfiguration';
import HipChatConfiguration from './components/HipChatConfiguration';
import SlackConfiguration from './components/SlackConfiguration';

class App extends React.Component {
    render() {
        return (
            <div>
                <GlobalConfiguration />
                <EmailConfiguration />
                <HipChatConfiguration />
                <SlackConfiguration />
            </div>
        )
    }
}

ReactDOM.render(
    <App />, document.getElementById('react')
);