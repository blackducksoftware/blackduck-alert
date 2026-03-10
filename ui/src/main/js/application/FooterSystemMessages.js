import React, { useState, useEffect, useRef } from 'react';
import ReactDOM from 'react-dom';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';
import { useDispatch, useSelector } from 'react-redux';
import { Overlay, Popover, PopoverBody, PopoverHeader } from 'react-bootstrap';
import { getAboutInfo } from 'store/actions/about';
import { getLatestMessages } from 'store/actions/system';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import SystemMessage from 'common/component/SystemMessage';


const useStyles = createUseStyles((theme) => ({
    statusIcon: {
        padding: '3px'
    },
    '@keyframes errorBlinker': {
        '50%':{
            opacity: 0
        }
    },
    errorStatus: {
        color: theme.colors.red.lightRed,
        animation: `$errorBlinker 3s linear infinite`
    },
    warningStatus: {
        color: theme.colors.warning
    },
    validStatus: {
        color: theme.colors.green.darkGreen
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
    popoverContainer:{
        width: '50%',
        zIndex: 1000000
    }
}));

const FooterSystemMessages = () => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const [showOverlay, setShowOverlay] = useState(false);
    const [hideOverlayByUser, setHideOverlayByUser] = useState(false);
    const targetRef = useRef(null);
    const { latestMessages, fetching } = useSelector((state) => state.system);
    
    useEffect(() => {
        dispatch(getLatestMessages());
    }, []);

     useEffect(() => {
        if (fetching === false) {
            const showOverlayValue = hasErrorMessages(latestMessages);
            if (!hideOverlayByUser) {
                setShowOverlay(showOverlayValue);
            }
        }

    }, [fetching]);

    const containsSeverity = (messages, severity) => {
        if (messages && messages.length > 0) {
            if (messages.find((message) => message.severity === severity)) {
                return true;
            }
            return false;
        }
        return false;
    };

    const hasErrorMessages = (messages) => {
        return containsSeverity(messages, 'ERROR');
    };

    const hasWarningMessages = (messages) => {
        return containsSeverity(messages, 'WARNING');
    };

    const getFontAwesomeIcon = () => {
        if (hasErrorMessages(latestMessages) || hasWarningMessages(latestMessages)) {
            return 'exclamation-triangle';
        }
        return 'check-circle';
    };

    const iconClass = classNames(classes.statusIcon, {
        [classes.errorStatus]: hasErrorMessages(latestMessages),
        [classes.warningStatus]: !hasErrorMessages(latestMessages) && hasWarningMessages(latestMessages),
        [classes.validStatus]: !hasErrorMessages(latestMessages) && !hasWarningMessages(latestMessages)
    });

    const createMessageList = () => {
        if (latestMessages && latestMessages.length > 0) {
            return latestMessages.map((message) => {
                const itemKey = `system_message_${message.createdAt}`;
                return (<SystemMessage key={itemKey} createdAt={message.createdAt} content={message.content} severity={message.severity} />);
            });
        }
        return null;
    };

    const handleOverlayButton = () => {
        setShowOverlay(!showOverlay);
        setHideOverlayByUser(!hideOverlayByUser);
    };
    
    const reload = () => {
        getAboutInfo();
        getLatestMessages();
    };
    
    return (
        <div id="about-FooterSystemMessages-status" className={classes.statusPopover}>
            <div
                ref={targetRef}
                onClick={handleOverlayButton}
            >
                <div className={iconClass}>
                    <FontAwesomeIcon icon={getFontAwesomeIcon()} size="lg" />
                </div>
            </div>
            <Overlay
                rootClose
                show={showOverlay}
                onHide={() => {
                    setShowOverlay(false);
                    setHideOverlayByUser(true);
                }}
                placement="top"
                target={() => ReactDOM.findDOMNode(targetRef.current)}
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
}

export default FooterSystemMessages;
