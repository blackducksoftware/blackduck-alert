import React from 'react';
import { createUseStyles } from 'react-jss';
import { NavLink } from 'react-router-dom';
import BlackDuckLogo from '../../resources/BlackDuckLogo.png';

const useStyles = createUseStyles({
    topNav: {
        height: '100%',
        width: '100%',
        backgroundImage: 'linear-gradient(90deg, #222, #222 30%, #5a2d83 95%, #564c9d)'
    },
    logo:{
        padding: ['11px', '11px', '11px', '18px'],
        width: 'fit-content',
        '& > a': {
            textDecoration: 'none',
            color: 'white'
        }
    },
    logoContainer: {
        display: 'flex',
        alignItems: 'center',
        columnGap: '8px'
    },
    divider: {
        height: '25px',
        borderLeft: '1px solid'
    },
    alertText: {
        fontSize: '1.5em',
        lineHeight: '1.25em',
        fontFamily: ['Roboto', 'Arial', 'sans-serif']
    }
});

const TopNavBar = () => {
    const classes = useStyles();

    return (
        <div className={classes.topNav}>
            <div className={classes.logo}>
                <NavLink to="/alert/general/about">
                    <div className={classes.logoContainer}>
                        <img src={BlackDuckLogo} alt="logo" height="30px" />
                        <span className={classes.divider} />
                        <span className={classes.alertText}>ALERT</span>
                    </div>
                </NavLink>
            </div>
        </div>
    );
};

export default TopNavBar;
