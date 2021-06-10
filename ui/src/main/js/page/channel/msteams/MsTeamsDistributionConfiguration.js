import React from 'react';
import PropTypes from 'prop-types';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import TextInput from 'common/input/TextInput';
import { MSTEAMS_DISTRIBUTION_FIELD_KEYS } from 'page/channel/msteams/MSTeamsModel';

const MsTeamsDistributionConfiguration = ({
    data, setData, errors, readonly
}) => (
    <>
        <TextInput
            id={MSTEAMS_DISTRIBUTION_FIELD_KEYS.webhook}
            key={MSTEAMS_DISTRIBUTION_FIELD_KEYS.webhook}
            name={MSTEAMS_DISTRIBUTION_FIELD_KEYS.webhook}
            label="Webhook"
            description="The MS Teams URL to receive alerts."
            required
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
