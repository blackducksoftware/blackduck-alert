import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import classNames from 'classnames';
import theme from '_theme';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    messageItem: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        columnGap: '12px'
    },
    errorMessageItem: {
        border: ['solid', '1px', theme.colors.status.error.border],
        borderLeft: 'none',
        borderRadius: '4px'
    },
    warningMessageItem: {
        border: ['solid', '1px', theme.colors.status.warning.border],
        borderLeft: 'none',
        borderRadius: '4px'
    },
    validMessageItem: {
        border: ['solid', '1px', theme.colors.status.success.border],
        borderLeft: 'none',
        borderRadius: '4px'
    },
    statusIconContainer: {
        alignSelf: 'stretch',
        padding: '12px',
        width: 'fit-content',
        borderTopLeftRadius: '4px',
        borderBottomLeftRadius: '4px'
    },
    errorIconStatus: {
        borderLeft: ['4px', 'solid', theme.colors.status.error.text],
        backgroundColor: theme.colors.status.error.background
    },
    warningIconStatus: {
        borderLeft: ['4px', 'solid', theme.colors.status.warning.text],
        backgroundColor: theme.colors.status.warning.background
    },
    validIconStatus: {
        borderLeft: ['4px', 'solid', theme.colors.status.success.text],
        backgroundColor: theme.colors.status.success.background
    },
    messageContent: {
        fontSize: '14px',
        margin: 0,
        padding: 0
    },
    messageTimestamp: {
        fontSize: '14px',
        color: theme.colors.grey.darkGrey,
        margin: [0, '12px', 0, 'auto'],
        padding: 0,
        textWrap: 'nowrap'
    }
});

const InfoMessage = ({ id, type, message, timestamp }) => {
    const classes = useStyles();

    function getIconConfig(type) {
        if (type === 'error') {
            return { icon: 'exclamation-triangle', color: theme.colors.status.error.text };
        } else if (type === 'warning') {
            return { icon: 'exclamation-triangle', color: theme.colors.status.warning.text };
        } else {
            return { icon: 'check-circle', color: theme.colors.status.success.text };
        }
    }

    function getIconClassName(type) {
        return classNames(classes.statusIconContainer, {
            [classes.errorIconStatus]: (type === 'error'),
            [classes.warningIconStatus]: (type === 'warning'),
            [classes.validIconStatus]: (type !== 'error' && type !== 'warning')
        });
    }

    function getMessageClassName(type) {
        return classNames(classes.messageItem, {
            [classes.errorMessageItem]: (type === 'error'),
            [classes.warningMessageItem]: (type === 'warning'),
            [classes.validMessageItem]: (type !== 'error' && type !== 'warning')
        });
    }

    const { icon, color } = getIconConfig(type);

    return (
        <div key={id} className={getMessageClassName(type)}>
            <div className={getIconClassName(type)}>
                <FontAwesomeIcon icon={icon} color={color} size="lg" />
            </div>
            <p className={classes.messageContent}>
                {message}
            </p>
            { timestamp && <p className={classes.messageTimestamp}>{timestamp}</p> }
        </div>
    );
};

InfoMessage.propTypes = {
    id: PropTypes.string,
    message: PropTypes.string.isRequired,
    timestamp: PropTypes.string,
    type: PropTypes.oneOf(['error', 'info', 'warning']).isRequired
};

export default InfoMessage;