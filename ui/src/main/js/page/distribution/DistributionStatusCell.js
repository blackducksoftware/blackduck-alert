import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import classNames from 'classnames';

const useStyles = createUseStyles((theme) => ({
    badge: {
        padding: ['1px', '4px'],
        fontSize: '0.9em',
        textAlign: 'center',
        borderRadius: '2px'
    },
    fail: {
        backgroundColor: theme.colors.statusFailure,
        color: theme.colors.white.default
    },
    pending: {
        backgroundColor: theme.colors.statusPending,
        border: '1px solid #F2B560'
    },
    success: {
        backgroundColor: theme.colors.statusSuccess,
        color: theme.colors.white.default
    }
}));

const DistributionEditCell = ({ data }) => {
    const classes = useStyles();
    const { auditStatus } = data;

    const statusClass = classNames(classes.badge, {
        [classes.fail]: auditStatus === 'FAILURE',
        [classes.pending]: auditStatus === 'PENDING',
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
            return '\u2014'
        }
    }
    
    return (
        <div className={statusClass}>
            {getStatus(auditStatus)}
        </div>
    )
};

DistributionEditCell.propTypes = {
    data: PropTypes.shape({
        auditStatus: PropTypes.string
    })
};

export default DistributionEditCell;
