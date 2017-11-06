import React from 'react';

class HipChatConfiguration extends React.Component {
    render() {
        return (
            <div>
                <h1>HipChat Configuration</h1>
                <label>Api Key</label>
                <input type="text" name="apiKey"></input><br />
                
                <label>Room Id</label>
                <input type="number" name="roomId"></input><br />
                
                <label>Notify</label>
                <input type="checkbox" name="notify"></input><br />
                
                <label>Color</label>
                <input type="text" name="color"></input><br />
            </div>
        )
    }
}

export default HipChatConfiguration;
