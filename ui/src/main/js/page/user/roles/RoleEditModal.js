import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Modal from 'common/component/modal/Modal';
import TextInput from 'common/component/input/TextInput';
import { saveRole, validateRole } from 'store/actions/roles';
import PermissionTable from 'page/user/roles/PermissionTable';


const RoleEditModal = ({ data, isOpen, toggleModal }) => {
    const dispatch = useDispatch();
    const [role, setRole] = useState(data);

    const ROLE_NAME_KEY = 'roleName';

    const fieldErrors = useSelector(state => state.roles.error.fieldErrors);
    const descriptors = useSelector(state => state.descriptors.items);

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
        const updatedPermissions = role.permissions.map(permission => {
            const descriptor = descriptors.find((currentDescriptor) => currentDescriptor.label === permission.descriptorName || currentDescriptor.name === permission.descriptorName);
            if (descriptor) {
                permission.descriptorName = descriptor.name;
                return permission;
            }
        });
        setRole(role => ({...role, permissions: updatedPermissions}));

        dispatch(validateRole(role));
        handleSave();
    }

    function getPermissionArray(permissionData) {
        setRole(role => ({...role, permissions: permissionData}));
    }

    return (
        <Modal 
            isOpen={isOpen} 
            size="lg" 
            title="Edit Role"
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText="Save"
        >
            <TextInput
                id={ROLE_NAME_KEY}
                name={ROLE_NAME_KEY}
                label="Role Name"
                description="The name of the role."
                required
                onChange={handleOnChange('roleName')}
                value={role[ROLE_NAME_KEY]}
                errorName={ROLE_NAME_KEY}
                errorValue={fieldErrors[ROLE_NAME_KEY]}
            />
            <div style={{'width': '90%', 'margin': 'auto'}}>
                <PermissionTable data={role.permissions} sendPermissionArray={getPermissionArray} role={role} />
            </div>
        </Modal>
    );
};

export default RoleEditModal;