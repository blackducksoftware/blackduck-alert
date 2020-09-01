import React from 'react';
import PropTypes from 'prop-types';

function DescriptorLabel(props) {
    const { descriptor } = props;
    const cellText = descriptor.label;
    return (
        <div className="inline" title={cellText}>
            {cellText}
        </div>
    );
}

DescriptorLabel.propTypes = {
    descriptor: PropTypes.object.isRequired
};

export default DescriptorLabel;
