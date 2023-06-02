import React from 'react';
import PropTypes from 'prop-types';

const JobsCountCell = ({ data }) => (
    <>
        {data.jobs.length}
    </>
);

JobsCountCell.propTypes = {
    data: PropTypes.object
};

export default JobsCountCell;
