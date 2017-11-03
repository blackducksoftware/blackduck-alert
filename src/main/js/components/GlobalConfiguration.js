import React from 'react';

class GlobalConfiguration extends React.Component {
	constructor(props) {
        super(props);
        this.state = {
            id: '',
            hubUrl: '',
            hubUsername: '',
            hubPassword: '',
            hubTimeout: 0,
            hubAlwaysTrustCertificate: false,
            hubProxyHost: '',
            hubProxyPort: 0,
            hubProxyUsername: '',
            hubProxyPassword: '',
            accumulatorCron: '',
            dailyDigestCron: '',
            
            configurationMessage: '',
            hubUrlError: '',
            hubUsernameError: '',
            hubTimeoutError: '',
            hubAlwaysTrustCertificateError: '',
            hubProxyHostError: '',
            hubProxyPortError: '',
            hubProxyUsernameError: '',
            accumulatorCronError: '',
            dailyDigestCronError: ''
        };
        this.handleChange = this.handleChange.bind(this);
    }
	
	componentDidMount() {
        var self = this;
        fetch('/configuration/global')  
        .then(function(response) {
            if (!response.ok) {
                return response.json().then(json => {
                     self.setState({
                        configurationMessage: json.message
                     });
                });
            } else {
                return response.json();
            }
        }).then(function(body) {
            if(body != null && body.length > 0){
                var globalConfiguration = body[0];
                self.setState({
                    id: globalConfiguration.id,
                    hubUrl: globalConfiguration.hubUrl,
                    hubUsername: globalConfiguration.hubUsername,
                    hubPassword: globalConfiguration.hubPassword,
                    hubTimeout: globalConfiguration.hubTimeout,
                    hubAlwaysTrustCertificate: globalConfiguration.hubAlwaysTrustCertificate,
                    hubProxyHost: globalConfiguration.hubProxyHost,
                    hubProxyPort: globalConfiguration.hubProxyPort,
                    hubProxyUsername: globalConfiguration.hubProxyUsername,
                    hubProxyPassword: globalConfiguration.hubProxyPassword,
                    accumulatorCron: globalConfiguration.accumulatorCron,
                    dailyDigestCron: globalConfiguration.dailyDigestCron
                });
            }
        });
	}
	
	handleSubmit(event) {
        event.preventDefault();
        var self = this;
        let jsonBody = JSON.stringify(this.state);
        var method = 'POST';
        if (this.state.id){
             method = 'PUT';
        }
        fetch('/configuration/global', {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: jsonBody
        }).then(function(response) {
            return response.json().then(json => {
                 self.setState({
                    configurationMessage: json.message
                 });
            });
        });
    }
	
	handleTestSubmit(event){
		event.preventDefault();
		var self = this;
        let jsonBody = JSON.stringify(this.state);
        fetch('/configuration/global/test', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: jsonBody
        }).then(function(response) {
            return response.json().then(json => {
                 self.setState({
                    configurationMessage: json.message
                 });
            });
        });
	}
	
	handleChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.name;

        this.setState({
            [name]: value
        });
    }
	
    render() {
        return (
            <div>
                <h1>Global Configuration</h1>
                <form onSubmit={this.handleSubmit.bind(this)}>
	                <h2>Hub Configuration</h2>
	                <label>Url</label>
	                <input type="text" name="hubUrl" value={this.state.hubUrl} onChange={this.handleChange}></input><br />
	                <div name="hubUrlError" value={this.state.hubUrlError}></div>
	                
	                <label>Username</label>
	                <input type="text" name="hubUsername" value={this.state.hubUsername} onChange={this.handleChange}></input><br />
	                <div name="hubUsernameError" value={this.state.hubUsernameError}></div>
	                
	                <label>Password</label>
	                <input type="password" name="hubPassword" value={this.state.hubPassword} onChange={this.handleChange}></input><br />
	                
	                <label>Timeout</label>
	                <input type="number" name="hubTimeout" value={this.state.hubTimeout} onChange={this.handleChange}></input><br />
	                <div name="hubTimeoutError" value={this.state.hubTimeoutError}></div>
	                
	                <label>Trust Https Certificates</label>
	                <input type="checkbox" name="hubAlwaysTrustCertificate" checked={this.state.hubAlwaysTrustCertificate} onChange={this.handleChange}></input><br />
	                <div name="hubAlwaysTrustCertificateError" value={this.state.hubAlwaysTrustCertificateError}></div>
	                
	                <h2>Proxy Configuration</h2>
	                <label>Host Name</label>
	                <input type="text" name="hubProxyHost" value={this.state.hubProxyHost} onChange={this.handleChange}></input><br />
	                <div name="hubProxyHostError" value={this.state.hubProxyHostError}></div>
	                
	                <label>Port</label>
	                <input type="number" name="hubProxyPort" value={this.state.hubProxyPort} onChange={this.handleChange}></input><br />
	                <div name="hubProxyPortError" value={this.state.hubProxyPortError}></div>
	                
	                <label>Username</label>
	                <input type="text" name="hubProxyUsername" value={this.state.hubProxyUsername} onChange={this.handleChange}></input><br />
	                <div name="hubProxyUsernameError" value={this.state.hubProxyUsernameError}></div>
	                
	                <label>Password</label>
	                <input type="password" name="hubProxyPassword" value={this.state.hubProxyPassword} onChange={this.handleChange}></input><br />
	                
	                <h2>Scheduling Configuration</h2>
	                <label>Accumulator Cron</label>
	                <input type="text" name="accumulatorCron" value={this.state.accumulatorCron} onChange={this.handleChange}></input><br />
	                <div name="accumulatorCronError" value={this.state.accumulatorCronError}></div>
	                
	                <label>Daily Digest Cron</label>
	                <input type="text" name="dailyDigestCron" value={this.state.dailyDigestCron} onChange={this.handleChange}></input><br /> 
	                <div name="dailyDigestCronError" value={this.state.dailyDigestCronError}></div>
	                
	                <input type="submit" value="Save"></input>
	                <input type="button" value="Test" onClick={this.handleTestSubmit.bind(this)}></input>
	                <p name="configurationMessage">{this.state.configurationMessage}</p>
	           </form>
            </div>
        )
    }
}

export default GlobalConfiguration;
