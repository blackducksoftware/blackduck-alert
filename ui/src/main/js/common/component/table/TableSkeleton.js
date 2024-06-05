import React from 'react';
import { createUseStyles } from 'react-jss';
import classNames from 'classnames';

const useStyles = createUseStyles(theme => ({
    '@keyframes loading': {
        from: {
            backgroundPosition: '-200px 0'
        },
        to: {
            backgroundPosition: 'calc(200px + 100%) 0'
        }
    },
    '@media (prefers-reduced-motion)': {
        skeleton: {
            animation: 'none'
        }
    },
    skeletonTableContainer: {
        width: '100%',
        paddingBottom: '12px',
        background: '#fafafa',
        borderRadius: '3px',
        display: 'grid',
        gridTemplateAreas: `
            "col1     col2     col3"
            "row1col1 row1col2 row1col3"
            "row2col1 row2col2 row2col3"
            "row3col1 row3col2 row3col3"
        `,
        gridTemplateRows: '45px 35px 35px 35px'
    },
    headerContainer: {
        width: '100%',
        height: '45px'
    },
    headerItem: {
        height: '25px',
        width: '75px',
        borderRadius: '5px',
        marginTop: '16px',
        animation: '$loading 1.5s ease-in-out infinite',
        backgroundSize: '200px 100%',
        backgroundRepeat: 'no-repeat',
        backgroundColor: '#ececec',
        backgroundImage: `linear-gradient(
            90deg,
            #ececec,
            #fbfbfb,
            #ececec
        )`
    },
    rowItem: {
        height: '20px',
        width: '125px',
        borderRadius: '5px',
        marginTop: '8px',
        animation: '$loading 1.5s ease-in-out infinite',
        backgroundSize: '200px 100%',
        backgroundRepeat: 'no-repeat',
        backgroundColor: '#ececec',
        backgroundImage: `linear-gradient(
            90deg,
            #ececec,
            #fbfbfb,
            #ececec
        )`
    },
    col1: {
        gridArea: 'col1',
        marginLeft: '10px'
    },
    row1col1: {
        gridArea: 'row1col1',
        marginLeft: '10px'
    },
    row2col1: {
        gridArea: 'row2col1',
        marginLeft: '10px'
    },
    row3col1: {
        gridArea: 'row3col1',
        marginLeft: '10px'
    },
    row1col2: {
        width: '200px'
    }, 
    row2col2: {
        width: '200px'
    }, 
    row3col2: {
        width: '200px'
    }
}));

export default function TableSkeleton() {
    const classes = useStyles();

    return (
        <div className={classes.skeletonTableContainer}>
            <div className={classNames(classes.headerItem, classes.col1)} />
            <div className={classNames(classes.headerItem, classes.col2)} />
            <div className={classNames(classes.headerItem, classes.col3)} />

            <div className={classNames(classes.rowItem, classes.row1col1)} />
            <div className={classNames(classes.rowItem, classes.row1col2)} />
            <div className={classNames(classes.rowItem, classes.row1col3)} />

            <div className={classNames(classes.rowItem, classes.row2col1)} />
            <div className={classNames(classes.rowItem, classes.row2col2)} />
            <div className={classNames(classes.rowItem, classes.row2col3)} />

            <div className={classNames(classes.rowItem, classes.row3col1)} />
            <div className={classNames(classes.rowItem, classes.row3col2)} />
            <div className={classNames(classes.rowItem, classes.row3col3)} />
        </div>
    );
};
