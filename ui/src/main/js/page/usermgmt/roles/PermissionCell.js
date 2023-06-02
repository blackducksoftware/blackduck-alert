import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { OverlayTrigger, Popover } from 'react-bootstrap';

const useStyles = createUseStyles({
    permissionDisplay: {
        borderRadius: '4px',
        padding: ['2px', '10px'],
        backgroundColor: '#D3D3D3',
        width: 'min-content'
    },
    multiPermissionDisplay: {
        borderRadius: '4px',
        padding: ['2px', '10px'],
        backgroundColor: '#D3D3D3',
        width: 'min-content',
        '&:hover': {
            cursor: 'pointer',
            backgroundColor: '#c6c6c6'
        }
    },
    emptyPermission: {
        fontStyle: 'italic'
    },
    popoverPermissions: {
        padding: '5px',
        fontSize: '14px'
    }
});

const permissionDisplay = {
    create: 'Create',
    delete: 'Delete',
    read: 'Read',
    write: 'Write',
    execute: 'Execute',
    uploadRead: 'Upload Read',
    uploadWrite: 'Upload Write',
    uploadDelete: 'Upload Delete'
};

const PermissionCell = ({ data }) => {
    const classes = useStyles();

    function getPermissions(permissionData) {
        const permissions = [];

        Object.entries(permissionData).forEach((permission) => {
            if (permission[1] === true) {
                permissions.push(permission[0]);
            }
        });

        return permissions.map((permission) => permissionDisplay[permission]);
    }

    const permissionsData = getPermissions(data);
    const count = permissionsData.length;

    const popoverPermissions = (
        <Popover id="permission-popover-trigger-hover-focus" title="Permissions">
            <div className={classes.popoverPermissions}>
                {permissionsData.join(', ')}
            </div>
        </Popover>
    );

    if (count === 1) {
        return (
            <span className={classes.permissionDisplay}>
                {`${permissionsData[0]}`}
            </span>
        );
    }

    if (count === 0) {
        return (
            <span className={classes.emptyPermission}>
                No permissions set.
            </span>
        );
    }

    return (
        <>
            <OverlayTrigger trigger={['hover', 'focus']} placement="right" overlay={popoverPermissions}>
                <span className={classes.multiPermissionDisplay}>
                    {`${permissionsData[0]} and ${count - 1} more...`}
                </span>
            </OverlayTrigger>
        </>
    );
};

PermissionCell.propTypes = {
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
    })
};

export default PermissionCell;
