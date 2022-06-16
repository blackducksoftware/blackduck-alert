import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import classNames from 'classnames';

const useStyles = createUseStyles({
    alertIcon: {
        padding: '3px'
    },
    errorStatus: {
        color: '#E03C31'
    },
    warningStatus: {
        color: '#E07C05'
    },
    validStatus: {
        color: '#3B7D3C'
    }
});

const SystemMessage = ({ createdAt, content, severity, id }) => {
    const classes = useStyles();

    function getIcon(severity) {        
        return (severity === 'ERROR' || severity === 'WARNING') ? 'exclamation-triangle' : 'check-circle';
    }

    function getClassName(severity) {
        return classNames({
            [classes.alertIcon]: true,
            [classes.errorStatus]: (severity === 'ERROR'),
            [classes.warningStatus]: (severity === 'WARNING'),
            [classes.validStatus]: (severity !== 'ERROR' && severity !== 'WARNING'),
        })
    }

    return (
        <div id={id} className="messageHeader">
            <FontAwesomeIcon
                icon={getIcon(severity)}
                className={getClassName(severity)}
                size="lg"
                title={severity}
            />
            <span className="messageDate">{createdAt}</span>
            <div>{content}</div>
        </div>
    );

}

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
