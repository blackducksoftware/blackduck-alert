/**
 * This data needs to be localized.
 * @type {*[]}
 */
export const jobTypes = [
    { label: '', value: '' },
    { label: 'Group Email', value: 'email_group_channel' },
    { label: 'HipChat', value: 'hipchat_channel' },
    { label: 'Slack', value: 'slack_channel' }
];

export const frequencyOptions = [
    { label: 'Real Time', value: 'REAL_TIME' },
    { label: 'Daily', value: 'DAILY' }
];

export const notificationOptions = [
    { label: 'Policy Violation', value: 'POLICY_VIOLATION' },
    { label: 'Policy Violation Cleared', value: 'POLICY_VIOLATION_CLEARED' },
    { label: 'Policy Violation Override', value: 'POLICY_VIOLATION_OVERRIDE' },
    { label: 'High Vulnerability', value: 'HIGH_VULNERABILITY' },
    { label: 'Medium Vulnerability', value: 'MEDIUM_VULNERABILITY' },
    { label: 'Low Vulnerability', value: 'LOW_VULNERABILITY' }
];
