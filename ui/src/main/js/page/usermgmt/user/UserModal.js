import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { saveUser, validateUser, fetchUsers } from 'store/actions/users';
import DynamicSelectInput from 'common/component/input/DynamicSelectInput';
import Modal from 'common/component/modal/Modal';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import UserFieldKeyEnum from 'page/usermgmt/user/UserModel';

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
});

const UserModal = ({ data, isOpen, toggleModal, modalOptions }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { external } = data;
    const { submitText, title, type } = modalOptions;
    const [userModel, setUserModel] = useState(type === 'CREATE' ? {} : data);
    const [showLoader, setShowLoader] = useState(false);

    const fieldErrors = useSelector(state => state.users.error.fieldErrors);
    const roles = useSelector(state => state.roles.data);
    const { saveStatus } = useSelector(state => state.users);

    useEffect(() => {
        if ( saveStatus === 'VALIDATING' || saveStatus === 'SAVING' ) {
            setShowLoader(true);
        }

        if ( saveStatus === 'VALIDATED' ) { 
            handleSave();
        }

        if ( saveStatus === 'SAVED' ) { 
            setShowLoader(false);
            handleClose();
        }
    }, [saveStatus]);

    function handleClose() {
        toggleModal(false);
        dispatch(fetchUsers());
    }

    const handleOnChange = (label) => {
        return ({ target: { value } }) => {
            setUserModel(userData => ({...userData, [label]: value }));
        }
    }

    function handleSave() {
        dispatch(saveUser(userModel));
    }

    function handleSubmit() {
        if (type === 'EDIT') {
            handleSave();
        }

        if (passwordsMatch(userModel)) {
            console.log('here', userModel);
            dispatch(validateUser(userModel));
        }
    }

    function passwordsMatch(user) {
        let passwordError = {};
        let matching = true;

        if ((user[UserFieldKeyEnum.PASSWORD_KEY] || user[UserFieldKeyEnum.CONFIRM_PASSWORD_KEY]) && (user[UserFieldKeyEnum.PASSWORD_KEY] !== user[UserFieldKeyEnum.CONFIRM_PASSWORD_KEY])) {
            passwordError = HTTPErrorUtils.createFieldError('Passwords do not match.');
            matching = false;
        }
        setUserModel(Object.assign(user, { [UserFieldKeyEnum.CONFIRM_PASSWORD_ERROR_KEY]: passwordError }));
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
            title={title}
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText={submitText}
            showLoader={showLoader}
        >
            { type === 'COPY' && (
                <div className={classes.descriptorContainer}>
                    <FontAwesomeIcon icon="exclamation-circle" size="2x" />
                    <span className={classes.descriptor}>
                        Performing this action will create a new user by using the same settings as '{modalOptions.copiedUsername}'
                    </span>
                </div>
            )}
            <div>
                <TextInput
                    id={UserFieldKeyEnum.USERNAME_KEY}
                    name={UserFieldKeyEnum.USERNAME_KEY}
                    label="Username"
                    description="The user's username."
                    placeholder="Enter username..."
                    readOnly={external}
                    required={!external}
                    onChange={handleOnChange(UserFieldKeyEnum.USERNAME_KEY)}
                    value={userModel[UserFieldKeyEnum.USERNAME_KEY]}
                    errorName={UserFieldKeyEnum.USERNAME_KEY}
                    errorValue={fieldErrors[UserFieldKeyEnum.USERNAME_KEY]}
                />
                <PasswordInput
                    id={UserFieldKeyEnum.PASSWORD_KEY}
                    name={UserFieldKeyEnum.PASSWORD_KEY}
                    label="Password"
                    description="The user's password."
                    placeholder="Enter password..."
                    readOnly={external}
                    required={!external}
                    onChange={handleOnChange(UserFieldKeyEnum.PASSWORD_KEY)}
                    value={userModel[UserFieldKeyEnum.PASSWORD_KEY]}
                    isSet={userModel[UserFieldKeyEnum.IS_PASSWORD_SET] || !type==='COPY'}
                    errorName={UserFieldKeyEnum.PASSWORD_KEY}
                    errorValue={fieldErrors[UserFieldKeyEnum.PASSWORD_KEY]}
                />
                <PasswordInput
                    id={UserFieldKeyEnum.CONFIRM_PASSWORD_KEY}
                    name={UserFieldKeyEnum.CONFIRM_PASSWORD_KEY}
                    label="Confirm Password"
                    description="The user's password."
                    placeholder="Confirm password..."
                    readOnly={false}
                    required
                    isSet={userModel[UserFieldKeyEnum.IS_PASSWORD_SET]}
                    onChange={handleOnChange(UserFieldKeyEnum.CONFIRM_PASSWORD_KEY)}
                    value={userModel[UserFieldKeyEnum.CONFIRM_PASSWORD_KEY]}
                    errorName={UserFieldKeyEnum.CONFIRM_PASSWORD_KEY}
                    errorValue={userModel[UserFieldKeyEnum.CONFIRM_PASSWORD_ERROR_KEY]}
                />
                <TextInput
                    id={UserFieldKeyEnum.EMAIL_KEY}
                    name={UserFieldKeyEnum.EMAIL_KEY}
                    label="Email"
                    description="The user's email."
                    placeholder="Enter email..."
                    readOnly={external}
                    required={!external}
                    onChange={handleOnChange(UserFieldKeyEnum.EMAIL_KEY)}
                    value={userModel[UserFieldKeyEnum.EMAIL_KEY]}
                    errorName={UserFieldKeyEnum.EMAIL_KEY}
                    errorValue={fieldErrors[UserFieldKeyEnum.EMAIL_KEY]}
                />
                <DynamicSelectInput
                    name={UserFieldKeyEnum.ROLENAMES_KEY}
                    id={UserFieldKeyEnum.ROLENAMES_KEY}
                    label="Roles"
                    description="Select the roles you want associated with the UserFieldKeyEnum."
                    onChange={handleOnChange(UserFieldKeyEnum.ROLENAMES_KEY)}
                    multiSelect
                    options={getRoles()}
                    value={userModel[UserFieldKeyEnum.ROLENAMES_KEY]}
                    errorName={UserFieldKeyEnum.ROLENAMES_KEY}
                    errorValue={fieldErrors[UserFieldKeyEnum.ROLENAMES_KEY]}
                />
            </div>
        </Modal>
    );
};

UserModal.propTypes = {
    data: PropTypes.oneOfType([
        PropTypes.arrayOf(PropTypes.object),
        PropTypes.object
    ]),
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    modalOptions: PropTypes.shape({
        type: PropTypes.string,
        submitText: PropTypes.string
    })
};

export default UserModal;