import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import ReadOnlyField from 'common/component/input/field/ReadOnlyField';
import { getAboutInfo } from 'store/actions/about';
import ConfigurationLabel from 'common/component/ConfigurationLabel';
import { NavLink } from 'react-router-dom';
import LabeledField from 'common/component/input/field/LabeledField';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { EXISTING_CHANNELS, EXISTING_PROVIDERS } from 'common/DescriptorInfo';
import AboutProviderTable from './AboutProviderTable';
import AboutChannelsTable from './AboutChannelsTable';

const useStyles = createUseStyles({
    aboutInfoContainer: {
        display: 'flex',
        padding: ['10px', 0]
    },
    aboutInfoDescriptor: {
        minWidth: '25%',
        padding: ['6px', '15px'],
        fontWeight: 'bold',
        lineHeight: '1.5',
        textAlign: 'right'
    },
    aboutInfoTable: {
        padding: [0, '16px'],
        width: '100%'
    }
});

const AboutInfo = ({
    getAbout, version, projectUrl, commitHash, documentationUrl, globalDescriptorMap, distributionDescriptorMap
}) => {
    const classes = useStyles();
    useEffect(() => {
        getAbout();
    }, []);

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
    const providersMissing = !providerData || providerData.length <= 0;
    const channelsMissing = !channelData || channelData.length <= 0;
    const commitHashUrl = `${projectUrl}/commit/${commitHash}`;

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
                <ReadOnlyField id="about-commit-hash" label="Commit Hash" name="commitHash" readOnly="true" value={commitHash} url={commitHashUrl} />
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

                {!providersMissing && (
                    <div className={classes.aboutInfoContainer}>
                        <div className={classes.aboutInfoDescriptor}>
                            Providers
                        </div>
                        <div className={classes.aboutInfoTable}>
                            <AboutProviderTable tableData={providerData} />
                        </div>
                    </div>
                )}

                {!channelsMissing && (
                    <div className={classes.aboutInfoContainer}>
                        <div className={classes.aboutInfoDescriptor}>
                            Distribution Channels
                        </div>
                        <div className={classes.aboutInfoTable}>
                            <AboutChannelsTable tableData={channelData} />
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

AboutInfo.propTypes = {
    getAbout: PropTypes.func.isRequired,
    version: PropTypes.string.isRequired,
    projectUrl: PropTypes.string.isRequired,
    commitHash: PropTypes.string.isRequired,
    documentationUrl: PropTypes.string.isRequired,
    globalDescriptorMap: PropTypes.object.isRequired,
    distributionDescriptorMap: PropTypes.object.isRequired
};

const mapStateToProps = (state) => ({
    version: state.about.version,
    projectUrl: state.about.projectUrl,
    commitHash: state.about.commitHash,
    documentationUrl: state.about.documentationUrl
});

const mapDispatchToProps = (dispatch) => ({
    getAbout: () => dispatch(getAboutInfo())
});

export default connect(mapStateToProps, mapDispatchToProps)(AboutInfo);
