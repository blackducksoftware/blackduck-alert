import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React from 'react';
import { createUseStyles } from 'react-jss';


const useStyles = createUseStyles({
    loadershape: {
        borderRadius: '50%',
        height: '450px',
        width: '450px',
        margin: ['55px', 'auto'],
        backgroundColor: '#E8E8E8'
    },
    contentContainer: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        // border: 'solid 1px',
        width: '80%',
        margin: ['55px', 'auto']
    },
    icon: {
        margin: ['65px', 0, '15px', 0]
    },
    openModalRegion: {
        fontSize: '1.7rem'
    },
    modalButton: {
        backgroundColor: 'inherit',
        border: 'none',
        '&:hover': {
            textDecoration: 'underline'
        },
        '&:active': {
            outline: 0,
            textDecoration: 'none'
        },
        '&:focus': {
            outline: 0,
        }
    },
    description: {
        textAlign: 'center',
        margin: '15px',
        fontSize: '1.4rem'
    }
});


const TableLoader = ({ icon, onClick, buttonLabel, description }) => {
    const classes = useStyles();

    return (
        <div className={classes.loadershape}>
            <div className={classes.contentContainer}>
                <div className={classes.icon}>
                    <FontAwesomeIcon icon={icon} size="6x"/>
                </div>
                <div className={classes.openModalRegion}>
                    <button className={classes.modalButton} onClick={onClick}>
                        {buttonLabel}
                    </button>
                </div>
                <div className={classes.description}>
                    {description}
                </div>
            </div>
        </div>
        
    );
};

export default TableLoader;