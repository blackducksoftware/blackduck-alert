import React, { useState } from 'react';
import CommonGlobalConfigurationForm from 'common/configuration/global/CommonGlobalConfigurationForm';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import {
    SCHEDULING_DIGEST_HOURS_OPTIONS,
    SCHEDULING_FIELD_KEYS,
    SCHEDULING_INFO,
    SCHEDULING_PURGE_AUDIT_FAILED_FREQUENCY_OPTIONS,
    SCHEDULING_PURGE_FREQUENCY_OPTIONS
} from 'page/scheduling/SchedulingModel';
import * as PropTypes from 'prop-types';
import PageLayout from 'common/component/PageLayout';
import DynamicSelectInput from 'common/component/input/DynamicSelectInput';
import ReadOnlyField from 'common/component/input/field/ReadOnlyField';
import * as GlobalRequestHelper from 'common/configuration/global/GlobalRequestHelper';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import FormCard from 'common/component/FormCard';
import useGetPermissions from 'common/hooks/useGetPermissions';

const SchedulingConfiguration = ({ csrfToken, errorHandler, descriptor }) => {
    const { readOnly, canSave } = useGetPermissions(descriptor);
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, SCHEDULING_INFO.key));
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(SCHEDULING_INFO.key, csrfToken);
        if (data) {
            setFormData(data);
        }
    };

    return (
        <PageLayout
            title={SCHEDULING_INFO.label}
            description="This page shows when system scheduled tasks will run next, as well as allow you to configure the frequency of the system tasks."
            headerIcon="calendar"
            lastUpdated={formData.lastUpdated}
        >
            <FormCard formTitle="Scheduling Settings">
                <CommonGlobalConfigurationForm
                    setErrors={(error) => setErrors(error)}
                    formData={formData}
                    setFormData={(data) => setFormData(data)}
                    csrfToken={csrfToken}
                    displayTest={false}
                    displayDelete={false}
                    buttonIdPrefix={SCHEDULING_INFO.key}
                    retrieveData={retrieveData}
                    readonly={readOnly}
                    displaySave={canSave}
                    errorHandler={errorHandler}
                >
                    <DynamicSelectInput
                        id={SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay}
                        name={SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay}
                        label="Daily Digest Hour Of Day"
                        fieldDescription="Select the hour of the day to run the daily digest distribution jobs."
                        required
                        readOnly={readOnly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        options={SCHEDULING_DIGEST_HOURS_OPTIONS}
                        clearable={false}
                        value={FieldModelUtilities.getFieldModelValues(formData, SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay)}
                        errorValue={errors.fieldErrors[SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay]}
                    />
                    <ReadOnlyField
                        id={SCHEDULING_FIELD_KEYS.dailyProcessorNextRun}
                        name={SCHEDULING_FIELD_KEYS.dailyProcessorNextRun}
                        label="Daily Digest Cron Next Run Time"
                        fieldDescription="This is the next time daily digest distribution jobs will run."
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, SCHEDULING_FIELD_KEYS.dailyProcessorNextRun)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.dailyProcessorNextRun)}
                        errorValue={errors.fieldErrors[SCHEDULING_FIELD_KEYS.dailyProcessorNextRun]}
                    />
                    <DynamicSelectInput
                        id={SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays}
                        name={SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays}
                        label="Purge Notification Data"
                        tooltipDescription="Choose a frequency for cleaning up provider notification data; the default value is three days. When the purge runs, it deletes all notification data that is older than the selected value. EX: data older than 3 days will be deleted."
                        required
                        readOnly={readOnly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        options={SCHEDULING_PURGE_FREQUENCY_OPTIONS}
                        clearable={false}
                        value={FieldModelUtilities.getFieldModelValues(formData, SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays)}
                        errorValue={errors.fieldErrors[SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays]}
                    />
                    <ReadOnlyField
                        id={SCHEDULING_FIELD_KEYS.purgeDataNextRun}
                        name={SCHEDULING_FIELD_KEYS.purgeDataNextRun}
                        label="Purge Notification Data Cron Next Run Time"
                        fieldDescription="This is the next time Alert will purge provider notification data."
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, SCHEDULING_FIELD_KEYS.purgeDataNextRun)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.purgeDataNextRun)}
                        errorValue={errors.fieldErrors[SCHEDULING_FIELD_KEYS.purgeDataNextRun]}
                    />
                    <DynamicSelectInput
                        id={SCHEDULING_FIELD_KEYS.purgeDataAuditFailedFrequencyDays}
                        name={SCHEDULING_FIELD_KEYS.purgeDataAuditFailedFrequencyDays}
                        label="Purge Audit Failed Data"
                        tooltipDescription="Choose a frequency for cleaning up failed audit data; the default value is ten days. When the purge runs, it deletes all data that is older than the selected value. EX: data older than 10 days will be deleted."
                        required
                        readOnly={readOnly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        options={SCHEDULING_PURGE_AUDIT_FAILED_FREQUENCY_OPTIONS}
                        clearable={false}
                        value={FieldModelUtilities.getFieldModelValues(formData, SCHEDULING_FIELD_KEYS.purgeDataAuditFailedFrequencyDays)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.purgeDataAuditFailedFrequencyDays)}
                        errorValue={errors.fieldErrors[SCHEDULING_FIELD_KEYS.purgeDataAuditFailedFrequencyDays]}
                    />
                    <ReadOnlyField
                        id={SCHEDULING_FIELD_KEYS.purgeDataAuditFailedNextRun}
                        name={SCHEDULING_FIELD_KEYS.purgeDataAuditFailedNextRun}
                        label="Purge Audit Failed Data Cron Next Run Time"
                        fieldDescription="This is the next time Alert will purge failed audit data."
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, SCHEDULING_FIELD_KEYS.purgeDataAuditFailedNextRun)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.purgeDataAuditFailedNextRun)}
                        errorValue={errors.fieldErrors[SCHEDULING_FIELD_KEYS.purgeDataAuditFailedNextRun]}
                    />
                </CommonGlobalConfigurationForm>
            </FormCard>
        </PageLayout>
    );
};

SchedulingConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    descriptor: PropTypes.object
};

export default SchedulingConfiguration;
