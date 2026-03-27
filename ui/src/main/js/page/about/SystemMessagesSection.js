import React from 'react';
import { createUseStyles } from 'react-jss';
import { useSelector } from 'react-redux';
import classNames from 'classnames';

import SectionCard from 'common/component/SectionCard';
import theme from '_theme';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    systemMessageList: {
        listStyleType: 'none',
        padding: 0,
        margin: ['28px', 0, 0, 0]
    },
    messageItem: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: '8px',
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

// TODO: Dispatch this action in parent and pass it to this component and footer component
const SystemMessagesSection = () => {
    const classes = useStyles();
    const { fetching, latestMessages } = useSelector((state) => state.system);

    function getIconConfig(severity) {
        if (severity === 'ERROR') {
            return { icon: 'exclamation-triangle', color: theme.colors.status.error.text };
        } else if (severity === 'WARNING') {
            return { icon: 'exclamation-triangle', color: theme.colors.status.warning.text };
        } else {
            return { icon: 'check-circle', color: theme.colors.status.success.text };
        }
    }

    function getIconClassName(severity) {
        return classNames(classes.statusIconContainer, {
            [classes.errorIconStatus]: (severity === 'ERROR'),
            [classes.warningIconStatus]: (severity === 'WARNING'),
            [classes.validIconStatus]: (severity !== 'ERROR' && severity !== 'WARNING')
        });
    }

    function getMessageClassName(severity) {
        return classNames(classes.messageItem, {
            [classes.errorMessageItem]: (severity === 'ERROR'),
            [classes.warningMessageItem]: (severity === 'WARNING'),
            [classes.validMessageItem]: (severity !== 'ERROR' && severity !== 'WARNING')
        });
    }

    return (
        <SectionCard title="System Messages" icon="bullhorn">
            {fetching && <p className={classes.messageContent}>Loading system messages...</p>}

            {!fetching && latestMessages && latestMessages.length > 0 ? (
                <ul className={classes.systemMessageList}>
                    {latestMessages.map((message) => {
                        const { icon, color } = getIconConfig(message.severity);
                        return (
                            <li key={message.createdAt} className={getMessageClassName(message.severity)}>
                                <div className={getIconClassName(message.severity)}>
                                    <FontAwesomeIcon icon={icon} color={color} size="lg" />
                                </div>
                                <p className={classes.messageContent}>
                                    {message.content}
                                </p>
                                <p className={classes.messageTimestamp}>
                                    {message.createdAt}
                                </p>
                            </li>
                        );
                    })}
                </ul>
            ) : (
                <p className={classes.messageContent}>No system messages to display.</p>
            )}
        </SectionCard>
    );
};

export default SystemMessagesSection;
