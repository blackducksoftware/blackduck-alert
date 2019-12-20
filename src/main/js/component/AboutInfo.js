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
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

class AboutInfo extends React.Component {
    componentDidMount() {
        this.props.getAboutInfo();
    }

    createNameColumnRenderer(uriPrefix) {
        return (cell, row) => {
            const id = `aboutNameKey-${cell}`;
            const url = `${uriPrefix}${row.urlName}`;
            return (<NavLink to={url} id={id}>{cell}</NavLink>);
        };
    }

    createDescriptorTable(tableData, uriPrefix) {
        const nameRenderer = this.createNameColumnRenderer(uriPrefix);
        const tableOptions = {
            defaultSortName: 'label',
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
                    <TableHeaderColumn dataField="label" isKey dataFormat={nameRenderer}>
                        Name
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="urlName" hidden>
                        Url
                    </TableHeaderColumn>
                </BootstrapTable>
            </div>
        );
    }

    render() {
        const {
            version, description, projectUrl, descriptors
        } = this.props;
        const providerList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
        const channelList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.DISTRIBUTION);
        const providerTable = this.createDescriptorTable(providerList, '/alert/providers/');
        const channelTable = this.createDescriptorTable(channelList, '/alert/channels/');
        const distributionLink = (<div className="d-inline-flex p-2 col-sm-8"><NavLink to="/alert/jobs/distribution">All Distributions</NavLink></div>);
        const providersMissing = !providerList || providerList.length <= 0;
        const channelsMissing = !channelList || channelList.length <= 0;
        return (
            <div>
                <ConfigurationLabel configurationName="About" />
                <div className="form-horizontal">
                    <ReadOnlyField label="Description" name="description" readOnly="true" value={description} />
                    <ReadOnlyField label="Version" name="version" readOnly="true" value={version} />
                    <ReadOnlyField label="Project URL" name="projectUrl" readOnly="true" value={projectUrl} url={projectUrl} />
                    <LabeledField label="View Distributions" name="distribution" readOnly="true" value="" field={distributionLink} />
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
    descriptors: PropTypes.arrayOf(PropTypes.object)
};

AboutInfo.defaultProps = {
    description: '',
    descriptors: []
};

const mapStateToProps = state => ({
    version: state.about.version,
    description: state.about.description,
    projectUrl: state.about.projectUrl,
    descriptors: state.descriptors.items
});

const mapDispatchToProps = dispatch => ({
    getAboutInfo: () => dispatch(getAboutInfo())
});

export default connect(mapStateToProps, mapDispatchToProps)(AboutInfo);
