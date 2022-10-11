import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Modal from 'common/component/modal/Modal';
import TextInput from 'common/component/input/TextInput';
import { saveRole, validateRole } from 'store/actions/roles';
import PermissionTable from './PermissionTable';

const RoleCreateModal = ({ data, isOpen, toggleModal }) => {
    const dispatch = useDispatch();
    const [newRole, setNewRole] = useState({permissions: []});

    const ROLE_NAME_KEY = 'roleName';

    const fieldErrors = useSelector(state => state.roles.error.fieldErrors);
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
            setNewRole(role => ({...role, [label]: value }));
        }
    }

    function handleSave() {
        dispatch(saveRole(newRole));
        handleClose();
    }

    function handleSubmit() {
        dispatch(validateRole(newRole));
    }

    function getPermissionArray(permissionArray) {
        setNewRole(role => ({...role, permissions: permissionArray}));
    }

    return (
        <Modal 
            isOpen={isOpen} 
            size="lg" 
            title="Create Role"
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText="Create"
        >
            <TextInput
                id={ROLE_NAME_KEY}
                name={ROLE_NAME_KEY}
                label="Role Name"
                description="The name of the role."
                required
                onChange={handleOnChange(ROLE_NAME_KEY)}
                value={newRole[ROLE_NAME_KEY]}
                errorName={ROLE_NAME_KEY}
                errorValue={fieldErrors[ROLE_NAME_KEY]}
            />
            <div style={{'width': '90%', 'margin': 'auto'}}>
                <PermissionTable data={newRole.permissions} sendPermissionArray={getPermissionArray}/>
            </div>
        </Modal>
    );
};

export default RoleCreateModal;