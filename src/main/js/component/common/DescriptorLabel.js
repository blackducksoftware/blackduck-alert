import React from 'react';
import PropTypes from 'prop-types';

function DescriptorLabel(props) {
    const { descriptor, keyPrefix } = props;
    const icon = `fa fa-${descriptor.fontAwesomeIcon} fa-fw`;
    const elementKey = `${keyPrefix}-${descriptor.label}`;
    const cellText = descriptor.label;
    return (
        <div className="inline">
            <span key={elementKey} className={icon} aria-hidden="true" />
            {cellText}
        </div>);
}

DescriptorLabel.propTypes = {
    descriptor: PropTypes.object.isRequired,
    keyPrefix: PropTypes.string.isRequired
};

export default DescriptorLabel;
