import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import PropTypes from 'prop-types';
import { clearRoleFieldErrors, fetchRoles, saveRole, validateRole } from 'store/actions/roles';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Modal from 'common/component/modal/Modal';
import TextInput from 'common/component/input/TextInput';
import PermissionTable from 'page/usermgmt/roles/PermissionTable';

const useStyles = createUseStyles({
    descriptorContainer: {
        display: 'flex',
        alignItems: 'center',
        margin: ['auto', '5%'],
        padding: [0, 0, '20px', '60px']
    },
    descriptor: {
        fontSize: '14px',
        paddingLeft: '8px'
    },
    permissionTable: {
        width: '90%',
        margin: 'auto'
    }
});

const RoleModal = ({ data, isOpen, toggleModal, modalOptions, setStatusMessage, statusMessage }) => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const { copyDescription, type, title, submitText } = modalOptions;
    const [role, setRole] = useState(type === 'CREATE' ? { permissions: [] } : data);
    const [showLoader, setShowLoader] = useState(false);

    const ROLE_NAME_KEY = 'roleName';

    const descriptors = useSelector((state) => state.descriptors.items);
    const { saveStatus, error } = useSelector((state) => state.roles);

    function handleClose() {
        toggleModal(false);
        dispatch(clearRoleFieldErrors());
        dispatch(fetchRoles());
    }

    function handleSave() {
        dispatch(saveRole(role));
    }

    function handleSubmit() {
        if (type === 'EDIT') {
            const updatedPermissions = role.permissions.map((permission) => {
                const permissionUpdate = permission;
                const descriptor = descriptors.find((currentDescriptor) => currentDescriptor.label === permission.descriptorName || currentDescriptor.name === permission.descriptorName);
                permissionUpdate.descriptorName = descriptor.name;

                return permissionUpdate;
            });
            setRole((updatedRole) => ({ ...updatedRole, permissions: updatedPermissions }));
        }

        if (type === 'COPY') {
            const updatedPermissions = role.permissions.map((permission) => {
                const permissionUpdate = permission;
                const descriptor = descriptors.find((currentDescriptor) => currentDescriptor.label === permission.descriptorName || currentDescriptor.name === permission.descriptorName);
                permissionUpdate.descriptorName = descriptor.name;

                return permissionUpdate;
            });
            setRole((updatedRole) => ({ ...updatedRole, permissions: updatedPermissions }));
        }

        dispatch(validateRole(role));
    }

    const handleOnChange = (label) => ({ target: { value } }) => {
        setRole((updatedRole) => ({ ...updatedRole, [label]: value }));
    };

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
                message: statusMessage,
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

    function getPermissionArray(permissionArray) {
        setRole((updatedRole) => ({ ...updatedRole, permissions: permissionArray }));
    }

    function handleFilterPermission(filteredPermissions) {
        setRole(filteredPermissions);
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
                        {copyDescription}
                    </span>
                </div>
            )}
            <TextInput
                id={ROLE_NAME_KEY}
                name={ROLE_NAME_KEY}
                label="Role Name"
                customDescription="The name of the role."
                required
                onChange={handleOnChange(ROLE_NAME_KEY)}
                value={role[ROLE_NAME_KEY]}
                errorName={ROLE_NAME_KEY}
                errorValue={error.fieldErrors[ROLE_NAME_KEY]}
            />
            <div className={classes.permissionTable}>
                <PermissionTable role={role} sendPermissionArray={getPermissionArray} handleFilterPermission={handleFilterPermission} />
            </div>
        </Modal>
    );
};

RoleModal.propTypes = {
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func,
    data: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string
    })),
    modalOptions: PropTypes.shape({
        title: PropTypes.string,
        type: PropTypes.string,
        submitText: PropTypes.string,
        copyDescription: PropTypes.string
    }),
    statusMessage: PropTypes.string,
    setStatusMessage: PropTypes.func
};

export default RoleModal;
