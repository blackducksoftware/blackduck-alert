import React, { Component } from 'react';
import TextInput from 'field/input/TextInput';
import TableDisplay from 'field/TableDisplay';

class RoleConfiguration extends Component {
    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
        this.retrievePermissionsData = this.retrievePermissionsData.bind(this);
        this.createPermissionsColumns = this.createPermissionsColumns.bind(this);

        this.state = {};
    }

    retrievePermissionsData() {
        return [];
    }

    handleChange(e) {
        const { name, value } = e.target;
        this.setState({
            [name]: value
        });
    }

    createPermissionsColumns() {
        return [
            {
                header: 'descriptorName',
                headerLabel: 'Descriptor',
                isKey: true
            }, {
                header: 'context',
                headerLabel: 'Context',
                isKey: false
            }, {
                header: 'createOperation',
                headerLabel: 'Create',
                isKey: false
            }, {
                header: 'deleteOperation',
                headerLabel: 'Delete',
                isKey: false
            }, {
                header: 'readOperation',
                headerLabel: 'Read',
                isKey: false
            }, {
                header: 'writeOperation',
                headerLabel: 'Write',
                isKey: false
            }, {
                header: 'executeOperation',
                headerLabel: 'Execute',
                isKey: false
            }, {
                header: 'uploadFileReadOperation',
                headerLabel: 'Upload File Read',
                isKey: false
            }, {
                header: 'uploadFileWriteOperation',
                headerLabel: 'Upload File Write',
                isKey: false
            }, {
                header: 'uploadFileDeleteOperation',
                headerLabel: 'Upload File Delete',
                isKey: false
            }
        ];
    }

    render() {
        const roleNameKey = 'rolename';
        const roleNameValue = this.state[roleNameKey];

        return (
            <div>
                <TextInput name={roleNameKey} label="Role Name" description="The name of the role." onChange={this.handleChange} value={roleNameValue} />
                <TableDisplay columns={this.createPermissionsColumns()} retrieveData={this.retrievePermissionsData} deleteButton={false} newButton={false} />
            </div>
        );
    }
}

RoleConfiguration.propTypes = {};

RoleConfiguration.defaultProps = {};

export default RoleConfiguration;
