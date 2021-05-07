import React from 'react';
import PropTypes from 'prop-types';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { SLACK_DISTRIBUTION_FIELD_KEYS } from 'distribution/channels/slack/SlackModel';
import TextInput from 'field/input/TextInput';

const SlackDistributionConfiguration = ({
    data, setData, errors, readonly
}) => (
    <>
        <TextInput
            key={SLACK_DISTRIBUTION_FIELD_KEYS.webhook}
            name={SLACK_DISTRIBUTION_FIELD_KEYS.webhook}
            label="Webhook"
            description="The Slack URL to receive alerts."
            readOnly={readonly}
            onChange={FieldModelUtilities.handleChange(data, setData)}
            value={FieldModelUtilities.getFieldModelSingleValue(data, SLACK_DISTRIBUTION_FIELD_KEYS.webhook)}
            errorName={FieldModelUtilities.createFieldModelErrorKey(SLACK_DISTRIBUTION_FIELD_KEYS.webhook)}
            errorValue={errors.fieldErrors[SLACK_DISTRIBUTION_FIELD_KEYS.webhook]}
        />
        <TextInput
            key={SLACK_DISTRIBUTION_FIELD_KEYS.channelName}
            name={SLACK_DISTRIBUTION_FIELD_KEYS.channelName}
            label="Channel Name"
            description="The name of the Slack channel."
            readOnly={readonly}
            onChange={FieldModelUtilities.handleChange(data, setData)}
            value={FieldModelUtilities.getFieldModelSingleValue(data, SLACK_DISTRIBUTION_FIELD_KEYS.channelName)}
            errorName={FieldModelUtilities.createFieldModelErrorKey(SLACK_DISTRIBUTION_FIELD_KEYS.channelName)}
            errorValue={errors.fieldErrors[SLACK_DISTRIBUTION_FIELD_KEYS.channelName]}
        />
        <TextInput
            key={SLACK_DISTRIBUTION_FIELD_KEYS.channelUser}
            name={SLACK_DISTRIBUTION_FIELD_KEYS.channelUser}
            label="Channel Username"
            description="The username to show as the message sender in the Slack channel."
            readOnly={readonly}
            onChange={FieldModelUtilities.handleChange(data, setData)}
            value={FieldModelUtilities.getFieldModelSingleValue(data, SLACK_DISTRIBUTION_FIELD_KEYS.channelUser)}
            errorName={FieldModelUtilities.createFieldModelErrorKey(SLACK_DISTRIBUTION_FIELD_KEYS.channelUser)}
            errorValue={errors.fieldErrors[SLACK_DISTRIBUTION_FIELD_KEYS.channelUser]}
        />
    </>
);

SlackDistributionConfiguration.propTypes = {
    data: PropTypes.object.isRequired,
    setData: PropTypes.func.isRequired,
    errors: PropTypes.object.isRequired,
    readonly: PropTypes.bool.isRequired
};

export default SlackDistributionConfiguration;
