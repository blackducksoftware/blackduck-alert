import React from 'react';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';

const ProviderCell = () => (
    <span>
        {BLACKDUCK_INFO.label}
    </span>
);

export default ProviderCell;
