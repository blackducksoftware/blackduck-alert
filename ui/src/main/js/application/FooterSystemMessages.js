import React, { useState, useEffect, useRef } from 'react';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';
import { useDispatch, useSelector } from 'react-redux';
import { Overlay, Popover, PopoverBody, PopoverHeader } from 'react-bootstrap';
import { getLatestMessages } from 'store/actions/system';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import SystemMessage from 'common/component/SystemMessage';

const useStyles = createUseStyles((theme) => ({
    statusIcon: {
        padding: '3px'
    },
    '@keyframes errorBlinker': {
        '50%': {
            opacity: 0
        }
    },
    errorStatus: {
        color: theme.colors.status.error.text,
        animation: '$errorBlinker 3s linear infinite'
    },
    warningStatus: {
        color: theme.colors.status.warning.text
    },
    validStatus: {
        color: theme.colors.status.success.text
    },
    popoverButton: {
        background: 'none',
        border: 'none',
        padding: 0
    },
    statusPopover: {
        float: 'right',
        border: '1px solid transparent',
        fontSize: '18px',
        marginLeft: '5px',

        '&:hover': {
            cursor: 'pointer'
        }
    },
    popoverContainer: {
        width: '50%',
        zIndex: 1000000
    }
}));

function containsSeverity(messages, severity) {
    return messages?.some((message) => message.severity === severity) ?? false;
}

const FooterSystemMessages = () => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const [showOverlay, setShowOverlay] = useState(false);
    const targetRef = useRef(null);
    const { latestMessages } = useSelector((state) => state.system);
    const hasErrorMessage = containsSeverity(latestMessages, 'ERROR');
    const hasWarningMessage = containsSeverity(latestMessages, 'WARNING');

    useEffect(() => {
        dispatch(getLatestMessages());
    }, []);

    useEffect(() => {
        setShowOverlay(hasErrorMessage);
    }, [hasErrorMessage]);

    const iconClass = classNames(classes.statusIcon, {
        [classes.errorStatus]: hasErrorMessage,
        [classes.warningStatus]: !hasErrorMessage && hasWarningMessage,
        [classes.validStatus]: !hasErrorMessage && !hasWarningMessage
    });

    const createMessageList = () => {
        if (latestMessages && latestMessages.length > 0) {
            return latestMessages.map((message) => {
                const messageId = `system_message_${message.createdAt}`;
                return (
                    <SystemMessage
                        key={messageId}
                        id={messageId}
                        createdAt={message.createdAt}
                        content={message.content}
                        severity={message.severity}
                    />
                );
            });
        }
        return null;
    };

    const reload = () => {
        dispatch(getLatestMessages());
    };

    const handleOverlayButton = () => {
        if (!showOverlay) {
            reload();
        }

        setShowOverlay(!showOverlay);
    };

    return (
        <div id="about-FooterSystemMessages-status" className={classes.statusPopover}>
            <button
                className={classes.popoverButton}
                type="button"
                ref={targetRef}
                onClick={handleOverlayButton}
            >
                <div className={iconClass}>
                    <FontAwesomeIcon
                        icon={hasErrorMessage || hasWarningMessage ? 'exclamation-triangle' : 'check-circle'}
                        size="lg"
                    />
                </div>
            </button>
            <Overlay
                rootClose
                show={showOverlay}
                onHide={() => {
                    setShowOverlay(false);
                }}
                placement="top"
                target={targetRef.current}
            >
                <Popover id="system-errors-popover" className={classes.popoverContainer}>
                    <PopoverHeader>System Messages</PopoverHeader>
                    <PopoverBody>
                        {createMessageList()}
                    </PopoverBody>
                </Popover>
            </Overlay>
        </div>
    );
};

export default FooterSystemMessages;
