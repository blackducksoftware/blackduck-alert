'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import GlobalConfiguration from './component/GlobalConfiguration';
import EmailConfiguration from './component/EmailConfiguration';
import HipChatConfiguration from './component/HipChatConfiguration';
import SlackConfiguration from './component/SlackConfiguration';

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