import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import ReadOnlyField from 'field/ReadOnlyField';
import { getAboutInfo } from 'store/actions/about';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import ConfigurationLabel from 'component/common/ConfigurationLabel';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as IconUtility from 'util/iconUtility';

class AboutInfo extends React.Component {
    componentDidMount() {
        this.props.getAboutInfo();
    }

    iconColumnRenderer(cell) {
        const keyText = `aboutIconKey-${cell}`;
        return (<FontAwesomeIcon key={keyText} icon={IconUtility.createIconPath(cell)} className="alert-icon" size="lg" />);
    }

    createDescriptorTable(tableData) {
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
                    <TableHeaderColumn dataField="fontAwesomeIcon" className="iconTableRow" columnClassName="iconTableRow" dataFormat={this.iconColumnRenderer} />
                    <TableHeaderColumn dataField="label" isKey>
                        Name
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
        const providerTable = this.createDescriptorTable(providerList);
        const channelTable = this.createDescriptorTable(channelList);
        return (
            <div>
                <ConfigurationLabel fontAwesomeIcon="info" configurationName="About" />
                <div className="form-horizontal">
                    <ReadOnlyField label="Description" name="description" readOnly="true" value={description} />
                    <ReadOnlyField label="Version" name="version" readOnly="true" value={version} />
                    <ReadOnlyField label="Project URL" name="projectUrl" readOnly="true" value={projectUrl} url={projectUrl} />
                    <div className="form-group">
                        <div className="form-group">
                            <label className="col-sm-3 col-form-label text-right">Supported Providers</label>
                            <div className="d-inline-flex p-2 col-sm-8">
                                <div className="form-control-static">
                                    {providerTable}
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="form-group">
                        <div className="form-group">
                            <label className="col-sm-3 col-form-label text-right">Supported Distribution Channels</label>
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
