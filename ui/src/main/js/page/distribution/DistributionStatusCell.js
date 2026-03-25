import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import classNames from 'classnames';

const useStyles = createUseStyles((theme) => ({
    badge: {
        display: 'flex',
        alignItems: 'center',
        columnGap: '6px',
        borderRadius: '8px',
        width: 'fit-content',
        padding: '0 10px',
        margin: 'auto',
        fontSize: '13px'
    },
    success: {
        color: theme.colors.status.success.default,
        border: `1px solid ${theme.colors.status.success.border}`,
        backgroundColor: theme.colors.status.success.background
    },
    fail: {
        color: theme.colors.status.error.default,
        border: `1px solid ${theme.colors.status.error.border}`,
        backgroundColor: theme.colors.status.error.background
    },
    pending: {
        color: theme.colors.status.warning.default,
        border: `1px solid ${theme.colors.status.warning.border}`,
        backgroundColor: theme.colors.status.warning.background
    }
}));

const DistributionStatusCell = ({ data }) => {
    const classes = useStyles();
    const { auditStatus } = data;

    const statusClass = classNames(classes.badge, {
        [classes.fail]: auditStatus === 'FAILURE',
        [classes.pending]: auditStatus !== 'SUCCESS' && auditStatus !== 'FAILURE',
        [classes.success]: auditStatus === 'SUCCESS',
    });
    
    function getStatus(status) {
        if (status === 'FAILURE') {
            return 'Failure'
        } else if (status === 'SUCCESS') {
            return 'Success'
        } else if (status === 'PENDING') {
            return 'Pending'
        } else {
            return 'Pending'
        }
    }
    
    return (
        <div className={statusClass}>
            {getStatus(auditStatus)}
        </div>
    )
};

DistributionStatusCell.propTypes = {
    data: PropTypes.shape({
        auditStatus: PropTypes.string
    })
};

export default DistributionStatusCell;
