import React, { Component } from 'react';
import TextInput from 'field/input/TextInput';
import PasswordInput from 'field/input/PasswordInput';
import CheckboxInput from 'field/input/CheckboxInput';

class UserConfiguration extends Component {
    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
        this.retrieveStateValue = this.retrieveStateValue.bind(this);

        this.state = {};
    }

    handleChange(e) {
        const { name, value, type, checked } = e.target;
        const updatedValue = type === 'checkbox' ? checked.toString() : value;
        this.setState({
            [name]: updatedValue
        });
    }

    retrieveStateValue(stateKey) {
        return this.state[stateKey];
    }

    render() {
        const usernameKey = 'username';
        const passwordKey = 'password';
        const emailKey = 'email_address';
        const enabledKey = 'enabled';

        return (
            <div>
                <TextInput name={usernameKey} label="Username" description="The users username." onChange={this.handleChange} value={this.retrieveStateValue(usernameKey)} />
                <PasswordInput name={passwordKey} label="Password" description="The users password." onChange={this.handleChange} value={this.retrieveStateValue(passwordKey)} />
                <TextInput name={emailKey} label="Email" description="The users email." onChange={this.handleChange} value={this.retrieveStateValue(emailKey)} />
                <CheckboxInput name={enabledKey} label="Enabled" description="Enable this user for Alert." onChange={this.handleChange} isChecked={this.retrieveStateValue(enabledKey)} />
            </div>
        );
    }
}

UserConfiguration.propTypes = {};

export default UserConfiguration;
