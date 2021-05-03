import { createTableSelectColumn } from 'field/input/TableSelectInput';

export const EMAIL_DISTRIBUTION_FIELD_KEYS = {
    additionalAddresses: 'email.additional.addresses',
    additionalAddressesOnly: 'email.additional.addresses.only',
    attachmentFormat: 'email.attachment.format',
    projectOwnerOnly: 'project.owner.only',
    subject: 'email.subject.line'
};

export const EMAIL_DISTRIBUTION_ATTACHMENT_OPTIONS = [
    { label: 'NONE', value: 'NONE' },
    { label: 'JSON', value: 'JSON' },
    { label: 'XML', value: 'XML' },
    { label: 'CSV', value: 'CSV' }
];

export const EMAIL_DISTRIBUTION_ADDITIONAL_EMAIL_COLUMNS = [
    createTableSelectColumn('emailAddress', 'Email Address', true, true, true)
];
