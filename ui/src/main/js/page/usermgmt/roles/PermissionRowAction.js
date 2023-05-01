import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles((theme) => ({
    deletePermissionBtn: {
        borderRadius: '3px',
        border: 'none',
        transitionDuration: '0.3s',
        backgroundColor: '#bdbdbd',
        '&:hover': {
            cursor: 'pointer',
            backgroundColor: '#b0b0b0'
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
        columnGap: '5px'
    },
    confirmOptionBtn: {
        border: 'none',
        borderRadius: '4px',
        padding: ['2px', '7px'],
        backgroundColor: '#E4FEE0',
        transitionDuration: '0.3s',
        display: 'flex',
        alignItems: 'center',
        columnGap: '6px',
        '&:hover': {
            cursor: 'pointer',
            backgroundColor: '#67AD5B',
            color: theme.colors.white.default
        }
    },
    cancelOptionBtn: {
        border: 'none',
        borderRadius: '4px',
        padding: ['2px', '7px'],
        backgroundColor: '#F9DEDE',
        transitionDuration: '0.3s',
        display: 'flex',
        alignItems: 'center',
        columnGap: '6px',
        '&:hover': {
            cursor: 'pointer',
            backgroundColor: '#E15241',
            color: theme.colors.white.default
        }
    }
}));

const PermissionRowAction = ({ data, settings, customCallback }) => {
    const classes = useStyles();
    const { permissionData, role } = settings;
    const descriptors = useSelector((state) => state.descriptors.items);

    const [showDelete, setShowDelete] = useState(false);
    const [roleData, setRoleData] = useState(role);

    function handleDeletePermission() {
        const { context: selectedContext, descriptorName: selectedDescriptor } = data;
        const updatedPermissions = permissionData
            .filter((permission) => !(permission.context === selectedContext && permission.descriptorName === selectedDescriptor))
            .map((permission) => {
                const permissionUpdate = permission;
                const descriptor = descriptors.find((currentDescriptor) => currentDescriptor.label === permission.descriptorName || currentDescriptor.name === permission.descriptorName);
                permissionUpdate.descriptorName = descriptor.name;

                return permissionUpdate;
            });

        setRoleData((updatedRole) => ({ ...updatedRole, permissions: updatedPermissions }));
        setShowDelete(false);
    }

    useEffect(() => {
        customCallback(roleData);
    }, [roleData]);

    return (
        <>
            {!showDelete ? (
                <button className={classes.deletePermissionBtn} onClick={() => setShowDelete(true)} type="button">
                    <FontAwesomeIcon icon="trash" />
                </button>
            ) : (
                <div className={classes.deleteContainer}>
                    <span className={classes.confirmMessage}>Delete Permission?</span>
                    <span className={classes.confirmContainer}>
                        <button className={classes.confirmOptionBtn} onClick={handleDeletePermission} type="button">
                            <div className={classes.editBtn}>Confirm</div>
                            <FontAwesomeIcon icon="check" />
                        </button>
                        <div> | </div>
                        <button className={classes.cancelOptionBtn} onClick={() => setShowDelete(false)} type="button">
                            Cancel
                            <FontAwesomeIcon icon="times" />
                        </button>
                    </span>
                </div>
            )}
        </>
    );
};

PermissionRowAction.propTypes = {
    data: PropTypes.shape({
        context: PropTypes.string,
        create: PropTypes.bool,
        delete: PropTypes.bool,
        descriptorName: PropTypes.string,
        execute: PropTypes.bool,
        read: PropTypes.bool,
        uploadDelete: PropTypes.bool,
        uploadRead: PropTypes.bool,
        uploadWrite: PropTypes.bool,
        write: PropTypes.bool
    }),
    settings: PropTypes.shape({
        alignment: PropTypes.string,
        permissionData: PropTypes.arrayOf(PropTypes.shape({
            context: PropTypes.string,
            create: PropTypes.bool,
            delete: PropTypes.bool,
            descriptorName: PropTypes.string,
            execute: PropTypes.bool,
            read: PropTypes.bool,
            uploadDelete: PropTypes.bool,
            uploadRead: PropTypes.bool,
            uploadWrite: PropTypes.bool,
            write: PropTypes.bool
        })),
        role: PropTypes.shape({
            id: PropTypes.string,
            roleName: PropTypes.string,
            permissionData: PropTypes.arrayOf(PropTypes.shape({
                context: PropTypes.string,
                create: PropTypes.bool,
                delete: PropTypes.bool,
                descriptorName: PropTypes.string,
                execute: PropTypes.bool,
                read: PropTypes.bool,
                uploadDelete: PropTypes.bool,
                uploadRead: PropTypes.bool,
                uploadWrite: PropTypes.bool,
                write: PropTypes.bool
            }))
        })
    }),
    customCallback: PropTypes.func
};

export default PermissionRowAction;
