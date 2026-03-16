import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

// TODO: Rework additional info, should accept more than lastUpdated
// TODO: Last updated has perfect ability to become LabelValuePair reusable component

const useStyles = createUseStyles((theme) => ({
    pageHeader: {
        padding: '30px',
        backgroundColor: theme.colors.white.default,
        width: '100%',
        alignItems: 'start',
        display: 'flex',
        flexDirection: 'row',
        columnGap: '12px',
        borderBottom: `solid 1px ${theme.colors.grey.lighterGrey}`
    },
    logoContainer: {
        gridArea: 'logo',
        height: '50px',
        minWidth: '50px',
        backgroundColor: theme.colors.white.default,
        border: 'solid 1px #ddd',
        color: theme.colors.white.default,
        borderRadius: '14px',
        padding: '8px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'oklch(55.8% 0.288 302.321)',
        boxShadow: `0 1px 3px 0 #0000001a, 0 1px 2px -1px #0000001a`
    },
    logoGlyph: {
        fontSize: '20px'
    },
    titleContainer: {
        display: 'flex',
        flexDirection: 'column'
    },
    title: {
        paddingBottom: 0,
        margin: ['4px', 0, '10px'],
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
        gridArea: 'additionalInfo',
        alignSelf: 'start',
        '&:empty + $title': {
            gridColumnEnd: 'additionalInfo'
        }
    }
}));

const PageHeader = ({ title, description, icon, lastUpdated }) => {
    const classes = useStyles();

    return (
        <header className={classes.pageHeader}>
            {icon ? (
                <div className={classes.logoContainer}>
                    <FontAwesomeIcon icon={icon} className={classes.logoGlyph} />
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

            {/* <div className={classes.additionalInfo}>
                {lastUpdated
                && (
                    <div>
                        <label className="text-right">Last Updated:</label>
                        <div className="d-inline-flex p-2">{lastUpdated}</div>
                    </div>
                )}
            </div> */}
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
