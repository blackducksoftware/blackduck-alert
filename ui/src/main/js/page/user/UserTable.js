import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'common/table/TableDisplay';
import TextInput from 'common/input/TextInput';
import PasswordInput from 'common/input/PasswordInput';
import { connect } from 'react-redux';
import { clearUserFieldErrors, deleteUser, fetchUsers, saveUser, validateUser } from 'store/actions/users';
import DynamicSelectInput from 'common/input/DynamicSelectInput';
import { fetchRoles } from 'store/actions/roles';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import StatusMessage from 'common/StatusMessage';

const KEY_CONFIRM_PASSWORD_ERROR = 'confirmPasswordError';

class UserTable extends Component {
    constructor(props) {
        super(props);

        this.retrieveData = this.retrieveData.bind(this);
        this.createColumns = this.createColumns.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onSave = this.onSave.bind(this);
        this.onDelete = this.onDelete.bind(this);
        this.checkIfPasswordsMatch = this.checkIfPasswordsMatch.bind(this);
        this.onConfigClose = this.onConfigClose.bind(this);
        this.createModalFields = this.createModalFields.bind(this);
        this.retrieveRoles = this.retrieveRoles.bind(this);
        this.clearModalFieldState = this.clearModalFieldState.bind(this);
        this.onEdit = this.onEdit.bind(this);
        this.onCopy = this.onCopy.bind(this);

        this.state = {
            user: {},
            validateCallback: () => null,
            saveCallback: () => null,
            errorMessageState: null
        };
    }

    componentDidUpdate(prevProps) {
        const { saveStatus, errorMessage, deleteStatus } = this.props;
        const { validateCallback, saveCallback } = this.state;

        if (prevProps.saveStatus === 'VALIDATING' && saveStatus === 'VALIDATED') {
            validateCallback(true);
        }
        if (prevProps.saveStatus === 'SAVING' && saveStatus === 'SAVED') {
            saveCallback(true);
        } else if (prevProps.saveStatus === 'SAVING' && saveStatus === 'ERROR') {
            saveCallback(false);
        }
        if (prevProps.deleteStatus === 'DELETING' && deleteStatus === 'ERROR') {
            this.setState({
                errorMessageState: errorMessage
            });
        }
    }

    onSave(callback) {
        const { user } = this.state;
        const { validateUserAction, saveUserAction } = this.props;
        if (this.checkIfPasswordsMatch(user)) {
            this.setState({
                validateCallback: () => saveUserAction(user),
                saveCallback: callback
            }, () => validateUserAction(user));
            return true;
        }
        callback(false);
        return false;
    }

    onDelete(usersToDelete, callback) {
        const { deleteUserAction } = this.props;
        if (usersToDelete) {
            usersToDelete.forEach((userId) => {
                deleteUserAction(userId);
            });
        }
        callback();
        this.retrieveData();
    }

    onConfigClose(callback) {
        const { clearFieldErrors } = this.props;
        const { user } = this.state;
        clearFieldErrors();
        if (user && user[KEY_CONFIRM_PASSWORD_ERROR]) {
            delete user[KEY_CONFIRM_PASSWORD_ERROR];
        }
        callback();
    }

    onEdit(selectedRow, callback) {
        this.setState({
            user: selectedRow
        });
        callback();
        const { clearFieldErrors } = this.props;
        clearFieldErrors();
    }

    onCopy(selectedRow, callback) {
        const copy = JSON.parse(JSON.stringify(selectedRow));
        copy.id = null;
        this.setState({
            user: copy
        });
        callback();
    }

    clearModalFieldState() {
        const { user } = this.state;
        if (user && Object.keys(user).length > 0) {
            this.setState({
                user: {},
                errorMessageState: null
            });
            const { clearFieldErrors } = this.props;
            clearFieldErrors();
        }
    }

    retrieveRoles() {
        const { roles } = this.props;
        return roles.map((role) => {
            const rolename = role.roleName;
            return {
                label: rolename,
                value: rolename
            };
        });
    }

    createColumns() {
        return [
            {
                header: 'id',
                headerLabel: 'Id',
                isKey: true,
                hidden: true
            },
            {
                header: 'external',
                headerLabel: 'External',
                isKey: false,
                hidden: true
            },
            {
                header: 'username',
                headerLabel: 'Username',
                isKey: false,
                hidden: false
            },
            {
                header: 'emailAddress',
                headerLabel: 'Email',
                isKey: false,
                hidden: false
            },
            {
                header: 'authenticationType',
                headerLabel: 'Authentication Type',
                isKey: false,
                hidden: false
            }
        ];
    }

    retrieveData() {
        const { getUsers } = this.props;
        getUsers();
    }

    handleChange(e) {
        const {
            name, value, type, checked
        } = e.target;
        const { user } = this.state;

        const updatedValue = type === 'checkbox' ? checked.toString()
            .toLowerCase() === 'true' : value;
        const newUser = Object.assign(user, { [name]: updatedValue });
        this.setState({
            user: newUser
        });
    }

    checkIfPasswordsMatch(user) {
        const passwordKey = 'password';
        const confirmPasswordKey = 'confirmPassword';
        const confirmPasswordError = KEY_CONFIRM_PASSWORD_ERROR;

        let passwordError = {};
        let matching = true;
        if ((user[passwordKey] || user[confirmPasswordKey]) && (user[passwordKey] !== user[confirmPasswordKey])) {
            passwordError = HTTPErrorUtils.createFieldError('Passwords do not match.');
            matching = false;
        }
        const newUser = Object.assign(user, { [confirmPasswordError]: passwordError });
        this.setState({
            user: newUser
        });
        return matching;
    }

    createModalFields() {
        const { user } = this.state;
        const { fieldErrors, getRoles } = this.props;

        const usernameKey = 'username';
        const passwordKey = 'password';
        const confirmPasswordKey = 'confirmPassword';
        const confirmPasswordError = KEY_CONFIRM_PASSWORD_ERROR;
        const emailKey = 'emailAddress';
        const roleNames = 'roleNames';
        const passwordSetKey = 'passwordSet';
        const externalKey = 'external';
        const external = user[externalKey];
        const externalNote = (
            <div className="form-group">
                <label className="col-sm-3 col-form-label text-right warningStatus" />
                <div className="d-inline-flex">
                    <span className="warningStatus">
                        <FontAwesomeIcon icon="exclamation-triangle" className="alert-icon" size="lg" />
                    </span>
                </div>
                <div className="d-inline-flex p-2 col-sm-8 warningStatus">
                    This user is managed by a system external to Alert. Only roles can be assigned.
                </div>
            </div>
        );
        let passwordConfirmField = null;
        if (!external) {
            passwordConfirmField = (
                <PasswordInput
                    id={confirmPasswordKey}
                    name={confirmPasswordKey}
                    label="Confirm Password"
                    description="The user's password."
                    readOnly={false}
                    required
                    onChange={this.handleChange}
                    value={user[confirmPasswordKey]}
                    errorName={confirmPasswordKey}
                    errorValue={user[confirmPasswordError]}
                />
            );
        }

        return (
            <div>
                {external && externalNote}
                <TextInput
                    id={usernameKey}
                    name={usernameKey}
                    label="Username"
                    description="The user's username."
                    readOnly={external}
                    required={!external}
                    onChange={this.handleChange}
                    value={user[usernameKey]}
                    errorName={usernameKey}
                    errorValue={fieldErrors[usernameKey]}
                />
                <PasswordInput
                    id={passwordKey}
                    name={passwordKey}
                    label="Password"
                    description="The user's password."
                    readOnly={external}
                    required={!external}
                    onChange={this.handleChange}
                    value={user[passwordKey]}
                    isSet={user[passwordSetKey]}
                    errorName={passwordKey}
                    errorValue={fieldErrors[passwordKey]}
                />
                {passwordConfirmField}
                <TextInput
                    id={emailKey}
                    name={emailKey}
                    label="Email"
                    description="The user's email."
                    readOnly={external}
                    required={!external}
                    onChange={this.handleChange}
                    value={user[emailKey]}
                    errorName={emailKey}
                    errorValue={fieldErrors[emailKey]}
                />
                <DynamicSelectInput
                    name={roleNames}
                    id={roleNames}
                    label="Roles"
                    description="Select the roles you want associated with the user."
                    onChange={this.handleChange}
                    multiSelect
                    options={this.retrieveRoles()}
                    value={user[roleNames]}
                    onFocus={getRoles}
                    errorName={roleNames}
                    errorValue={fieldErrors[roleNames]}
                />
            </div>
        );
    }

    render() {
        const {
            canCreate, canDelete, fieldErrors, errorMessage, inProgress, users, autoRefresh
        } = this.props;
        const { user, errorMessageState } = this.state;
        const fieldErrorKeys = Object.keys(fieldErrors);
        const hasErrors = (fieldErrorKeys && fieldErrorKeys.length > 0)
            || (user[KEY_CONFIRM_PASSWORD_ERROR] && user[KEY_CONFIRM_PASSWORD_ERROR].length > 0);
        return (
            <div>
                <div>
                    <StatusMessage
                        id="$user-status-message"
                        errorMessage={errorMessageState}
                    />
                    <TableDisplay
                        id="users"
                        autoRefresh={autoRefresh}
                        newConfigFields={this.createModalFields}
                        modalTitle="User"
                        clearModalFieldState={this.clearModalFieldState}
                        onConfigSave={this.onSave}
                        onConfigDelete={this.onDelete}
                        onConfigClose={this.onConfigClose}
                        onEditState={this.onEdit}
                        onConfigCopy={this.onCopy}
                        refreshData={this.retrieveData}
                        data={users}
                        columns={this.createColumns()}
                        newButton={canCreate}
                        deleteButton={canDelete}
                        hasFieldErrors={hasErrors}
                        errorDialogMessage={errorMessage}
                        inProgress={inProgress}
                    />
                </div>
            </div>
        );
    }
}

UserTable.defaultProps = {
    canCreate: true,
    canDelete: true,
    errorMessage: null,
    fieldErrors: {},
    inProgress: false,
    saveStatus: null,
    deleteStatus: null,
    users: [],
    roles: [],
    autoRefresh: true
};

UserTable.propTypes = {
    saveUserAction: PropTypes.func.isRequired,
    deleteUserAction: PropTypes.func.isRequired,
    validateUserAction: PropTypes.func.isRequired,
    getUsers: PropTypes.func.isRequired,
    getRoles: PropTypes.func.isRequired,
    clearFieldErrors: PropTypes.func.isRequired,
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool,
    errorMessage: PropTypes.string,
    fieldErrors: PropTypes.object,
    inProgress: PropTypes.bool,
    saveStatus: PropTypes.string,
    deleteStatus: PropTypes.string,
    users: PropTypes.array,
    roles: PropTypes.array,
    autoRefresh: PropTypes.bool
};

const mapStateToProps = (state) => ({
    users: state.users.data,
    roles: state.roles.data,
    errorMessage: state.users.error.message,
    fieldErrors: state.users.error.fieldErrors,
    inProgress: state.users.inProgress,
    saveStatus: state.users.saveStatus,
    deleteStatus: state.users.deleteStatus,
    autoRefresh: state.refresh.autoRefresh
});

const mapDispatchToProps = (dispatch) => ({
    saveUserAction: (user) => dispatch(saveUser(user)),
    deleteUserAction: (userId) => dispatch(deleteUser(userId)),
    getUsers: () => dispatch(fetchUsers()),
    getRoles: () => dispatch(fetchRoles()),
    clearFieldErrors: () => dispatch(clearUserFieldErrors()),
    validateUserAction: (user) => dispatch(validateUser(user))
});

export default connect(mapStateToProps, mapDispatchToProps)(UserTable);
