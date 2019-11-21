import React, { Component } from 'react';
import TextInput from 'field/input/TextInput';

class RoleConfiguration extends Component {
    constructor(props) {
        super(props);
        
    }


    render() {
        return (
            <div>
                <TextInput label="Role Name" description="The name of the role." />
            </div>
        );
    }
}

RoleConfiguration.propTypes = {};

export default RoleConfiguration;
