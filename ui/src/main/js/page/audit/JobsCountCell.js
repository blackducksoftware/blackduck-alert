import React from 'react';
import PropTypes from 'prop-types';

const JobsCountCell = ({ data }) => {
    return (
        <>
            {data.jobs.length}
        </>
    );
};

JobsCountCell.propTypes = {
    data: PropTypes.object
};

export default JobsCountCell;
