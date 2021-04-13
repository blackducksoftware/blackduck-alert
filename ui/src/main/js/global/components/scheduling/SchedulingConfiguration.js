import React, { useState } from 'react';
import CommonGlobalConfigurationForm from 'global/CommonGlobalConfigurationForm';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { CONTEXT_TYPE } from 'util/descriptorUtilities';
import { SCHEDULING_FIELD_KEYS, SCHEDULING_INFO } from 'global/components/scheduling/SchedulingModel';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import DynamicSelectInput from 'field/input/DynamicSelect';
import ReadOnlyField from 'field/ReadOnlyField';

const SchedulingConfiguration = ({ csrfToken, readonly }) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, SCHEDULING_INFO.key));
    const [errors, setErrors] = useState({});

    const digestHours = [
        { label: '12 am', value: '0' },
        { label: '1 am', value: '1' },
        { label: '2 am', value: '2' },
        { label: '3 am', value: '3' },
        { label: '4 am', value: '4' },
        { label: '5 am', value: '5' },
        { label: '6 am', value: '6' },
        { label: '7 am', value: '7' },
        { label: '8 am', value: '8' },
        { label: '9 am', value: '9' },
        { label: '10 am', value: '10' },
        { label: '11 am', value: '11' },
        { label: '12 pm', value: '12' },
        { label: '1 pm', value: '13' },
        { label: '2 pm', value: '14' },
        { label: '3 pm', value: '15' },
        { label: '4 pm', value: '16' },
        { label: '5 pm', value: '17' },
        { label: '6 pm', value: '18' },
        { label: '7 pm', value: '19' },
        { label: '8 pm', value: '20' },
        { label: '9 pm', value: '21' },
        { label: '10 pm', value: '22' },
        { label: '11 pm', value: '23' }
    ];

    const purgeFrequencies = [
        { label: 'Every day', value: '1' },
        { label: 'Every 2 days', value: '2' },
        { label: 'Every 3 days', value: '3' },
        { label: 'Every 4 days', value: '4' },
        { label: 'Every 5 days', value: '5' },
        { label: 'Every 6 days', value: '6' },
        { label: 'Every 7 days', value: '7' }
    ];

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
            >
                <DynamicSelectInput
                    id={SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay}
                    key={SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay}
                    name={SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay}
                    label="Daily Digest Hour Of Day"
                    description="Select the hour of the day to run the daily digest distribution jobs."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    options={digestHours}
                    value={FieldModelUtilities.getFieldModelValues(formData, SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay)}
                    errorValue={errors[SCHEDULING_FIELD_KEYS.dailyProcessorHourOfDay]}
                />
                <ReadOnlyField
                    key={SCHEDULING_FIELD_KEYS.dailyProcessorNextRun}
                    name={SCHEDULING_FIELD_KEYS.dailyProcessorNextRun}
                    label="Daily Digest Cron Next Run"
                    description="This is the next time daily digest distribution jobs will run."
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, SCHEDULING_FIELD_KEYS.dailyProcessorNextRun)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.dailyProcessorNextRun)}
                    errorValue={errors[SCHEDULING_FIELD_KEYS.dailyProcessorNextRun]}
                />
                <DynamicSelectInput
                    id={SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays}
                    key={SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays}
                    name={SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays}
                    label="Purge Data Frequency In Days"
                    description="Choose a frequency for cleaning up provider data; the default value is three days. When the purge runs, it deletes all data that is older than the selected value. EX: data older than 3 days will be deleted."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    options={purgeFrequencies}
                    value={FieldModelUtilities.getFieldModelValues(formData, SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays)}
                    errorValue={errors[SCHEDULING_FIELD_KEYS.purgeDataFrequencyDays]}
                />
                <ReadOnlyField
                    key={SCHEDULING_FIELD_KEYS.purgeDataNextRun}
                    name={SCHEDULING_FIELD_KEYS.purgeDataNextRun}
                    label="Purge Cron Next Run"
                    description="This is the next time Alert will purge provider data."
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, SCHEDULING_FIELD_KEYS.purgeDataNextRun)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(SCHEDULING_FIELD_KEYS.purgeDataNextRun)}
                    errorValue={errors[SCHEDULING_FIELD_KEYS.purgeDataNextRun]}
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

SchedulingConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool
};

SchedulingConfiguration.defaultProps = {
    readonly: false
};

export default SchedulingConfiguration;
