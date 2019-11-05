import React from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as IconUtility from 'util/iconUtility';

function DescriptorLabel(props) {
    const { descriptor } = props;
    const cellText = descriptor.label;
    return (
        <div className="inline">
            {cellText}
        </div>);
}

DescriptorLabel.propTypes = {
    descriptor: PropTypes.object.isRequired
};

export default DescriptorLabel;
