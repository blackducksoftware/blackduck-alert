import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

// TODO: Rework additional info, should accept more than lastUpdated
// TODO: Last updated has perfect ability to become LabelValuePair reusable component

const useStyles = createUseStyles((theme) => ({
    pageHeader: {
        width: '100%',
        alignItems: 'center',
        display: 'grid',
        gridTemplateAreas: `
            "logo title  additionalInfo"
            "bottom bottom bottom"
            "separator separator separator"
        `,
        gridTemplateColumns: 'auto 1fr auto',
        padding: '8px'
    },
    logoContainer: {
        gridArea: 'logo',
        height: '50px',
        width: '50px',
        backgroundColor: theme.colors.white.default,
        border: 'solid 1px #ddd',
        color: '#24256',
        borderRadius: '50%',
        padding: '8px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        marginRight: '8px'
    },
    logoGlyph: {
        opacity: '0.5',
        width: '100%',
        height: '100%'
    },
    title: {
        gridArea: 'title',

        '& > h1': {
            margin: 0
        },

        '& > small': {
            fontSize: '1rem',
            lineHeight: 1
        }
    },
    additionalInfo: {
        gridArea: 'additionalInfo',
        alignSelf: 'start',
        '&:empty + $title': {
            gridColumnEnd: 'additionalInfo'
        }
    },
    bottom: {
        gridArea: 'bottom',
        padding: '5px'
    },
    headerSeperator: {
        gridArea: 'separator',
        borderBottom: `solid 1px ${theme.colors.grey.default}`,
        margin: '10px 0',
        width: '100%'
    }
}));

const PageHeader = ({ title, description, icon, lastUpdated }) => {
    const classes = useStyles();

    return (
        <header className={classes.pageHeader}>
            {icon ? (
                <div className={classes.logoContainer}>
                    <FontAwesomeIcon icon={icon} className={classes.logoGlyph} size="2x" />
                </div>
            ) : null}
            <div className={classes.title}>
                <h1 className="descriptorHeader">
                    {title}
                </h1>
            </div>
            <div className={classes.additionalInfo}>
                {lastUpdated
                && (
                    <div>
                        <label className="text-right">Last Updated:</label>
                        <div className="d-inline-flex p-2">{lastUpdated}</div>
                    </div>
                )}
            </div>
            <div className={classes.bottom}>
                <div>
                    {description}
                </div>
            </div>
            <div className={classes.headerSeperator} />
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
