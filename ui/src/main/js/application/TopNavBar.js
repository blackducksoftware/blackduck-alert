import React from 'react';
import { createUseStyles } from 'react-jss';
import { NavLink } from 'react-router-dom';
import '/src/main/css/logos.scss';
import BlackDuckLogo from '/src/main/resources/BlackDuckLogo.png';

const useStyles = createUseStyles({
    topNav: {
        height: '100%',
        width: '100%',
        backgroundImage: 'linear-gradient(90deg, #222, #222 30%, #5a2d83 95%, #564c9d)'
    }
});

const TopNavBar = () => {
    const classes = useStyles();

    return (
        <div className={classes.topNav}>
            <div className="logo">
                <NavLink to="/alert/general/about">
                    <div className="logoContainer">
                        <img src={BlackDuckLogo} alt="logo" height="30px" />
                        <span className="divider" />
                        <span className="alertText">ALERT</span>
                    </div>
                </NavLink>
            </div>
        </div>
    );
};

export default TopNavBar;
