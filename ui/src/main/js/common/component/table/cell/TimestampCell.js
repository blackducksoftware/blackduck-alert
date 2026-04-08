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
    );
};

TimestampCell.propTypes = {
    id: PropTypes.string,
    data: PropTypes.object
};

export default TimestampCell;
