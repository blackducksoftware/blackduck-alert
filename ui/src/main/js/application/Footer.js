import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { getAboutInfo } from 'store/actions/about';
import BlackDuckLogoAllBlack from '/src/main/img/BlackDuckLogo.png';
import FooterSystemMessages from 'application/FooterSystemMessages';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles(theme => ({
    footer: {
        display: 'flex',
        alignItems: 'center',
        position: 'fixed',
        bottom: 0,
        left: 0,
        width: '100%',
        backgroundColor: theme.colors.grey.blackout,
        height: '36px',
        lineHeight: '24px',
        padding: [0, '15px'],
        fontSize: '11px',
        color: theme.colors.defaultBorderColor,
        transition: 'opacity .3s ease-in-out 0s',
        zIndex: 9999,

        '& a': {
            color: theme.colors.defaultBorderColor,
        }
    },
    logoContainer: {
        display: 'flex',
        alignItems: 'center',
        columnGap: '6px'
    },
    blackduckFooterLogo: {
        maxHeight: '16px',
        marginBottom: '2px'
    },
    logoDivider: {
        borderLeft: `solid 1px ${theme.colors.grey.default}`,
        height: '16px'
    },
    footerAlertText: {
        color: theme.colors.defaultBorderColor,
        lineHeight: '2em'
    },
    productVersion: {
        color: theme.colors.defaultBorderColor,
        textAlign: 'left',
        marginLeft: '.4em',
        marginRight: 'auto'
    },
    copyright: {
        color: theme.colors.defaultBorderColor,
        marginLeft: 'auto',
        textAlign: 'left',
        float: 'right'
    }
}));

const Footer = () => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const { version, projectUrl, copyrightYear } = useSelector((state) => state.about);
    
    useEffect(() => {
        dispatch(getAboutInfo());
    }, []);
    
    return (
        <div className={classes.footer}>
            <span className={classes.logoContainer}>
                <a href={projectUrl}>
                    <img
                        className={classes.blackduckFooterLogo}
                        src={BlackDuckLogoAllBlack}
                        alt={projectUrl}
                    />
                </a>
                <span className={classes.logoDivider} />
                <span className={classes.footerAlertText}>ALERT</span>
            </span>
            <span className={classes.productVersion}>
                v
                {version}
            </span>
            <span className={classes.copyright}>
                &nbsp;© {copyrightYear}&nbsp;
                <a id="aboutLink" href="https://www.blackduck.com/">Black Duck Software, Inc.</a>
                &nbsp;All rights reserved
            </span>
            <FooterSystemMessages />
        </div>
    );
}

export default Footer;
