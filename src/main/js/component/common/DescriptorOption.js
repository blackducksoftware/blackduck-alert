import React from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as IconUtility from 'util/iconUtility';

function DescriptorOption(props) {
    const formattedIcon = (<FontAwesomeIcon key={`icon-${props.value}`} icon={IconUtility.createIconPath(props.icon)} className="alert-icon" size="lg" />);
    const icon = props.icon != null ? formattedIcon : null;
    return (
        <div>
            {icon}
            <span key={`name-${props.value}`}>{props.label}</span>
        </div>
    );
}

DescriptorOption.propTypes = {
    icon: PropTypes.string,
    label: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired
};

DescriptorOption.defaultProps = {
    icon: null
}

export default DescriptorOption;
