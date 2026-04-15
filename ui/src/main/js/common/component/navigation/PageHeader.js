import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles((theme) => ({
    pageHeader: {
        padding: '30px',
        backgroundColor: theme.colors.white.default,
        width: '100%',
        display: 'flex',
        flexDirection: 'row',
        columnGap: '12px',
        borderBottom: `solid 1px ${theme.colors.grey.lighterGrey}`
    },
    logoContainer: {
        height: '65px',
        minWidth: '65px',
        color: theme.colors.grey.darkerGrey,
        borderRadius: '14px',
        padding: '8px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: theme.colors.purple.darkerPurple,
        boxShadow: '0 6px 12px rgba(0, 0, 0, 0.175)'
    },
    logoGlyph: {
        fontSize: '30px'
    },
    titleContainer: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between'
    },
    title: {
        padding: 0,
        marginBottom: '10px',
        position: 'relative',
        fontWeight: '900',
        borderBottom: 'none',
        fontSize: '26px'
    },
    description: {
        fontSize: '15px',
        color: theme.colors.grey.darkGrey
    },
    additionalInfo: {
        alignSelf: 'center',
        marginLeft: 'auto',
        paddingLeft: '20px',
        minWidth: 'fit-content'
    },
    lastUpdatedTitle: {
        fontSize: '13px',
        color: theme.colors.grey.darkGrey,
        marginRight: '4px'
    },
    lastUpdatedBadge: {
        fontSize: '13px',
        color: theme.colors.grey.darkerGrey,
        backgroundColor: theme.colors.grey.lighterGrey,
        padding: '4px 12px',
        borderRadius: '4px'
    }
}));

const PageHeader = ({ title, description, icon, lastUpdated }) => {
    const classes = useStyles();

    return (
        <header className={classes.pageHeader}>
            {icon ? (
                <div className={classes.logoContainer}>
                    <FontAwesomeIcon icon={icon} className={classes.logoGlyph} color="white" />
                </div>
            ) : null}
            <div className={classes.titleContainer}>
                <h1 className={classes.title}>
                    {title}
                </h1>
                <div className={classes.description}>
                    {description}
                </div>
            </div>

            <div className={classes.additionalInfo}>
                {lastUpdated && (
                    <div>
                        <div className={classes.lastUpdatedTitle}>Last Updated:</div>
                        <div className={classes.lastUpdatedBadge}>{lastUpdated}</div>
                    </div>
                )}
            </div>
        </header>
    );
};

PageHeader.propTypes = {
    title: PropTypes.string.isRequired,
    description: PropTypes.string,
    icon: PropTypes.oneOfType([PropTypes.string, PropTypes.array]),
    lastUpdated: PropTypes.string
};

PageHeader.defaultProps = {
    description: '',
    lastUpdated: null
};

export default PageHeader;