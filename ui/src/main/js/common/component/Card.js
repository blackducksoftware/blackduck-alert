import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles((theme) => ({
    card: {
        display: 'flex',
        border: `solid 1px ${theme.colors.grey.lightGrey}`,
        borderRadius: '5px',
        backgroundColor: theme.colors.grey.lighterGrey,
        padding: '8px',
        margin: [0, '50px', '10px', '20px'],
        width: '250px'
    },
    icon: {
        flexBasis: '20%',
        backgroundColor: theme.colors.white.default,
        border: `solid 1px ${theme.colors.grey.lightGrey}`,
        borderRadius: '5px',
        height: '50px',
        paddingTop: '5px',
        textAlign: 'center'
    },
    cardInfo: {
        flexGrow: 1,
        padding: ['5px', 0, 0, '15px']
    }
}));

const Card = ({ icon, label, description }) => {
    const classes = useStyles();

    return (
        <div className={classes.card}>
            <div className={classes.icon}>
                <FontAwesomeIcon icon={icon} size="3x" />
            </div>
            <div className={classes.cardInfo}>
                <div style={{ fontSize: '16px' }}>{label}</div>
                { description && (<div>{description}</div>) }
            </div>
        </div>
    );
};

Card.propTypes = {
    icon: PropTypes.oneOfType([PropTypes.array, PropTypes.string]),
    label: PropTypes.string,
    description: PropTypes.string
};

export default Card;
