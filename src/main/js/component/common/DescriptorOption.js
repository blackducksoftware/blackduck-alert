import React from 'react';
import PropTypes from 'prop-types';

function DescriptorOption(props) {
    const fontAwesomeIcon = `fa fa-${props.icon} fa-fw`;
    const formattedIcon = (<span key={`icon-${props.value}`} className={fontAwesomeIcon} aria-hidden="true" />);
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
