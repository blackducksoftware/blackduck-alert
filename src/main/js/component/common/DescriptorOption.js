import React from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as IconUtility from 'util/iconUtility';

function DescriptorOption(props) {
    return (
        <div style={props.style}>
            <span key={`name-${props.value}`}>{props.label}</span>
        </div>
    );
}

DescriptorOption.propTypes = {
    label: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    style: PropTypes.object
};

DescriptorOption.defaultProps = {
    style: {}
};

export default DescriptorOption;
