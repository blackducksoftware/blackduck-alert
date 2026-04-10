import React from 'react';
import { useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import SectionCard from 'common/component/SectionCard';
import TitleContentPair from 'common/component/TitleContentPair';

const useStyles = createUseStyles((theme) => ({
    systemInformationContainer: {
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 1fr)',
        gap: '30px',
        marginTop: '28px',
        '& > *': {
            minWidth: 0
        }
    },
    projectUrl: {
        gridColumn: '1 / -1'
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
        maxWidth: '100%',
        fontSize: '14px',
        textDecoration: 'none',
        whiteSpace: 'normal',
        overflowWrap: 'anywhere',
        wordBreak: 'break-word',
        '&:hover': {
            textDecoration: 'underline'
        }
    }
}));

const InfoLink = ({ url, text }) => {
    const classes = useStyles();

    // Very unlikely scenario, but if there is no URL, render the text without a link.
    if (!url) {
        return (
            <span
                className={classes.contentLink}
                aria-disabled="true"
            >
                {text}
            </span>
        );
    }

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

                <TitleContentPair title="API Guide">
                    <InfoLink url={documentationUrl} text="Swagger UI" />
                </TitleContentPair>

                <div className={classes.projectUrl}>
                    <TitleContentPair title="Project URL">
                        <InfoLink url={projectUrl} text={projectUrl} />
                    </TitleContentPair>
                </div>
            </div>
        </SectionCard>
    );
};

export default SystemInformationSection;
