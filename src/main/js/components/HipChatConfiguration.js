import React from 'react';

class HipChatConfiguration extends React.Component {
    render() {
        return (
            <div>
                <h1>HipChat Configuration</h1>
                <label for="apiKey">Api Key</label>
                <input type="text" name="apiKey"></input><br />
                
                <label for="roomId">Room Id</label>
                <input type="number" name="roomId"></input><br />
                
                <label for="notify">Notify</label>
                <input type="checkbox" name="notify"></input><br />
                
                <label for="color">Color</label>
                <input type="text" name="color"></input><br />
            </div>
        )
    }
}

export default HipChatConfiguration;
