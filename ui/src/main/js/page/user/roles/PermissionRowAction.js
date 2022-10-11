import React, { useCallback, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useDispatch, useSelector } from 'react-redux';
import { saveRole, validateRole } from 'store/actions/roles';

const useStyles = createUseStyles({
    deletePermissionBtn: {
        borderRadius: '3px',
        border: 'none',
        transitionDuration: '0.3s',
        backgroundColor: '#bdbdbd',
        '&:hover': {
            cursor: 'pointer',
            backgroundColor: '#b0b0b0',
        }
    },
    deleteContainer: {
        display: 'flex',
        flexDirection: 'column',
        width: 'fit-content',
        justifyContent: 'flex-end',
        marginLeft: 'auto',
        marginRight: 0
    },
    confirmMessage: {
        margin: 'auto'
    },
    confirmContainer: {
        display: 'flex',
        fontSize: '11px',
        columnGap: '5px',
    },
    confirmOptionBtn: {
        border: 'none',
        borderRadius: '4px',
        padding: ['2px', '7px'],
        backgroundColor: '#E4FEE0',
        transitionDuration: '0.3s',
        '&:hover': {
            cursor: 'pointer',
            backgroundColor: '#67AD5B',
            color: 'white'
        }
    },
    cancelOptionBtn: {
        border: 'none',
        borderRadius: '4px',
        padding: ['2px', '7px'],
        backgroundColor: '#F9DEDE',
        transitionDuration: '0.3s',
        '&:hover': {
            cursor: 'pointer',
            backgroundColor: '#E15241',
            color: 'white'
        }
    }
})

const PermissionRowAction = ({ data, settings}) => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const { parentData } = settings;
    const filterData = parentData?.permissions.filter(permission => {
        return !Object.is(permission, data)
    });

    const descriptors = useSelector(state => state.descriptors.items);
    const roles = useSelector(state => state.roles);

    const [showDelete, setShowDelete] = useState(false);
    const [roleData, setRoleData] = useState(parentData);

    useEffect(() => {
        if (roles.saveStatus === 'VALIDATED' && !roles.inProgress) { 
            handleSave();
        } 
    }, [roles.saveStatus]);

    function handleDeletePermission() {
        roleData.permissions = filterData;

        const updatedPermissions = roleData.permissions.map(permission => {
            const descriptor = descriptors.find((currentDescriptor) => currentDescriptor.label === permission.descriptorName || currentDescriptor.name === permission.descriptorName);
            if (descriptor) {
                permission.descriptorName = descriptor.name;
                return permission;
            }
        });
        setRoleData(role => ({...role, permissions: updatedPermissions}));
        
        dispatch(validateRole(roleData));
    }

    function handleSave() {
        dispatch(saveRole(roleData));
    }

    return (
        <>
            {!showDelete ? (
                <button className={classes.deletePermissionBtn} onClick={() => setShowDelete(true)}>
                    <FontAwesomeIcon icon="trash"/>
                </button>
            ) : (
                <div className={classes.deleteContainer}>
                    <span className={classes.confirmMessage}>Delete Permission?</span>
                    <span className={classes.confirmContainer}>
                        <button className={classes.confirmOptionBtn} onClick={handleDeletePermission}>
                            Confirm
                            <FontAwesomeIcon icon="check" />
                        </button>
                        <div> | </div>
                        <button className={classes.cancelOptionBtn} onClick={() => setShowDelete(false)}>
                            Cancel
                            <FontAwesomeIcon icon="times" />
                        </button>
                    </span>
                </div>
            )}
        </>
    )
}

export default PermissionRowAction;