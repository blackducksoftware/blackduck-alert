import React from 'react';
import PropTypes from 'prop-types';

const FREQUENCIES = [
    { label: 'Daily', value: 'DAILY' },
    { label: 'Real Time', value: 'REAL_TIME' }
];

const FrequencyCell = ({ data }) => {
    const { frequencyType } = data;

    const frequency = FREQUENCIES.filter((type) => type.value === frequencyType);

    return (
        <span>
            { frequency ? frequency[0].label : '-' }
        </span>
    );
};

FrequencyCell.propTypes = {
    data: PropTypes.shape({
        frequencyType: PropTypes.string
    })
};

export default FrequencyCell;
