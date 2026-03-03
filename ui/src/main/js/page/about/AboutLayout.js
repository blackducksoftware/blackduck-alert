import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { getAboutInfo } from 'store/actions/about';
import { EXISTING_CHANNELS } from 'common/DescriptorInfo';
import SystemInformationSection from 'page/about/SystemInformationSection';
import DistributionChannelsSection from 'page/about/DistributionChannelsSection';
import SystemMessagesSection from 'page/about/SystemMessagesSection';

const useStyles = createUseStyles((theme) => ({
    aboutPageLayout: {
        margin: '20px'
    },
    aboutPageHeader: {
        fontSize: '32px',
        fontWeight: 'bold',
        paddingBottom: '10px',
        marginBottom: 0
    },
    aboutPageDescription: {
        fontSize: '16px',
        color: theme.colors.grey.darkerGrey,
        paddingBottom: '24px'
    },
    aboutPageContent: {
        display: 'flex',
        flexDirection: 'column',
        rowGap: '20px'
    }
}));

const AboutLayout = ({ globalDescriptorMap, distributionDescriptorMap }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const { version, projectUrl, commitHash, documentationUrl } = useSelector((state) => state.about);

    useEffect(() => {
        dispatch(getAboutInfo());
    }, []);

    const getChannelData = (descriptorMapping, existingData) => Object.values(descriptorMapping)
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

    const channelDescriptorData = addGlobalConfigurationCheck(globalDescriptorMap, distributionDescriptorMap);
    const channelData = getChannelData(channelDescriptorData, EXISTING_CHANNELS);
    const channelsMissing = !channelData || channelData.length <= 0;
    const commitHashUrl = `${projectUrl}/commit/${commitHash}`;

    return (
        <div className={classes.aboutPageLayout}>
            <h1 className={classes.aboutPageHeader}>Home</h1>
            <p className={classes.aboutPageDescription}>This application provides the ability to send notifications from a provider to various distribution channels.</p>
            <section className={classes.aboutPageContent}>
                <SystemInformationSection
                    commitHash={commitHash}
                    commitHashUrl={commitHashUrl}
                    documentationUrl={documentationUrl}
                    projectUrl={projectUrl}
                    version={version}
                />

                {!channelsMissing ?
                    <DistributionChannelsSection channelDescriptorData={channelDescriptorData} />
                    : null
                }

                <SystemMessagesSection />
            </section>
        </div>
    );
};

AboutLayout.propTypes = {
    globalDescriptorMap: PropTypes.object.isRequired,
    distributionDescriptorMap: PropTypes.object.isRequired
};

export default AboutLayout;
