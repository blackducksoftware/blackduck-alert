import React from 'react';
import * as PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { Tabs } from 'react-bootstrap';

const useStyles = createUseStyles((theme) => ({
    tabNavigation: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        '& .tab-content': {
            width: '100%',
            marginTop: '20px'
        }
    },
    tabContainer: {
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: 'fit-content',
        margin: 'auto',
        borderBottom: 'none',
        '& .nav-item': {
            flex: '0 0 11rem'
        },
        '& .nav-link': {
            width: '100%',
            textAlign: 'center',
            borderRadius: '12px',
            color: 'black',
            backgroundColor: theme.colors.grey.lightGrey,
            border: `1px solid ${theme.colors.grey.lightGrey}`
        },
        '& .nav-link.active': {
            color: theme.colors.white.default,
            backgroundColor: theme.colors.purple.darkerPurple,
            border: `1px solid ${theme.colors.purple.darkerPurple}`
        },
        '& .nav-item:not(:first-child) .nav-link': {
            borderBottomLeftRadius: 0,
            borderTopLeftRadius: 0
        },
        '& .nav-item:not(:last-child) .nav-link': {
            borderBottomRightRadius: 0,
            borderTopRightRadius: 0
        }
    }
}));
const ViewTabs = ({ children, defaultActiveKey = 1, id }) => {
    const classes = useStyles();
    return (
        <div className={classes.tabNavigation}>
            <Tabs defaultActiveKey={defaultActiveKey} id={id} className={classes.tabContainer}>
                {children}
            </Tabs>
        </div>
    );
};

ViewTabs.propTypes = {
    children: PropTypes.node.isRequired,
    defaultActiveKey: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    id: PropTypes.string.isRequired
};

export default ViewTabs;
