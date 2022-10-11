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
})

const RoleCopyModal = ({ data, isOpen, toggleModal, copiedRoleName }) => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const [role, setRole] = useState(data);

    const ROLE_NAME_KEY = 'roleName';

    const fieldErrors = useSelector(state => state.roles.error.fieldErrors);
    const descriptors = useSelector(state => state.descriptors.items);
    const { saveStatus, inProgress } = useSelector(state => state.roles);

    useEffect(() => {
        if ( saveStatus === 'VALIDATED' && !inProgress) { 
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
        const updatedPermissions = role.permissions.map(permission => {
            const descriptor = descriptors.find((currentDescriptor) => currentDescriptor.label === permission.descriptorName || currentDescriptor.name === permission.descriptorName);
            if (descriptor) {
                permission.descriptorName = descriptor.name;
                return permission;
            }
        });
        setRole(role => ({...role, permissions: updatedPermissions}))

        dispatch(validateRole(role));
    }

    function getPermissionArray(datar) {
        setRole(role => ({...role, permissions: datar}));
    }

    return (
        <Modal 
            isOpen={isOpen} 
            size="lg" 
            title="Copy Role"
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText="Create"
        >
            <div className={classes.descriptorContainer}>
                <FontAwesomeIcon icon="exclamation-circle" size="2x" />
                <span className={classes.descriptor}>
                    Performing this action will create a new role by using the same settings as '{copiedRoleName}'
                </span>
            </div>
            <TextInput
                id={ROLE_NAME_KEY}
                name={ROLE_NAME_KEY}
                label="Role Name"
                description="The name of the role."
                required
                onChange={handleOnChange('roleName')}
                value={data.roleName}
                errorName={ROLE_NAME_KEY}
                errorValue={fieldErrors[ROLE_NAME_KEY]}
            />
            <div style={{'width': '90%', 'margin': 'auto'}}>
                <PermissionTable data={data.permissions} sendPermissionArray={getPermissionArray}/>
            </div>
        </Modal>
    );
};

export default RoleCopyModal;