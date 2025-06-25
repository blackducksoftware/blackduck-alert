import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { clearUserFieldErrors, fetchUsers, saveUser, validateUser } from 'store/actions/users';
import DynamicSelectInput from 'common/component/input/DynamicSelectInput';
import Modal from 'common/component/modal/Modal';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import { USER_INPUT_FIELD_KEYS } from 'page/usermgmt/user/UserModel';
import classNames from 'classnames';

const useStyles = createUseStyles((theme) => ({
    messageContainer: {
        display: 'flex',
        alignItems: 'center',
        padding: [0, 0, '20px', '70px'],
        justifyContent: 'center'
    },
    warningStyle: {
        color: theme.colors.warning
    },
    descriptor: {
        fontSize: '14px',
        paddingLeft: '8px'
    },
    userModalContent: {
        margin: ['30px', 0, '60px']
    }
}));

const UserModal = ({ data, isOpen, toggleModal, modalOptions, setStatusMessage, successMessage }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { external } = data;
    const { copyDescription, submitText, title, type } = modalOptions;
    const [userModel, setUserModel] = useState(type === 'CREATE' ? {} : data);
    const [showLoader, setShowLoader] = useState(false);
    const messageContainerClass = classNames(classes.messageContainer, {
        [classes.warningStyle]: type === 'EDIT'
    });
    const [confirmPassword, setConfirmPassword] = useState();
    const [confirmPasswordError, setConfirmPasswordError] = useState({});

    const fieldErrors = useSelector((state) => state.users.error.fieldErrors);
    const roles = useSelector((state) => state.roles.data);
    const { saveStatus, error } = useSelector((state) => state.users);

    function disableSubmit() {
        const password = userModel[USER_INPUT_FIELD_KEYS.PASSWORD_KEY];
        const isPasswordSet = userModel[USER_INPUT_FIELD_KEYS.IS_PASSWORD_SET];

        // isPasswordSet will be set when the user is being edited. We don't need to disable the submit button in this case
        if (isPasswordSet) {
            return false;
        }
        // When copying or creating a user, these fields will be empty initially.
        // We want to disable the submit button until the user enters a password and confirms it
        if (!password || !confirmPassword) {
            return true;
        } else if (password && confirmPassword){
            return false;
        }

        // Otherwise, disable
        return true;
    }

    function passwordsMatch(user) {
        let passwordError = {};
        let matching = true;

        if ((user[USER_INPUT_FIELD_KEYS.PASSWORD_KEY] || confirmPassword) && (user[USER_INPUT_FIELD_KEYS.PASSWORD_KEY] !== confirmPassword)) {
            passwordError = HTTPErrorUtils.createFieldError('Passwords do not match.');
            matching = false;
        }
        setConfirmPasswordError(passwordError);

        return matching;
    }

    function handleClose() {
        toggleModal(false);
        dispatch(clearUserFieldErrors());
        dispatch(fetchUsers());
    }

    const handleOnChange = (label) => ({ target: { value } }) => {
        setUserModel((userData) => ({ ...userData, [label]: value }));
    };

    function handleSave() {
        dispatch(saveUser(userModel));
    }

    function handleSubmit() {
        dispatch(clearUserFieldErrors());

        if (passwordsMatch(userModel)) {
            dispatch(validateUser(userModel));
        }
    }

    useEffect(() => {
        if (saveStatus === 'VALIDATING' || saveStatus === 'SAVING') {
            setShowLoader(true);
        }

        if (saveStatus === 'VALIDATED') {
            handleSave();
        }

        if (saveStatus === 'SAVED') {
            setShowLoader(false);
            handleClose();
            setStatusMessage({
                message: successMessage,
                type: 'success'
            });
        }

        if (saveStatus === 'ERROR') {
            setShowLoader(false);
            setStatusMessage({
                message: error.fieldErrors.message,
                type: 'error'
            });
        }
    }, [saveStatus]);

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
            disableSubmit={disableSubmit()}
            submitText={submitText}
            showLoader={showLoader}
            noOverflow
        >
            <div className={classes.userModalContent}>
                {type === 'COPY' && (
                    <div className={messageContainerClass}>
                        <FontAwesomeIcon icon="exclamation-circle" size="2x" />
                        <span className={classes.descriptor}>
                            {copyDescription}
                        </span>
                    </div>
                )}
                {type === 'EDIT' && external && (
                    <div className={messageContainerClass}>
                        <FontAwesomeIcon icon="exclamation-circle" size="2x" />
                        <span className={classes.descriptor}>
                            This user is managed by a system external to Alert. Only roles can be assigned.
                        </span>
                    </div>
                )}
                <TextInput
                    id={USER_INPUT_FIELD_KEYS.USERNAME_KEY}
                    name={USER_INPUT_FIELD_KEYS.USERNAME_KEY}
                    label="Username"
                    customDescription="The user's username."
                    placeholder="Enter username..."
                    readOnly={external}
                    required={!external}
                    onChange={handleOnChange(USER_INPUT_FIELD_KEYS.USERNAME_KEY)}
                    value={userModel[USER_INPUT_FIELD_KEYS.USERNAME_KEY] || undefined}
                    errorName={USER_INPUT_FIELD_KEYS.USERNAME_KEY}
                    errorValue={fieldErrors[USER_INPUT_FIELD_KEYS.USERNAME_KEY]}
                />
                <PasswordInput
                    id={USER_INPUT_FIELD_KEYS.PASSWORD_KEY}
                    name={USER_INPUT_FIELD_KEYS.PASSWORD_KEY}
                    label="Password"
                    customDescription="The user's password."
                    placeholder="Enter password..."
                    readOnly={external}
                    required={!external}
                    onChange={handleOnChange(USER_INPUT_FIELD_KEYS.PASSWORD_KEY)}
                    value={userModel[USER_INPUT_FIELD_KEYS.PASSWORD_KEY] || undefined}
                    isSet={userModel[USER_INPUT_FIELD_KEYS.IS_PASSWORD_SET] || !type === 'COPY'}
                    errorName={USER_INPUT_FIELD_KEYS.PASSWORD_KEY}
                    errorValue={fieldErrors[USER_INPUT_FIELD_KEYS.PASSWORD_KEY]}
                />
                {!external && (
                    <PasswordInput
                        id="confirmPassword"
                        name="confirmPassword"
                        label="Confirm Password"
                        customDescription="The user's password."
                        placeholder="Confirm password..."
                        readOnly={false}
                        required
                        isSet={userModel[USER_INPUT_FIELD_KEYS.IS_PASSWORD_SET]}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        value={confirmPassword || undefined}
                        errorName="confirmPasswordError"
                        errorValue={confirmPasswordError}
                    />
                )}
                <TextInput
                    id={USER_INPUT_FIELD_KEYS.EMAIL_KEY}
                    name={USER_INPUT_FIELD_KEYS.EMAIL_KEY}
                    label="Email"
                    customDescription="The user's email."
                    placeholder="Enter email..."
                    readOnly={external}
                    required={!external}
                    onChange={handleOnChange(USER_INPUT_FIELD_KEYS.EMAIL_KEY)}
                    value={userModel[USER_INPUT_FIELD_KEYS.EMAIL_KEY] || undefined}
                    errorName={USER_INPUT_FIELD_KEYS.EMAIL_KEY}
                    errorValue={fieldErrors[USER_INPUT_FIELD_KEYS.EMAIL_KEY]}
                />
                <DynamicSelectInput
                    name={USER_INPUT_FIELD_KEYS.ROLENAMES_KEY}
                    id={USER_INPUT_FIELD_KEYS.ROLENAMES_KEY}
                    label="Roles"
                    customDescription="Select the roles you want associated with the user."
                    onChange={handleOnChange(USER_INPUT_FIELD_KEYS.ROLENAMES_KEY)}
                    multiSelect
                    options={getRoles()}
                    value={userModel[USER_INPUT_FIELD_KEYS.ROLENAMES_KEY] || undefined}
                    errorName={USER_INPUT_FIELD_KEYS.ROLENAMES_KEY}
                    errorValue={fieldErrors[USER_INPUT_FIELD_KEYS.ROLENAMES_KEY]}
                    maxMenuHeight={215}
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
        submitText: PropTypes.string,
        title: PropTypes.string,
        copyDescription: PropTypes.string
    }),
    setStatusMessage: PropTypes.func,
    successMessage: PropTypes.string
};

export default UserModal;
