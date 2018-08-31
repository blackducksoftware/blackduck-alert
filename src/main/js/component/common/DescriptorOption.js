import React, {Component} from 'react';
import PropTypes from "prop-types";

class DescriptorOption extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const fontAwesomeIcon = `fa fa-${this.props.icon} fa-fw`;
        return (
            <div>
                <span key={`icon-${this.props.value}`} className={fontAwesomeIcon} aria-hidden="true"/>
                <span key={`name-${this.props.value}`}>{this.props.label}</span>
            </div>
        );
    }
}

DescriptorOption.propTypes = {
    icon: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired
};

DescriptorOption.defaultProps = {
    icon: '',
    label: '',
    value: ''
};

export default DescriptorOption;
