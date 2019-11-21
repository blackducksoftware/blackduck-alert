import React, { Component } from 'react';
import TextInput from 'field/input/TextInput';

class UserConfiguration extends Component {
    constructor(props) {
        super(props);

    }

    render() {
        return (
            <div>
                <TextInput label="User Name" description="The name of the user." />
            </div>
        );
    }
}

UserConfiguration.propTypes = {};

export default UserConfiguration;
