import React from 'react';
import PropTypes from 'prop-types';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import TextInput from 'field/input/TextInput';
import { MSTEAMS_DISTRIBUTION_FIELD_KEYS } from 'distribution/channels/msteams/MsTeamsModel';

const MsTeamsDistributionConfiguration = ({
    data, setData, errors, readonly
}) => (
    <>
        <TextInput
            key={MSTEAMS_DISTRIBUTION_FIELD_KEYS.webhook}
            name={MSTEAMS_DISTRIBUTION_FIELD_KEYS.webhook}
            label="Webhook"
            description="The MS Teams URL to receive alerts."
            readOnly={readonly}
            onChange={FieldModelUtilities.handleChange(data, setData)}
            value={FieldModelUtilities.getFieldModelSingleValue(data, MSTEAMS_DISTRIBUTION_FIELD_KEYS.webhook)}
            errorName={FieldModelUtilities.createFieldModelErrorKey(MSTEAMS_DISTRIBUTION_FIELD_KEYS.webhook)}
            errorValue={errors.fieldErrors[MSTEAMS_DISTRIBUTION_FIELD_KEYS.webhook]}
        />
    </>
);

MsTeamsDistributionConfiguration.propTypes = {
    data: PropTypes.object.isRequired,
    setData: PropTypes.func.isRequired,
    errors: PropTypes.object.isRequired,
    readonly: PropTypes.bool.isRequired
};

export default MsTeamsDistributionConfiguration;
