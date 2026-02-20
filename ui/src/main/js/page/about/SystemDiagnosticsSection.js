import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { useDispatch } from 'react-redux';
import { fetchSystemDiagnostics } from '../../store/actions/system-diagnostics';
import Button from 'common/component/button/Button';

const useStyles = createUseStyles({
    systemDiagnosticsSection: {
        padding: [0, '50px', '50px']
    },
    sectionHeader: {
        fontSize: '16px'
    },
    sectionDescription: {
        padding: ['6px', 0]
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

const SystemDiagnosticsSection = ({ alertVersion }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleDownload = async () => {
        setIsLoading(true);
        
        try {
            const diagnosticsData = await dispatch(fetchSystemDiagnostics());
            
            if (diagnosticsData && Object.keys(diagnosticsData).length > 0) {
                // Create a Blob from the diagnostics data
                const jsonString = JSON.stringify(diagnosticsData, null, 2);
                const blob = new Blob([jsonString], { type: 'application/json' });
                const url = URL.createObjectURL(blob);

                // Create Filename
                const dateStamp = new Date();
                const filename = `system-diagnostics-${alertVersion}-${dateStamp.toISOString()}.json`;
                
                // Create a temporary link and append it to the document
                const link = document.createElement('a');
                link.href = url;
                link.download = filename;
                document.body.appendChild(link);

                // Trigger the download and clean up
                link.click();
                document.body.removeChild(link);
                URL.revokeObjectURL(url);
            }
        } catch (error) {
            setError(error);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <section className={classes.systemDiagnosticsSection}>
            <h4 className={classes.sectionHeader}>System Diagnostics</h4>
            <div className={classes.sectionDescription}>
                Gain insight into your Alert instance by downloading on demand run-time data. This data can be used to help debug and diagnose issues within the system. When a support issue is going to be opened, it is advisable to gather this data to include in the support ticket.
            </div>

            <div className={classes.sectionActions}>
                <Button
                    onClick={handleDownload}
                    text="Download System Diagnostics"
                    icon="download"
                    showLoader={isLoading}
                    isDisabled={isLoading}
                />
                {error && (
                    <div className={classes.errorMessage}>
                        Error: {error.message || 'An error occurred while fetching system diagnostics.'}
                    </div>
                )}
            </div>
        </section>
    );
};

export default SystemDiagnosticsSection;
