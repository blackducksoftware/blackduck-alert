import React, {Component} from 'react';
import PropTypes from 'prop-types';

class DescriptorLabel extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        const {descriptor, keyPrefix} = this.props;
        const icon = `fa fa-${descriptor.fontAwesomeIcon} fa-fw`;
        const elementKey = `${keyPrefix}-${descriptor.label}`;
        const cellText = descriptor.label;
        return (
            <div className="inline">
                <span key={elementKey} className={icon} aria-hidden="true" />
                {cellText}
            </div>);
    }
}

DescriptorLabel.propTypes = {
    descriptor: PropTypes.object.isRequired,
    keyPrefix: PropTypes.string.isRequired
};

DescriptorLabel.defaultProps = {
    descriptor: {},
    keyPrefix: ''
};

export default DescriptorLabel;
