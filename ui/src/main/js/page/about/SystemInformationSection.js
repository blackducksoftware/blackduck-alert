import React, { useEffect } from 'react';
import { useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import SectionCard from 'common/component/SectionCard';
import TitleContentPair from 'common/component/TitleContentPair';

const useStyles = createUseStyles((theme) => ({
    systemInformationContainer: {
        display: 'grid',
        gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
        gap: '30px'
    },
    versionBadge: {
        border: theme.defaultBorder,
        borderRadius: '12px',
        width: 'fit-content',
        padding: '0 8px'
    },
    contentLink: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        columnGap: '4px',
        width: 'fit-content',
        fontSize: '14px',
        textDecoration: 'none',
        whiteSpace: 'nowrap',
        '&:hover': {
            textDecoration: 'underline'
        }
    }
}));

const InfoLink = ({ url, text }) => {
    const classes = useStyles();

    return (
        <a
            href={url}
            className={classes.contentLink}
            disabled={!url}
            target="_blank"
            rel="noopener noreferrer"
        >
            {text}
        </a>
    );
};

InfoLink.propTypes = {
    url: PropTypes.string.isRequired,
    text: PropTypes.string.isRequired
};

const SystemInformationSection = () => {
    const classes = useStyles();
    const { version, projectUrl, commitHash, documentationUrl } = useSelector((state) => state.about);

    const commitHashUrl = `${projectUrl}/commit/${commitHash}`;

    return (
        <SectionCard title="System Information" icon="cubes">
            <div className={classes.systemInformationContainer}>
                <TitleContentPair title="Version">
                    <div className={classes.versionBadge}>
                        {version}
                    </div>
                </TitleContentPair>

                <TitleContentPair title="Commit Hash">
                    <InfoLink url={commitHashUrl} text={commitHash} />
                </TitleContentPair>

                <TitleContentPair title="Project URL">
                    <InfoLink url={projectUrl} text={projectUrl} />
                </TitleContentPair>

                <TitleContentPair title="API Guide">
                    <InfoLink url={documentationUrl} text="Swagger UI" />
                </TitleContentPair>
            </div>
        </SectionCard>
    );
};

export default SystemInformationSection;
