import React, { useEffect, useState } from 'react';
import { createUseStyles } from 'react-jss';
import { useDispatch, useSelector } from 'react-redux';
import { saveUser, validateUser } from 'store/actions/users';
import { fetchRoles } from 'store/actions/roles';
import Modal from 'common/component/modal/Modal';
import DynamicSelectInput from 'common/component/input/DynamicSelectInput';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import UserEnum from 'common/util/enums/User';

const useStyles = createUseStyles({
    descriptorContainer: {
        display: 'flex',
        alignItems: 'center', 
        padding: [0, 0, '20px', '60px']
    },
    descriptor: {
        fontSize: '14px',
        paddingLeft: '8px'
    }
})

const UserCopyModal = ({ data, isOpen, toggleModal, copiedUsername }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { external } = data;
    const modalHeader = `Copy User '${copiedUsername}'`
    const [userData, setUserData] = useState(data);
    
    const fieldErrors = useSelector(state => state.users.error.fieldErrors);
    const inProgress = useSelector(state => state.users.inProgress);
    const roles = useSelector(state => state.roles.data);
    const saveStatus = useSelector(state => state.users.saveStatus);

    useEffect(() => {
        if ( saveStatus === 'VALIDATED' && !inProgress) { 
            handleSave();
        }
    }, [saveStatus, inProgress]);

    function handleClose() {
        toggleModal(false);
    }

    const handleOnChange = (label) => {
        return ({ target: { value } }) => {
            setUserData(userData => ({...userData, [label]: value }));
        }
    }
    
    function handleSave() {
        dispatch(saveUser(userData));
        handleClose();
    }

    function handleSubmit() {
        if (passwordsMatch(userData)) {
            dispatch(validateUser(userData));
        }
    }

    function passwordsMatch(user) {
        let passwordError = {};
        let matching = true;
        if ((user[UserEnum.PASSWORD_KEY] || user[UserEnum.CONFIRM_PASSWORD_KEY]) && (user[UserEnum.PASSWORD_KEY] !== user[UserEnum.CONFIRM_PASSWORD_KEY])) {
            passwordError = HTTPErrorUtils.createFieldError('Passwords do not match.');
            matching = false;
        }
        setUserData(Object.assign(user, { [UserEnum.CONFIRM_PASSWORD_ERROR_KEY]: passwordError }));
        // TODO: Why do I have to refresh roles for this validation to show?
        dispatch(fetchRoles());
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

    console.log(userData);

    return (
        <Modal 
            isOpen={isOpen} 
            size="lg" 
            title={modalHeader}
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText="Create"
        >
            <div className={classes.descriptorContainer}>
                <FontAwesomeIcon icon="exclamation-circle" size="2x" />
                <span className={classes.descriptor}>
                    Performing this action will create a new user by using the same settings as '{copiedUsername}'
                </span>
            </div>
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
                    value={userData[UserEnum.USERNAME_KEY]}
                    errorName={UserEnum.USERNAME_KEY}
                    errorValue={fieldErrors[UserEnum.USERNAME_KEY]}
                />
                <PasswordInput
                    id={UserEnum.PASSWORD_KEY}
                    name={UserEnum.PASSWORD_KEY}
                    label="Password"
                    description="The user's password."
                    readOnly={external}
                    required={!external}
                    onChange={handleOnChange(UserEnum.PASSWORD_KEY)}
                    value={userData[UserEnum.PASSWORD_KEY]}
                    isSet
                    errorName={UserEnum.PASSWORD_KEY}
                    errorValue={fieldErrors[UserEnum.PASSWORD_KEY]}
                />
                <PasswordInput
                    id={UserEnum.CONFIRM_PASSWORD_KEY}
                    name={UserEnum.CONFIRM_PASSWORD_KEY}
                    label="Confirm Password"
                    description="The user's password."
                    readOnly={false}
                    required
                    onChange={handleOnChange(UserEnum.CONFIRM_PASSWORD_KEY)}
                    value={userData[UserEnum.CONFIRM_PASSWORD_KEY]}
                    isSet
                    errorName={UserEnum.CONFIRM_PASSWORD_KEY}
                    errorValue={userData[UserEnum.CONFIRM_PASSWORD_ERROR_KEY]}
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
                    value={userData[UserEnum.EMAIL_KEY]}
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
                    value={userData[UserEnum.ROLENAMES_KEY]}
                    errorName={UserEnum.ROLENAMES_KEY}
                    errorValue={fieldErrors[UserEnum.ROLENAMES_KEY]}
                />
            </div>
        </Modal>
    );
};

export default UserCopyModal;