import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import ReadOnlyField from 'field/ReadOnlyField';
import { getAboutInfo } from 'store/actions/about';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import ConfigurationLabel from 'component/common/ConfigurationLabel';
import { NavLink } from 'react-router-dom';
import LabeledField from 'field/LabeledField';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

class AboutInfo extends React.Component {
    componentDidMount() {
        this.props.getAboutInfo();
    }

    createNameColumnRenderer(uriPrefix) {
        return (cell, row) => {
            const id = `aboutNameKey-${cell}`;
            const url = `${uriPrefix}${row.urlName}`;
            if (row.navigate) {
                return (<NavLink to={url} id={id}>{cell}</NavLink>);
            } else {
                return (<div id={id}>{cell}</div>);
            }
        };
    }

    createDescriptorTable(tableData, uriPrefix) {
        const nameRenderer = this.createNameColumnRenderer(uriPrefix);
        const tableOptions = {
            defaultSortName: 'name',
            defaultSortOrder: 'asc',
            noDataText: 'No data found'
        };
        return (
            <div className="form-group">
                <BootstrapTable
                    version="4"
                    data={tableData}
                    options={tableOptions}
                    headerContainerClass="scrollable"
                    bodyContainerClass="scrollable"
                >
                    <TableHeaderColumn dataField="name" isKey dataFormat={nameRenderer}>
                        Name
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="urlName" hidden>
                        Url
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="navigate" hidden>
                        Navigate
                    </TableHeaderColumn>
                </BootstrapTable>
            </div>
        );
    }

    createTableData(userBasedDescriptors, descriptorNames) {
        debugger;
        const data = [];
        for (let key in descriptorNames) {
            const descriptor = descriptorNames[key];
            const globalDescriptor = DescriptorUtilities.findFirstDescriptorByNameAndContext(
                userBasedDescriptors, descriptor, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
            const globalConfigAllowed = Boolean(globalDescriptor);
            if (globalConfigAllowed) {
                data.push({
                    name: descriptor.label,
                    urlName: descriptor.urlName,
                    navigate: globalConfigAllowed
                });
            } else {
                
            }
        }
        return data;
    }

    render() {
        const {
            version, description, projectUrl, descriptors, providers, channels
        } = this.props;
        const userProviderList = DescriptorUtilities.findDescriptorByType(descriptors, DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER);
        const userChannelList = DescriptorUtilities.findDescriptorByType(descriptors, DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL);
        const providerData = this.createTableData(userProviderList, providers);
        const channelData = this.createTableData(userChannelList, channels);
        const providerTable = this.createDescriptorTable(providerData, '/alert/providers/');
        const channelTable = this.createDescriptorTable(channelData, '/alert/channels/');
        const distributionLink = (<div className="d-inline-flex p-2 col-sm-8"><NavLink to="/alert/jobs/distribution">All
            Distributions</NavLink></div>);
        const providersMissing = !providerData || providerData.length <= 0;
        const channelsMissing = !channelData || channelData.length <= 0;
        return (
            <div>
                <ConfigurationLabel configurationName="About" />
                <div className="form-horizontal">
                    <ReadOnlyField label="Description" name="description" readOnly="true" value={description} />
                    <ReadOnlyField label="Version" name="version" readOnly="true" value={version} />
                    <ReadOnlyField label="Project URL" name="projectUrl" readOnly="true" value={projectUrl}
                                   url={projectUrl} />
                    <LabeledField label="View Distributions" name="distribution" readOnly="true" value=""
                                  field={distributionLink} />
                    {providersMissing && channelsMissing &&
                    <div className="form-group">
                        <div className="form-group">
                            <label className="col-sm-3 col-form-label text-right" />
                            <div className="d-inline-flex p-2 col-sm-8 missingData">
                                <FontAwesomeIcon icon="exclamation-triangle" className="alert-icon" size="lg" />
                                The current user cannot view Distribution Channel or Provider data!
                            </div>
                        </div>
                    </div>
                    }
                    {!providersMissing &&
                    <div className="form-group">
                        <div className="form-group">
                            <label className="col-sm-3 col-form-label text-right">Providers</label>
                            <div className="d-inline-flex p-2 col-sm-8">
                                <div className="form-control-static">
                                    {providerTable}
                                </div>
                            </div>
                        </div>
                    </div>
                    }
                    {!channelsMissing &&
                    <div className="form-group">
                        <div className="form-group">
                            <label className="col-sm-3 col-form-label text-right">Distribution Channels</label>
                            <div className="d-inline-flex p-2 col-sm-8">
                                <div className="form-control-static">
                                    {channelTable}
                                </div>
                            </div>
                        </div>
                    </div>
                    }
                </div>
            </div>
        );
    }
}

AboutInfo.propTypes = {
    getAboutInfo: PropTypes.func.isRequired,
    version: PropTypes.string.isRequired,
    description: PropTypes.string,
    projectUrl: PropTypes.string.isRequired,
    providers: PropTypes.array,
    channels: PropTypes.array,
    descriptors: PropTypes.arrayOf(PropTypes.object)
};

AboutInfo.defaultProps = {
    description: '',
    providers: [],
    channels: [],
    descriptors: []
};

const mapStateToProps = state => ({
    version: state.about.version,
    description: state.about.description,
    projectUrl: state.about.projectUrl,
    providers: state.about.providerList,
    channels: state.about.channelList,
    descriptors: state.descriptors.items
});

const mapDispatchToProps = dispatch => ({
    getAboutInfo: () => dispatch(getAboutInfo())
});

export default connect(mapStateToProps, mapDispatchToProps)(AboutInfo);
