import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { saveUser, validateUser, fetchUsers } from 'store/actions/users';
import DynamicSelectInput from 'common/component/input/DynamicSelectInput';
import Modal from 'common/component/modal/Modal';
import CheckboxInput from 'common/component/input/CheckboxInput';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { USER_INPUT_FIELD_KEYS } from 'page/usermgmt/user/UserModel';
import { BLACKDUCK_GLOBAL_FIELD_KEYS } from './blackduck/BlackDuckModel';

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

const ProviderModal = ({ data, isOpen, toggleModal, modalOptions, setStatusMessage, successMessage, readonly }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { external } = data;
    const { copyDescription, submitText, title, type } = modalOptions;
    const [userModel, setUserModel] = useState(type === 'CREATE' ? {} : data);
    const [showLoader, setShowLoader] = useState(false);

    const fieldErrors = useSelector((state) => state.users.error.fieldErrors);
    const roles = useSelector((state) => state.roles.data);
    const { saveStatus, error } = useSelector((state) => state.users);

    function passwordsMatch(user) {
        let passwordError = {};
        let matching = true;

        if ((user[USER_INPUT_FIELD_KEYS.PASSWORD_KEY] || user[USER_INPUT_FIELD_KEYS.CONFIRM_PASSWORD_KEY]) && (user[USER_INPUT_FIELD_KEYS.PASSWORD_KEY] !== user[USER_INPUT_FIELD_KEYS.CONFIRM_PASSWORD_KEY])) {
            passwordError = HTTPErrorUtils.createFieldError('Passwords do not match.');
            matching = false;
        }
        setUserModel(Object.assign(user, { [USER_INPUT_FIELD_KEYS.CONFIRM_PASSWORD_ERROR_KEY]: passwordError }));

        return matching;
    }

    function handleClose() {
        toggleModal(false);
        dispatch(fetchUsers());
    }

    const handleOnChange = (label) => ({ target: { value } }) => {
        setUserModel((userData) => ({ ...userData, [label]: value }));
    };

    function handleSave() {
        dispatch(saveUser(userModel));
    }

    function handleSubmit() {
        if (type === 'EDIT') {
            handleSave();
        }

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
            handleClose();
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
            submitText={submitText}
            showLoader={showLoader}
        >
            <CheckboxInput
                id={BLACKDUCK_GLOBAL_FIELD_KEYS.enabled}
                name={BLACKDUCK_GLOBAL_FIELD_KEYS.enabled}
                label="Enabled"
                description="If selected, this provider configuration will be able to pull data into Alert and available to configure with distribution jobs, otherwise, it will not be available for those usages."
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                isChecked={FieldModelUtilities.getFieldModelBooleanValue(formData, BLACKDUCK_GLOBAL_FIELD_KEYS.enabled)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(BLACKDUCK_GLOBAL_FIELD_KEYS.enabled)}
                errorValue={errors.fieldErrors[BLACKDUCK_GLOBAL_FIELD_KEYS.enabled]}
            />
            <TextInput
                id={USER_INPUT_FIELD_KEYS.USERNAME_KEY}
                name={USER_INPUT_FIELD_KEYS.USERNAME_KEY}
                label="Username"
                description="The user's username."
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
                description="The user's password."
                placeholder="Enter password..."
                readOnly={external}
                required={!external}
                onChange={handleOnChange(USER_INPUT_FIELD_KEYS.PASSWORD_KEY)}
                value={userModel[USER_INPUT_FIELD_KEYS.PASSWORD_KEY] || undefined}
                isSet={userModel[USER_INPUT_FIELD_KEYS.IS_PASSWORD_SET] || !type === 'COPY'}
                errorName={USER_INPUT_FIELD_KEYS.PASSWORD_KEY}
                errorValue={fieldErrors[USER_INPUT_FIELD_KEYS.PASSWORD_KEY]}
            />
            <PasswordInput
                id={USER_INPUT_FIELD_KEYS.CONFIRM_PASSWORD_KEY}
                name={USER_INPUT_FIELD_KEYS.CONFIRM_PASSWORD_KEY}
                label="Confirm Password"
                description="The user's password."
                placeholder="Confirm password..."
                readOnly={false}
                required
                isSet={userModel[USER_INPUT_FIELD_KEYS.IS_PASSWORD_SET]}
                onChange={handleOnChange(USER_INPUT_FIELD_KEYS.CONFIRM_PASSWORD_KEY)}
                value={userModel[USER_INPUT_FIELD_KEYS.CONFIRM_PASSWORD_KEY] || undefined}
                errorName={USER_INPUT_FIELD_KEYS.CONFIRM_PASSWORD_KEY}
                errorValue={userModel[USER_INPUT_FIELD_KEYS.CONFIRM_PASSWORD_ERROR_KEY]}
            />
            <TextInput
                id={USER_INPUT_FIELD_KEYS.EMAIL_KEY}
                name={USER_INPUT_FIELD_KEYS.EMAIL_KEY}
                label="Email"
                description="The user's email."
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
                description="Select the roles you want associated with the USER_INPUT_FIELD_KEYS."
                onChange={handleOnChange(USER_INPUT_FIELD_KEYS.ROLENAMES_KEY)}
                multiSelect
                options={getRoles()}
                value={userModel[USER_INPUT_FIELD_KEYS.ROLENAMES_KEY] || undefined}
                errorName={USER_INPUT_FIELD_KEYS.ROLENAMES_KEY}
                errorValue={fieldErrors[USER_INPUT_FIELD_KEYS.ROLENAMES_KEY]}
            />
        </Modal>
    );
};

ProviderModal.propTypes = {
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

export default ProviderModal;
