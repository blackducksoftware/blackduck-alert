import React from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as IconUtility from 'util/iconUtility';

function DescriptorLabel(props) {
    const { descriptor, keyPrefix } = props;
    const elementKey = `${keyPrefix}-${descriptor.label}`;
    const cellText = descriptor.label;
    return (
        <div className="inline">
            <FontAwesomeIcon key={elementKey} icon={IconUtility.createIconPath(descriptor.fontAwesomeIcon)} className="alert-icon" size="lg" />
            {cellText}
        </div>);
}

DescriptorLabel.propTypes = {
    descriptor: PropTypes.object.isRequired,
    keyPrefix: PropTypes.string.isRequired
};

export default DescriptorLabel;
