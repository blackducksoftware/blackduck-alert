import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchRoles } from 'store/actions/roles';
import { saveUser, validateUser, fetchUsers } from 'store/actions/users';
import Modal from 'common/component/modal/Modal';
import DynamicSelectInput from 'common/component/input/DynamicSelectInput';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import UserEnum from 'common/util/enums/User';

const UserCreateModal = ({ data, isOpen, toggleModal }) => {
    const dispatch = useDispatch();
    const { external } = data;
    const [newUser, setNewUser] = useState({});
    
    const fieldErrors = useSelector(state => state.users.error.fieldErrors);
    const inProgress = useSelector(state => state.users.inProgress);
    const roles = useSelector(state => state.roles.data);
    const saveStatus = useSelector(state => state.users.saveStatus);

    useEffect(() => {
        dispatch(fetchRoles());
    }, [data]);

    useEffect(() => {
        if ( saveStatus === 'VALIDATED' && !inProgress) { 
            handleSave();
        }
    }, [saveStatus]);

    function clearInputs() {
        setNewUser({});
    }

    function handleClose() {
        toggleModal(false);
        clearInputs();
        dispatch(fetchUsers());
    }

    const handleOnChange = (label) => {
        return ({ target: { value } }) => {
            setNewUser(userData => ({...userData, [label]: value }));
        }
    }

    function handleSave() {
        dispatch(saveUser(newUser));
        handleClose();
    }

    function handleSubmit() {
        if (passwordsMatch(newUser)) {
            dispatch(validateUser(newUser));
        }
    }

    function passwordsMatch(user) {
        let passwordError = {};
        let matching = true;
        if ((user[UserEnum.PASSWORD_KEY] || user[UserEnum.CONFIRM_PASSWORD_KEY]) && (user[UserEnum.PASSWORD_KEY] !== user[UserEnum.CONFIRM_PASSWORD_KEY])) {
            passwordError = HTTPErrorUtils.createFieldError('Passwords do not match.');
            matching = false;
        }
        setNewUser(Object.assign(user, { [UserEnum.CONFIRM_PASSWORD_ERROR_KEY]: passwordError }));
        // TODO: Why do I have to refresh data for this validation to show?
        dispatch(fetchUsers());
        return matching;
    }

    function getRoles() {
        return roles.map((role) => {
            const { roleName } = role;
            return {
                label: roleName,
                value: roleName
            };
        });
    }

    return (
        <Modal 
            isOpen={isOpen} 
            size="lg" 
            title="Create User"
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText="Create"
        >
            <div>
                <TextInput
                    id={UserEnum.USERNAME_KEY}
                    name={UserEnum.USERNAME_KEY}
                    label="Username"
                    description="The user's username."
                    placeholder="Enter username..."
                    readOnly={external}
                    required={!external}
                    onChange={handleOnChange(UserEnum.USERNAME_KEY)}
                    value={newUser[UserEnum.USERNAME_KEY]}
                    errorName={UserEnum.USERNAME_KEY}
                    errorValue={fieldErrors[UserEnum.USERNAME_KEY]}
                />
                <PasswordInput
                    id={UserEnum.PASSWORD_KEY}
                    name={UserEnum.PASSWORD_KEY}
                    label="Password"
                    description="The user's password."
                    placeholder="Enter password..."
                    readOnly={external}
                    required={!external}
                    onChange={handleOnChange(UserEnum.PASSWORD_KEY)}
                    value={newUser[UserEnum.PASSWORD_KEY]}
                    isSet={false}
                    errorName={UserEnum.PASSWORD_KEY}
                    errorValue={fieldErrors[UserEnum.PASSWORD_KEY]}
                />
                <PasswordInput
                    id={UserEnum.CONFIRM_PASSWORD_KEY}
                    name={UserEnum.CONFIRM_PASSWORD_KEY}
                    label="Confirm Password"
                    description="The user's password."
                    placeholder="Confirm password..."
                    readOnly={false}
                    required
                    onChange={handleOnChange(UserEnum.CONFIRM_PASSWORD_KEY)}
                    value={newUser[UserEnum.CONFIRM_PASSWORD_KEY]}
                    errorName={UserEnum.CONFIRM_PASSWORD_KEY}
                    errorValue={newUser[UserEnum.CONFIRM_PASSWORD_ERROR_KEY]}
                />
                <TextInput
                    id={UserEnum.EMAIL_KEY}
                    name={UserEnum.EMAIL_KEY}
                    label="Email"
                    description="The user's email."
                    placeholder="Enter email..."
                    readOnly={external}
                    required={!external}
                    onChange={handleOnChange(UserEnum.EMAIL_KEY)}
                    value={newUser[UserEnum.EMAIL_KEY]}
                    errorName={UserEnum.EMAIL_KEY}
                    errorValue={fieldErrors[UserEnum.EMAIL_KEY]}
                />
                <DynamicSelectInput
                    name={UserEnum.ROLENAMES_KEY}
                    id={UserEnum.ROLENAMES_KEY}
                    label="Roles"
                    description="Select the roles you want associated with the UserEnum."
                    onChange={handleOnChange(UserEnum.ROLENAMES_KEY)}
                    multiSelect
                    options={getRoles()}
                    value={newUser[UserEnum.ROLENAMES_KEY]}
                    errorName={UserEnum.ROLENAMES_KEY}
                    errorValue={fieldErrors[UserEnum.ROLENAMES_KEY]}
                />
            </div>
        </Modal>
    );
};

export default UserCreateModal;