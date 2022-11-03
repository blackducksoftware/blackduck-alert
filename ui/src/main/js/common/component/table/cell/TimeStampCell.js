import React from 'react';
import { createUseStyles } from 'react-jss';
// import { DateTime } from "luxon";

const useStyles = createUseStyles({
    wrapperCell: {
        padding: '8px',
        textAlign: 'left',
        '&:empty::after': {
            content: '"\\00a0"'
        }
    },
    right: {
        textAlign: 'right',
        paddingRight: '35px'
    },
    center: {
        textAlign: 'center'
    }
});

const TimeStampCell = ({ data }) => {
    const {createdAt, lastUpdated} = data;
    
    // Example: Nov 13, 2022 5:15 PM
    const fullFormat = { month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit' };

    return (
        <>
            {createdAt || lastUpdated}
            {/* {DateTime.fromISO(createdAt || lastUpdated).toLocaleString(fullFormat)} */}
        </>
    )
};

export default TimeStampCell;