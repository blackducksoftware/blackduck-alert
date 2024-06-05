import React from 'react';
import PropTypes from 'prop-types';

const DistributionLastSentCell = ({ data }) => {
    const { timeLastSent } = data.auditJobStatusModel;
    return (
        <>
            {timeLastSent}
        </>
    );
};

DistributionLastSentCell.propTypes = {
    data: PropTypes.object
};

export default DistributionLastSentCell;
