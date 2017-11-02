'use strict';

const React = require('react');
const ReactDOM = require('react-dom');

class App extends React.Component {
    render() {
        return (
            <div>
                <h1>Hub Alert Global Config</h1>
                <input type="text" name="hubUrl"></input><br />
                <input type="text" name="hubUsername"></input><br />
                <input type="password" name="hubPassword"></input><br />
                <input type="number" name="hubTimeout"></input><br />
                <input type="checkbox" name="hubAlwaysTrustCertificate"></input><br />
                
                <input type="text" name="hubProxyHost"></input><br />
                <input type="number" name="hubProxyPort"></input><br />
                <input type="text" name="hubProxyUsername"></input><br />
                <input type="text" name="hubProxyPassword"></input><br />
                
                <input type="text" name="accumulatorCron"></input><br />
                <input type="text" name="dailyDigestCron"></input><br /> 
            </div>
        )
    }
}

ReactDOM.render(
    <App />, document.getElementById('react')
);