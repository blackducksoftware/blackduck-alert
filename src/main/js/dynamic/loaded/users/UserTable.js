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
        this.onDelete = this.onDelete.bind(this);
        this.createModalFields = this.createModalFields.bind(this);
        this.retrieveRoles = this.retrieveRoles.bind(this);
        this.onDelete = this.onDelete.bind(this);

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
        this.retrieveData();
    }

    onDelete(usersToDelete) {
        if (usersToDelete) {
            usersToDelete.forEach(userName => {
                this.props.deleteUser(userName);
            });
        }
        this.retrieveData();
    }

    retrieveRoles() {
        return this.props.roles.map(role => {
            const rolename = role.roleName;
            return { label: rolename, value: rolename }
        });
    }

    createModalFields(selectedRow) {
        const { user } = this.state;
        let newUser = user;
        if (selectedRow) {
            newUser = Object.assign({}, user, selectedRow);
        }

        const usernameKey = 'username';
        const passwordKey = 'password';
        const emailKey = 'emailAddress';
        const enabledKey = 'enabled';
        const roleNames = 'roleNames';

        return (
            <div>
                <TextInput name={usernameKey} label="Username" description="The users username." required={true} onChange={this.handleChange} value={newUser[usernameKey]} />
                <PasswordInput name={passwordKey} label="Password" description="The users password." required={true} onChange={this.handleChange} value={newUser[passwordKey]} />
                <TextInput name={emailKey} label="Email" description="The users email." required={true} onChange={this.handleChange} value={newUser[emailKey]} />
                <CheckboxInput name={enabledKey} label="Enabled" description="Enable this user for Alert." onChange={this.handleChange} isChecked={newUser[enabledKey]} />
                <DynamicSelectInput
                    name={roleNames}
                    id={roleNames}
                    label="Roles"
                    description="Select the roles you want associated with the user."
                    onChange={this.handleChange}
                    multiSelect={true}
                    options={this.retrieveRoles()}
                    value={newUser[roleNames]}
                    onFocus={this.props.getRoles} />
            </div>
        );
    }

    render() {
        const { canCreate, canDelete } = this.props;

        return (
            <div>
                <div>
                    <TableDisplay
                        newConfigFields={this.createModalFields}
                        modalTitle="User"
                        onConfigSave={this.onSave}
                        onConfigDelete={this.onDelete}
                        refreshData={this.retrieveData}
                        data={this.props.users}
                        columns={this.createColumns()}
                        newButton={canCreate}
                        deleteButton={canDelete} />
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
