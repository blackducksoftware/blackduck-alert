import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ReadOnlyField from '../../field/ReadOnlyField';
import { getAboutInfo } from '../../store/actions/about';
import {getDescriptorByType} from '../../store/actions/descriptors';

class AboutInfo extends React.Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.getAboutInfo();
        this.props.getDescriptorByType('PROVIDER_CONFIG');
        this.props.getDescriptorByType('CHANNEL_DISTRIBUTION_CONFIG');
    }

    iconColumnRenderer(cell) {
        const altText = cell;
        const keyText = `aboutIconKey-${cell}`;
        const classNameText= `fa fa-${cell}`;
        return (<span key={keyText} alt={altText} className={classNameText} aria-hidden="true" />);
    }

    createDescriptorTable(tableData) {
        const tableOptions = {
            defaultSortName: 'label',
            defaultSortOrder: 'asc',
            noDataText: 'No data found',
        };
        return (
            <div className="form-group">
                <BootstrapTable
                    data={tableData}
                    options={tableOptions}
                    headerContainerClass="scrollable"
                    bodyContainerClass="scrollable">
                    <TableHeaderColumn dataField="fontAwesomeIcon" className="iconTableRow" columnClassName="iconTableRow" dataFormat={this.iconColumnRenderer}>
                    </TableHeaderColumn>
                    <TableHeaderColumn dataField="label" isKey>
                        Name
                    </TableHeaderColumn>
                </BootstrapTable>
            </div>
        );
    }

    render() {
        const { version,description, projectUrl } = this.props;
        const providerList = this.props.descriptors.items['PROVIDER_CONFIG'];
        const channelList = this.props.descriptors.items['CHANNEL_DISTRIBUTION_CONFIG'];
        const projectUrlLink = <a alt={projectUrl} href={projectUrl}>{projectUrl}</a>;
        const providerTable = this.createDescriptorTable(providerList);
        console.log("channel list", channelList);
        const channelTable = this.createDescriptorTable(channelList);
        return (
            <div>
                <h1>
                    <span className="fa fa-info"/>
                    About
                </h1>
                <div className="form-horizontal">
                    <ReadOnlyField label="Description" name="description" readOnly="true" value={description}/>
                    <ReadOnlyField label="Version" name="version" readOnly="true" value={version}/>
                    <ReadOnlyField label="Project URL" name="projectUrl" readOnly="true" value={projectUrlLink}/>
                    <div className="form-group">
                        <div className="form-group">
                            <label className="col-sm-3 control-label">Supported Providers</label>
                            <div className="col-sm-8">
                                <div className="form-control-static">
                                {providerTable}
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="form-group">
                        <div className="form-group">
                            <label className="col-sm-3 control-label">Supported Distribution Channels</label>
                            <div className="col-sm-8">
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
    fetching: PropTypes.bool,
    version: PropTypes.string.isRequired,
    description: PropTypes.string,
    projectUrl: PropTypes.string.isRequired,
    descriptors: PropTypes.object
};

AboutInfo.defaultProps = {
    fetching: false,
    version: '',
    description: '',
    projectUrl: '',
    descriptors: {}
};

const mapStateToProps = state => ({
    fetching: state.about.fetching,
    version: state.about.version,
    description: state.about.description,
    projectUrl: state.about.projectUrl,
    descriptors: state.descriptors
});

const mapDispatchToProps = dispatch => ({
    getAboutInfo: () => dispatch(getAboutInfo()),
    getDescriptorByType: (descriptorType) => dispatch(getDescriptorByType(descriptorType))
});

export default connect(mapStateToProps, mapDispatchToProps)(AboutInfo);
