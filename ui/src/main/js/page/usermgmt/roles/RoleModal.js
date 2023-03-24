import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import Modal from 'common/component/modal/Modal';
import TextInput from 'common/component/input/TextInput';
import { saveRole, validateRole } from 'store/actions/roles';
import PermissionTable from './PermissionTable';

const useStyles = createUseStyles({
    descriptorContainer: {
        display: 'flex',
        alignItems: 'center',
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

// Modal Capabilities -> Create (type: create) | Update (type: edit) | Duplicate (type: copy)
const RoleModal = ({ data, isOpen, toggleModal, title, type = 'create', submitText, copiedRoleName }) => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const copyWarning = `Performing this action will create a new role by using the same settings as '${copiedRoleName}'`;
    const [role, setRole] = useState(type === 'create' ? { permissions: [] } : data);

    const ROLE_NAME_KEY = 'roleName';

    const descriptors = useSelector((state) => state.descriptors.items);
    const { saveStatus, inProgress, fieldErrors } = useSelector((state) => state.roles);

    function handleClose() {
        toggleModal(false);
    }

    function handleSave() {
        dispatch(saveRole(role));
        handleClose();
    }

    function handleSubmit() {
        if (type === 'edit') {
            const updatedPermissions = role.permissions.map((permission) => {
                const permissionUpdate = permission;
                const descriptor = descriptors.find((currentDescriptor) => currentDescriptor.label === permission.descriptorName || currentDescriptor.name === permission.descriptorName);
                permissionUpdate.descriptorName = descriptor.name;

                return permissionUpdate;
            });
            setRole((updatedRole) => ({ ...updatedRole, permissions: updatedPermissions }));
        }

        dispatch(validateRole(role));
        handleSave();
    }

    const handleOnChange = (label) => ({ target: { value } }) => {
        setRole((updatedRole) => ({ ...updatedRole, [label]: value }));
    };

    useEffect(() => {
        if (saveStatus === 'VALIDATED' && !inProgress) {
            handleSave();
        }
    }, [saveStatus]);

    function getPermissionArray(permissionArray) {
        setRole((updatedRole) => ({ ...updatedRole, permissions: permissionArray }));
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
        >
            { (type === 'copy' && copiedRoleName) ? (
                <div className={classes.descriptorContainer}>
                    <FontAwesomeIcon icon="exclamation-circle" size="2x" />
                    <span className={classes.descriptor}>{copyWarning}</span>
                </div>
            ) : null}
            <TextInput
                id={ROLE_NAME_KEY}
                name={ROLE_NAME_KEY}
                label="Role Name"
                description="The name of the role."
                required
                onChange={handleOnChange(ROLE_NAME_KEY)}
                value={role[ROLE_NAME_KEY]}
                errorName={ROLE_NAME_KEY}
                errorValue={fieldErrors[ROLE_NAME_KEY]}
            />
            <div className={classes.permissionTable}>
                <PermissionTable data={role.permissions} sendPermissionArray={getPermissionArray} role={role} />
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
    title: PropTypes.string,
    type: PropTypes.string,
    submitText: PropTypes.string,
    copiedRoleName: PropTypes.string
};

export default RoleModal;
