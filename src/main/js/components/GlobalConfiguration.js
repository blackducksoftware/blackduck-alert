import React from 'react';

class GlobalConfiguration extends React.Component {
    render() {
        return (
            <div>
                <h1>Global Configuration</h1>
                <h2>Hub Configuration</h2>
                <label for="hubUrl">Url</label>
                <input type="text" name="hubUrl"></input><br />
                
                <label for="hubUsername">Username</label>
                <input type="text" name="hubUsername"></input><br />
                
                <label for="hubPassword">Password</label>
                <input type="password" name="hubPassword"></input><br />
                
                <label for="hubTimeout">Timeout</label>
                <input type="number" name="hubTimeout"></input><br />
                
                <label for="hubAlwaysTrustCertificate">Trust Https Certificates</label>
                <input type="checkbox" name="hubAlwaysTrustCertificate"></input><br />
                
                <h2>Proxy Configuration</h2>
                <label for="hubProxyHost">Host Name</label>
                <input type="text" name="hubProxyHost"></input><br />
                
                <label for="hubProxyPort">Port</label>
                <input type="number" name="hubProxyPort"></input><br />
                
                <label for="hubProxyUsername">Username</label>
                <input type="text" name="hubProxyUsername"></input><br />
                
                <label for="hubProxyPassword">Password</label>
                <input type="password" name="hubProxyPassword"></input><br />
                
                <h2>Scheduling Configuration</h2>
                <label for="accumulatorCron">Accumulator Cron</label>
                <input type="text" name="accumulatorCron"></input><br />
                
                <label for="dailyDigestCron">Daily Digest Cron</label>
                <input type="text" name="dailyDigestCron"></input><br /> 
            </div>
        )
    }
}

export default GlobalConfiguration;
