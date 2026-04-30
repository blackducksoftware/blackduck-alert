import React from 'react';
import PropTypes from 'prop-types';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import ProviderTable from 'page/provider/ProviderTable';
import useGetPermissions from 'common/hooks/useGetPermissions';

const ProviderPageLayout = ({ descriptor }) => {
    const { readOnly } = useGetPermissions(descriptor);
    
    return (
        <div>
            <PageHeader
                title={BLACKDUCK_INFO.label}
                description={BLACKDUCK_INFO.description}
                icon="handshake"
            />
            <ProviderTable readonly={readOnly} />
        </div>
    );
};

ProviderPageLayout.propTypes = {
    descriptor: PropTypes.object
};

export default ProviderPageLayout;
