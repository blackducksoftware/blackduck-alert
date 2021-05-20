export const SCHEDULING_INFO = {
    key: 'component_scheduling',
    url: 'scheduling',
    label: 'Scheduling'
};

export const SCHEDULING_FIELD_KEYS = {
    dailyProcessorHourOfDay: 'scheduling.daily.processor.hour',
    dailyProcessorNextRun: 'scheduling.daily.processor.next.run',
    purgeDataFrequencyDays: 'scheduling.purge.data.frequency',
    purgeDataNextRun: 'scheduling.purge.data.next.run'
};

export const SCHEDULING_DIGEST_HOURS_OPTIONS = [
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

export const SCHEDULING_PURGE_FREQUENCY_OPTIONS = [
    { label: 'Every day', value: '1' },
    { label: 'Every 2 days', value: '2' },
    { label: 'Every 3 days', value: '3' },
    { label: 'Every 4 days', value: '4' },
    { label: 'Every 5 days', value: '5' },
    { label: 'Every 6 days', value: '6' },
    { label: 'Every 7 days', value: '7' }
];
