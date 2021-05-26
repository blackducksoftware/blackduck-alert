import React, { useState } from 'react';
import CommonGlobalConfigurationForm from 'common/global/CommonGlobalConfigurationForm';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import { SCHEDULING_DIGEST_HOURS_OPTIONS, SCHEDULING_FIELD_KEYS, SCHEDULING_INFO, SCHEDULING_PURGE_FREQUENCY_OPTIONS } from 'page/scheduling/SchedulingModel';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import DynamicSelectInput from 'common/input/DynamicSelectInput';
import ReadOnlyField from 'common/input/field/ReadOnlyField';
import * as GlobalRequestHelper from 'common/global/GlobalRequestHelper';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';

const SchedulingConfiguration = ({ csrfToken, errorHandler, readonly, displaySave }) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, SCHEDULING_INFO.key));
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(SCHEDULING_INFO.key, csrfToken);
        if (data) {
            setFormData(data);
        }
    };

    return (
        <CommonGlobalConfiguration
            label={SCHEDULING_INFO.label}
            description="This page shows when system scheduled tasks will run next, as well as allow you to configure the frequency of the system tasks."
            lastUpdated={formData.lastUpdated}
        >
            <CommonGlobalConfigurationForm
                setErrors={(error) => setErrors(error)}
                formData={formData}
                setFormData={(data) => setFormData(data)}
                csrfToken={csrfToken}
                displayTest={false}
                displayDelete={false}
                buttonIdPrefix={SCHEDULING_INFO.key}
                retrieveData={retrieveData}
                readonly={readonly}
                displaySave={displaySave}
                errorHandler={errorHandler}
            >
                <DynamicSelectInput
                    id={SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay}
                    name={SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay}
                    label="Daily Digest Hour Of Day"
                    description="Select the hour of the day to run the daily digest distribution jobs."
                    required
                    readOnly={readonly}
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
                    label="Daily Digest Cron Next Run"
                    description="This is the next time daily digest distribution jobs will run."
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, SCHEDULING_FIELD_KEYS.dailyProcessorNextRun)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.dailyProcessorNextRun)}
                    errorValue={errors.fieldErrors[SCHEDULING_FIELD_KEYS.dailyProcessorNextRun]}
                />
                <DynamicSelectInput
                    id={SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays}
                    name={SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays}
                    label="Purge Data Frequency In Days"
                    description="Choose a frequency for cleaning up provider data; the default value is three days. When the purge runs, it deletes all data that is older than the selected value. EX: data older than 3 days will be deleted."
                    required
                    readOnly={readonly}
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
                    label="Purge Cron Next Run"
                    description="This is the next time Alert will purge provider data."
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, SCHEDULING_FIELD_KEYS.purgeDataNextRun)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.purgeDataNextRun)}
                    errorValue={errors.fieldErrors[SCHEDULING_FIELD_KEYS.purgeDataNextRun]}
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

SchedulingConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displaySave: PropTypes.bool
};

SchedulingConfiguration.defaultProps = {
    readonly: false,
    displaySave: true
};

export default SchedulingConfiguration;
