import React from 'react';
import { createUseStyles } from 'react-jss';
import Header from 'common/component/Header';

const useStyles = createUseStyles((theme) => ({
    viewWrapper: {
        padding: '0px',
        fontSize: '13px',
        height: '800px',
        width: '100%',
        fontFamily: ['Roboto', 'Arial', 'sans-serif'],
        display: 'inlineBlock',
        position: 'absolute'
    },
    authContainer: {
        width: '100vw',
        height: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
    },
    authContent: {
        backgroundColor: theme.colors.defaultBackgroundColor,
        borderRadius: '8px',
        boxShadow: `1px 1px 10px ${theme.colors.defaultBorderColor}`,
        marginBottom: '20px',
        maxWidth: '500px',
        minHeight: '175px',
        minWidth: '300px',
        overflow: 'hidden',
        position: 'fixed',
        width: '100%'
    }
}));

const AuthorizationView = ({ children }) => {
    const classes = useStyles();

    return (
        <div className={classes.viewWrapper}>
            <div className={classes.authContainer}>
                <div className={classes.authContent}>
                    <Header />
                    {children}
                </div>
            </div>
        </div>
    );
};

export default AuthorizationView;
