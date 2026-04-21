import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { useDispatch, useSelector } from 'react-redux';
import { fetchSystemDiagnostics } from 'store/actions/system-diagnostics';
import Button from 'common/component/button/Button';
import SectionCard from 'common/component/SectionCard';

const useStyles = createUseStyles({
    sectionDescription: {
        padding: ['20px', '20px', '6px', 0]
    },
    sectionActions: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginTop: '10px'
    },
    errorMessage: {
        color: 'red',
        marginLeft: '20px'
    }
});

const SystemDiagnosticsSection = () => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const [isLoading, setIsLoading] = useState(false);
    const [sectionError, setSectionError] = useState(null);
    const { version } = useSelector((state) => state.about);

    const handleDownload = async () => {
        setIsLoading(true);
        setSectionError(null);

        try {
            const diagnosticsData = await dispatch(fetchSystemDiagnostics());

            const dataToWrite = (diagnosticsData && Object.keys(diagnosticsData).length > 0)
                ? diagnosticsData
                : {};

            // Create a Blob from the diagnostics data
            const jsonString = JSON.stringify(dataToWrite, null, 2);
            const blob = new Blob([jsonString], { type: 'application/json' });
            const url = URL.createObjectURL(blob);

            // Create Filename
            const dateStamp = new Date();
            const filename = `system-diagnostics-${version}-${dateStamp.toISOString()}.json`;

            // Create a temporary link and append it to the document
            const link = document.createElement('a');
            link.href = url;
            link.download = filename;
            document.body.appendChild(link);

            // Trigger the download and clean up
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
        } catch (error) {
            setSectionError(error);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <SectionCard title="System Diagnostics" icon="chart-column">
            <div className={classes.sectionDescription}>
                Gain insight into your Alert instance by downloading on demand run-time data. This data can be used to help debug and diagnose issues within the system. When a support issue is going to be opened, it is advisable to gather this data to include in the support ticket.
            </div>

            <div className={classes.sectionActions}>
                <Button
                    onClick={handleDownload}
                    text="Download System Diagnostics"
                    icon="download"
                    buttonStyle="action"
                    showLoader={isLoading}
                    isDisabled={isLoading}
                />
                {sectionError && (
                    <div className={classes.errorMessage}>
                        Error: {sectionError.message || 'An error occurred while fetching system diagnostics.'}
                    </div>
                )}
            </div>
        </SectionCard>
    );
};

export default SystemDiagnosticsSection;
