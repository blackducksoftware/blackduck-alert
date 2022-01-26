import React from 'react';
import PropTypes from 'prop-types';

const DescriptorLabel = ({ descriptor }) => (
    <div className="inline" title={descriptor.label}>
        {descriptor.label}
    </div>
);

DescriptorLabel.propTypes = {
    descriptor: PropTypes.object.isRequired
};

export default DescriptorLabel;
