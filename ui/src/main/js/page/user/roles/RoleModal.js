import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import Modal from 'common/component/modal/Modal';
import TextInput from 'common/component/input/TextInput';
import { saveRole, validateRole } from 'store/actions/roles';
import PermissionTable from './PermissionTable';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

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

// Modal Capabilities -> Create (type: create) | Update (type: edit) | Duplicate (type: copy)

const RoleModal = ({ data, isOpen, toggleModal, title, type = 'create', submitText, copiedRoleName }) => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const [role, setRole] = useState(type === 'create' ? {permissions: []} : data);

    const ROLE_NAME_KEY = 'roleName';

    const fieldErrors = useSelector(state => state.roles);
    const descriptors = useSelector(state => state.descriptors.items);
    const { saveStatus, inProgress } = useSelector(state => state.roles);

    useEffect(() => {
        if (saveStatus === 'VALIDATED' && !inProgress) { 
            handleSave();
        } 
    }, [saveStatus]);

    function handleClose() {
        toggleModal(false);
    }

    const handleOnChange = (label) => {
        return ({ target: { value } }) => {
            setRole(role => ({...role, [label]: value }));
        }
    }

    function handleSave() {
        dispatch(saveRole(role));
        handleClose();
    }

    function handleSubmit() {
        if (type === 'edit') {
            const updatedPermissions = role.permissions.map(permission => {
                const descriptor = descriptors.find((currentDescriptor) => currentDescriptor.label === permission.descriptorName || currentDescriptor.name === permission.descriptorName);
                if (descriptor) {
                    permission.descriptorName = descriptor.name;
                    return permission;
                }
            });
            setRole(role => ({...role, permissions: updatedPermissions}));
        }

        dispatch(validateRole(role));
        handleSave();
    }

    function getPermissionArray(permissionArray) {
        setRole(role => ({...role, permissions: permissionArray}));
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
                    <span className={classes.descriptor}>
                        Performing this action will create a new role by using the same settings as '{copiedRoleName}'
                    </span>
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
            <div style={{'width': '90%', 'margin': 'auto'}}>
                <PermissionTable data={role.permissions} sendPermissionArray={getPermissionArray}/>
            </div>
        </Modal>
    );
};

export default RoleModal;