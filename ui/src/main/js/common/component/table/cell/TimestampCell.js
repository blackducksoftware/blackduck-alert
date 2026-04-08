import React from 'react';
import PropTypes from 'prop-types';
import { formatDate } from 'common/util/formatDate';

const TimestampCell = ({ id, data }) => {
    const date = data[id];
    const timestamp = formatDate(date);

    return (
        <>
            {timestamp}
        </>
    )
}

TimestampCell.propTypes = {
    data: PropTypes.shape({
        authorizationMethod: PropTypes.string
    })
};

export default TimestampCell;
