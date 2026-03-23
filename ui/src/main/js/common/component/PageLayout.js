import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import PageHeader from 'common/component/navigation/PageHeader';

const useStyles = createUseStyles({
    bodyContent: {
        padding: '30px'
    }
});

const PageLayout = ({ title, description, headerIcon, children }) => {
    const classes = useStyles();

    return (
        <>
            <PageHeader
                title={title}
                description={description}
                icon={headerIcon}
            />
            <div className={classes.bodyContent}>
                {children}
            </div>
        </>
    );
};

PageLayout.propTypes = {
    title: PropTypes.string.isRequired,
    description: PropTypes.string,
    headerIcon: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.array
    ]),
    children: PropTypes.any
};

export default PageLayout;