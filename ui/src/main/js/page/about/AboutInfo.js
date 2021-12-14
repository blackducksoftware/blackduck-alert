import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import ReadOnlyField from 'common/input/field/ReadOnlyField';
import { getAboutInfo } from 'store/actions/about';
import ConfigurationLabel from 'common/ConfigurationLabel';
import { NavLink } from 'react-router-dom';
import LabeledField from 'common/input/field/LabeledField';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { EXISTING_CHANNELS, EXISTING_PROVIDERS } from 'common/DescriptorInfo';

const AboutInfo = ({
    getAbout, version, projectUrl, documentationUrl, globalDescriptorMap, distributionDescriptorMap
}) => {
    useEffect(() => {
        getAbout();
    }, []);

    const createDescriptorTable = (id, tableData, uriPrefix, tableName) => {
        const nameRenderer = (cell, row) => {
            const nameId = `aboutNameKey-${cell}`;
            const url = `${uriPrefix}${row.urlName}`;
            const renderedItem = row.urlName ? <NavLink to={url} id={nameId}>{cell}</NavLink> : <div id={nameId}>{cell}</div>;
            return renderedItem;
        };
        const tableOptions = {
            defaultSortName: 'name',
            defaultSortOrder: 'asc',
            noDataText: 'No data found'
        };

        return (
            <div className="form-group">
                <div className="form-group">
                    <label className="col-sm-3 col-form-label text-right">{tableName}</label>
                    <div className="d-inline-flex p-2 col-sm-8">
                        <div className="form-control-static">
                            <div id={id} className="form-group">
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
                                </BootstrapTable>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    };

    const createTableData = (descriptorMapping, existingData) => Object.values(descriptorMapping)
        .filter((descriptor) => existingData[descriptor.name])
        .map((descriptor) => {
            const descriptorModel = existingData[descriptor.name];
            const url = descriptor.navigation ? descriptorModel.url : null;
            return {
                name: descriptorModel.label,
                urlName: url
            };
        });

    const addGlobalConfigurationCheck = (globalDescriptorMapping, descriptorMapping) => Object.values(descriptorMapping)
        .map((descriptor) => {
            const globalConfig = globalDescriptorMapping[descriptor.name];
            const navigation = Boolean(globalConfig);
            return {
                ...descriptor,
                navigation
            };
        });

    const providerDescriptorData = addGlobalConfigurationCheck(globalDescriptorMap, globalDescriptorMap);
    const channelDescriptorData = addGlobalConfigurationCheck(globalDescriptorMap, distributionDescriptorMap);
    const providerData = createTableData(providerDescriptorData, EXISTING_PROVIDERS);
    const channelData = createTableData(channelDescriptorData, EXISTING_CHANNELS);
    const providerTable = createDescriptorTable('about-providers', providerData, '/alert/providers/', 'Providers');
    const channelTable = createDescriptorTable('about-channels', channelData, '/alert/channels/', 'Distribution Channels');
    const providersMissing = !providerData || providerData.length <= 0;
    const channelsMissing = !channelData || channelData.length <= 0;

    return (
        <div>
            <ConfigurationLabel configurationName="About" />
            <div className="form-horizontal">
                <ReadOnlyField
                    id="about-description"
                    label="Description"
                    name="description"
                    readOnly="true"
                    value="This application provides the ability to send notifications from a provider to various distribution channels."
                />
                <ReadOnlyField id="about-version" label="Version" name="version" readOnly="true" value={version} />
                <ReadOnlyField
                    id="about-url"
                    label="Project URL"
                    name="projectUrl"
                    readOnly="true"
                    value={projectUrl}
                    url={projectUrl}
                />
                <ReadOnlyField
                    id="about-documentation-url"
                    label="API Documentation (Preview)"
                    name="documentationUrl"
                    readOnly="true"
                    value="Swagger UI"
                    url={documentationUrl}
                />
                <LabeledField
                    id="about-view-distribution"
                    label="View Distributions"
                    name="distribution"
                    readOnly="true"
                >
                    <div className="d-inline-flex p-2 col-sm-8">
                        <NavLink to="/alert/jobs/distribution">
                            All Distributions
                        </NavLink>
                    </div>
                </LabeledField>
                {providersMissing && channelsMissing
                && (
                    <div className="form-group">
                        <div className="form-group">
                            <label className="col-sm-3 col-form-label text-right" />
                            <div className="d-inline-flex p-2 col-sm-8 missingData">
                                <FontAwesomeIcon icon="exclamation-triangle" className="alert-icon" size="lg" />
                                The current user cannot view Distribution Channel or Provider data!
                            </div>
                        </div>
                    </div>
                )}
                {!providersMissing && providerTable}
                {!channelsMissing && channelTable}
            </div>
        </div>
    );
};

AboutInfo.propTypes = {
    getAbout: PropTypes.func.isRequired,
    version: PropTypes.string.isRequired,
    projectUrl: PropTypes.string.isRequired,
    documentationUrl: PropTypes.string.isRequired,
    globalDescriptorMap: PropTypes.object.isRequired,
    distributionDescriptorMap: PropTypes.object.isRequired
};

const mapStateToProps = (state) => ({
    version: state.about.version,
    projectUrl: state.about.projectUrl,
    documentationUrl: state.about.documentationUrl
});

const mapDispatchToProps = (dispatch) => ({
    getAbout: () => dispatch(getAboutInfo())
});

export default connect(mapStateToProps, mapDispatchToProps)(AboutInfo);
