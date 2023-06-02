import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    notification: {
        width: '90%',
        margin: [0, 'auto', '20px'],
        padding: ['0.75rem', '1.25rem'],
        borderRadius: '0.25rem'
    },
    success: {
        color: '#155724',
        backgroundColor: '#d4edda',
        border: ['1px', 'solid', '#c3e6cb']
    },
    error: {
        color: '#721c24',
        backgroundColor: '#f8d7da',
        border: ['1px', 'solid', '#f5c6cb']
    },
    title: {
        fontWeight: 'bold'
    }
});

const Notification = ({ notification }) => {
    const classes = useStyles();
    const { message, title, type } = notification;

    const notificationClass = classNames(classes.notification, {
        [classes.error]: type === 'error',
        [classes.success]: type === 'success'
    });

    return (
        <div className={notificationClass}>
            <div className={classes.title}>
                {title}
            </div>
            {message && (
                <div>
                    {message}
                </div>
            )}
        </div>
    );
};

Notification.propTypes = {
    notification: PropTypes.shape({
        message: PropTypes.string,
        title: PropTypes.string,
        type: PropTypes.oneOf(['error', 'success'])
    })
};

export default Notification;
