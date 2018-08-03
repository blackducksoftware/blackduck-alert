/**
 * This data needs to be localized.
 * @type {*[]}
 */
export const jobTypes = [
    {label: '', value: ''},
    {label: 'Group Email', value: 'channel_email'},
    {label: 'HipChat', value: 'channel_hipchat'},
    {label: 'Slack', value: 'channel_slack'}
];

export const frequencyOptions = [
    {label: 'Real Time', value: 'REAL_TIME'},
    {label: 'Daily', value: 'DAILY'}
];

export const notificationOptions = [
    {label: 'Policy Violation', value: 'RULE_VIOLATION'},
    {label: 'Policy Violation Cleared', value: 'RULE_VIOLATION_CLEARED'},
    {label: 'Policy Violation Override', value: 'POLICY_OVERRIDE'},
    {label: 'Vulnerability', value: 'VULNERABILITY'}
];
