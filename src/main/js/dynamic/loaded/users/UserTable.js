import React, { Component } from 'react';
import PropTypes from 'prop-types'
import TableDisplay from 'field/TableDisplay';
import TextInput from 'field/input/TextInput';
import PasswordInput from 'field/input/PasswordInput';
import CheckboxInput from 'field/input/CheckboxInput';
import { connect } from 'react-redux';
import { createNewUser, deleteUser, fetchUsers } from 'store/actions/users';
import DynamicSelectInput from 'field/input/DynamicSelect';
import { fetchRoles } from 'store/actions/roles';

class UserTable extends Component {
    constructor(props) {
        super(props);

        this.retrieveData = this.retrieveData.bind(this);
        this.createColumns = this.createColumns.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onSave = this.onSave.bind(this);
        this.createModalFields = this.createModalFields.bind(this);
        this.retrieveRoles = this.retrieveRoles.bind(this);

        this.state = {
            user: {},
            roles: []
        };
    }

    createColumns() {
        return [
            {
                header: 'username',
                headerLabel: 'Username',
                isKey: true
            },
            {
                header: 'emailAddress',
                headerLabel: 'Email',
                isKey: false
            }
        ];
    }

    retrieveData() {
        this.props.getUsers();
        return this.props.users;
    }

    handleChange(e) {
        const { name, value, type, checked } = e.target;
        const { user } = this.state;
        const updatedValue = type === 'checkbox' ? checked.toString().toLowerCase() === 'true' : value;
        const newUser = Object.assign(user, { [name]: updatedValue });
        this.setState({
            user: newUser
        });
    }

    onSave() {
        this.props.createUser(this.state.user);
        this.setState({
            user: {}
        });
    }

    retrieveRoles() {
        console.log('Retrieving roles');
        this.props.getRoles();
        const roleMapping = this.props.roles.forEach(role => {
            const rolename = role.roleName;
            return { label: rolename, value: rolename }
        });
        this.setState({
            roles: roleMapping
        });
        console.log(this.state.roles);
    }

    createModalFields() {
        const usernameKey = 'username';
        const passwordKey = 'password';
        const emailKey = 'emailAddress';
        const enabledKey = 'enabled';
        const roleNames = 'roleNames';

        return (
            <div>
                <TextInput name={usernameKey} label="Username" description="The users username." onChange={this.handleChange} value={this.state.user[usernameKey]} />
                <PasswordInput name={passwordKey} label="Password" description="The users password." onChange={this.handleChange} value={this.state.user[passwordKey]} />
                <TextInput name={emailKey} label="Email" description="The users email." onChange={this.handleChange} value={this.state.user[emailKey]} />
                <CheckboxInput name={enabledKey} label="Enabled" description="Enable this user for Alert." onChange={this.handleChange} isChecked={this.state.user[enabledKey]} />
                <DynamicSelectInput name={roleNames} label="Roles" description="Select the roles you want associated with the user." onChange={this.handleChange} multiSelect={true} options={this.state.roles}
                                    value={this.state.user[roleNames]} onFocus={this.retrieveRoles} />
            </div>
        );
    }

    render() {
        console.log('Render');
        console.log(this.state.roles);
        const { canCreate, canDelete } = this.props;

        return (
            <div>
                <div>
                    <TableDisplay newConfigFields={this.createModalFields} modalTitle="User" onConfigSave={this.onSave} retrieveData={this.retrieveData} columns={this.createColumns()} newButton={canCreate} deleteButton={canDelete} />
                </div>
            </div>
        );
    }
}

UserTable.defaultProps = {
    canCreate: true,
    canDelete: true
};

UserTable.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool
};

const mapStateToProps = state => ({
    users: state.users.data,
    roles: state.roles.data
});

const mapDispatchToProps = dispatch => ({
    createUser: user => dispatch(createNewUser(user)),
    deleteUser: username => dispatch(deleteUser(username)),
    getUsers: () => dispatch(fetchUsers()),
    getRoles: () => dispatch(fetchRoles())
});

export default connect(mapStateToProps, mapDispatchToProps)(UserTable);
