import React from 'react';
import PropTypes from 'prop-types';

function DescriptorOption({ label, value, style }) {
    return (
        <div style={style}>
            <span key={`name-${value}`}>{label}</span>
        </div>
    );
}

DescriptorOption.propTypes = {
    label: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    style: PropTypes.object
};

export default DescriptorOption;
