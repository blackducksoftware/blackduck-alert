import React from 'react';

class SlackConfiguration extends React.Component {
    render() {
        return (
            <div>
                <h1>Slack Configuration</h1>
                <label for="channelName">Channel Name</label>
                <input type="text" name="channelName"></input><br />
                
                <label for="username">Username</label>
                <input type="text" name="username"></input><br />
                
                <label for="webhook">Webhook</label>
                <input type="text" name="webhook"></input><br />
            </div>
        )
    }
}

export default SlackConfiguration;
