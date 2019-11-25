import React, { Component } from 'react';
import PropTypes from 'prop-types'
import TableDisplay from 'field/TableDisplay';
import TextInput from 'field/input/TextInput';
import PasswordInput from 'field/input/PasswordInput';
import CheckboxInput from 'field/input/CheckboxInput';
import { connect } from 'react-redux';
import { createNewUser, deleteUser, fetchUsers } from 'store/actions/users';

class UserTable extends Component {
    constructor(props) {
        super(props);

        this.retrieveData = this.retrieveData.bind(this);
        this.createColumns = this.createColumns.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onSave = this.onSave.bind(this);

        this.state = {};
    }

    componentDidMount() {
        this.retrieveData();
    }

    createColumns() {
        return [
            {
                header: 'username',
                headerLabel: 'Username',
                isKey: true
            },
            {
                header: 'email_address',
                headerLabel: 'Email',
                isKey: false
            },
            {
                header: 'expired',
                headerLabel: 'Expired',
                isKey: false
            },
            {
                header: 'locked',
                headerLabel: 'Locked',
                isKey: false
            },
            {
                header: 'password_expired',
                headerLabel: 'Password Expired',
                isKey: false
            },
            {
                header: 'enabled',
                headerLabel: 'Enabled',
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
        const updatedValue = type === 'checkbox' ? checked.toString().toLowerCase() === 'true' : value;
        this.setState({
            [name]: updatedValue
        });
    }

    onSave() {
        this.props.createUser(this.state['username']);
    }

    render() {
        const { canCreate, canDelete } = this.props;

        const usernameKey = 'username';
        const passwordKey = 'password';
        const emailKey = 'email_address';
        const enabledKey = 'enabled';

        return (
            <div>
                <div>
                    <TableDisplay modalTitle="User" onConfigSave={this.onSave} retrieveData={this.retrieveData} columns={this.createColumns()} newButton={canCreate} deleteButton={canDelete}>
                        <TextInput name={usernameKey} label="Username" description="The users username." onChange={this.handleChange} value={this.state[usernameKey]} />
                        <PasswordInput name={passwordKey} label="Password" description="The users password." onChange={this.handleChange} value={this.state[passwordKey]} />
                        <TextInput name={emailKey} label="Email" description="The users email." onChange={this.handleChange} value={this.state[emailKey]} />
                        <CheckboxInput name={enabledKey} label="Enabled" description="Enable this user for Alert." onChange={this.handleChange} isChecked={this.state[enabledKey]} />
                    </TableDisplay>
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
    users: state.users.data
});

const mapDispatchToProps = dispatch => ({
    createUser: username => dispatch(createNewUser(username)),
    deleteUser: username => dispatch(deleteUser(username)),
    getUsers: () => dispatch(fetchUsers())
});

export default connect(mapStateToProps, mapDispatchToProps)(UserTable);
