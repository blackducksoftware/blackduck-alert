import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles((theme) => ({
    titleContentContainer: {
        display: 'flex',
        flexDirection: 'column'
    },
    title: {
        marginBottom: '4px',
        fontSize: '14px',
        color: theme.colors.grey.default
    }
}));

/**
 * Common component for displaying a stacked title and content pair. Used in the about page to display system information.
 * @param {string} title - The title to be displayed above the content
 * @param {string | ReactNode} children - The content to be displayed below the title
 * @returns
 */
const TitleContentPair = ({
    title, children
}) => {
    const classes = useStyles();

    return (
        <div className={classes.titleContentContainer}>
            <p className={classes.title}>{title}</p>
            {children}
        </div>
    );
};

TitleContentPair.propTypes = {
    title: PropTypes.string.isRequired,
    children: PropTypes.oneOfType([PropTypes.string, PropTypes.node]).isRequired
};

export default TitleContentPair;
