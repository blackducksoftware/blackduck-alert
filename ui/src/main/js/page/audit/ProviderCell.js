import React from 'react';
import PropTypes from 'prop-types';
import { EXISTING_CHANNELS, EXISTING_PROVIDERS } from 'common/DescriptorInfo';

const descriptorOptions = {
    ...EXISTING_PROVIDERS,
    ...EXISTING_CHANNELS
};

const ProviderCell = ({ data }) => {
    const { provider } = data.notification;

    const descriptor = descriptorOptions[provider];

    return (
        <>
            {descriptor.label}
        </>
    );
};

ProviderCell.propTypes = {
    data: PropTypes.object
};

export default ProviderCell;
