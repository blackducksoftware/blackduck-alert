import React, { Component } from 'react';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import ConfigurationLabel from 'component/common/ConfigurationLabel';
import PropTypes from "prop-types";
import { getAllConfigs } from 'store/actions/globalConfiguration';
import * as DescriptorUtilities from 'util/descriptorUtilities';

class ProviderTable extends Component {
    constructor(props) {
        super(props);

        this.state = {
            descriptor: null
        };
    }

    componentDidMount() {
        const descriptor = this.props.descriptors.find(descriptor => descriptor.name === DescriptorUtilities.DESCRIPTOR_NAME.PROVIDER_BLACKDUCK)
        if (descriptor) {
            this.setState({
                descriptor
            });
            this.props.getAllConfigs(descriptor.name);
        }
    }

    render() {
        const { descriptor } = this.state;
        const { providerConfigs } = this.props;
        const descriptorHeader = descriptor && (
            <div>
                <ConfigurationLabel configurationName={descriptor.label} description={descriptor.description} />
            </div>
        );

        return (
            <div>
                {descriptorHeader}
                <div>
                    Configuration table goes here!
                    Configs Found = {providerConfigs && providerConfigs.length}

                </div>
            </div>
        );
    }
}


ProviderTable.propTypes = {
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    descriptorFetching: PropTypes.bool.isRequired,
    providerConfigs: PropTypes.arrayOf(PropTypes.object).isRequired,
    getAllConfigs: PropTypes.func.isRequired
};

const mapStateToProps = state => ({
    descriptors: state.descriptors.items,
    descriptorFetching: state.descriptors.fetching,
    providerConfigs: state.globalConfiguration.allConfigs
});

const mapDispatchToProps = dispatch => ({
    getAllConfigs: descriptorName => dispatch(getAllConfigs(descriptorName))
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(ProviderTable));
