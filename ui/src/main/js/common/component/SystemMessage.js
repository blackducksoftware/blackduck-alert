import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import classNames from 'classnames';

const useStyles = createUseStyles((theme) => ({
    alertIcon: {
        padding: '3px'
    },
    errorStatus: {
        color: theme.colors.status.error.text
    },
    warningStatus: {
        color: theme.colors.status.warning.text
    },
    validStatus: {
        color: theme.colors.status.success.text
    },
    messageHeader: {
        width: '100%',
        borderTop: `1px solid ${theme.colors.darkGreyAlertColor}`
    },
    messageDate: {
        marginLeft: '5px',
        marginRight: '5px',
        fontWeight: 'bold'
    }
}));

const SystemMessage = ({ createdAt, content, severity, id }) => {
    const classes = useStyles();

    function getIcon(messageSeverity) {
        return (messageSeverity === 'ERROR' || messageSeverity === 'WARNING') ? 'exclamation-triangle' : 'check-circle';
    }

    function getClassName(messageSeverity) {
        return classNames(classes.alertIcon, {
            [classes.errorStatus]: (messageSeverity === 'ERROR'),
            [classes.warningStatus]: (messageSeverity === 'WARNING'),
            [classes.validStatus]: (messageSeverity !== 'ERROR' && messageSeverity !== 'WARNING')
        });
    }

    return (
        <div id={id} className={classes.messageHeader}>
            <FontAwesomeIcon
                icon={getIcon(severity)}
                className={getClassName(severity)}
                size="lg"
                title={severity}
            />
            <span className={classes.messageDate}>{createdAt}</span>
            <div>{content}</div>
        </div>
    );
};

SystemMessage.propTypes = {
    id: PropTypes.string,
    severity: PropTypes.string,
    createdAt: PropTypes.string,
    content: PropTypes.string
};

SystemMessage.defaultProps = {
    id: 'systemMessageId',
    severity: 'INFO',
    createdAt: '',
    content: ''
};

export default SystemMessage;
