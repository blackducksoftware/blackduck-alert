import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ConfigurationLabel from 'component/common/ConfigurationLabel';

class TaskManagement extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const { label, description } = this.props;
        return (
            <div>
                <ConfigurationLabel
                    configurationName={label}
                    description={description} />
            </div>
        );
    }
}

TaskManagement.propTypes = {
    descriptors: PropTypes.array.isRequired,
    description: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired
};

const mapStateToProps = state => ({
    descriptors: state.descriptors.items
});

export default connect(mapStateToProps, null)(TaskManagement);
