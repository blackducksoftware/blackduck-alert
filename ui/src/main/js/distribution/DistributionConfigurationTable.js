import * as PropTypes from 'prop-types';
import React, { useState } from 'react';

const DistributionConfigurationTable = ({ csrfToken, readonly, shouldRefresh }) => {
    const [tableData, setTableData] = useState([]);
    const [selectedConfigs, setSelectedConfigs] = useState([]);
    const [showDelete, setShowDelete] = useState(false);
    const [selectedRow, setSelectedRow] = useState(null);

    return (
        <div>
            Distribution Table Goes here...
        </div>
    );
};
DistributionConfigurationTable.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    shouldRefresh: PropTypes.bool
};

DistributionConfigurationTable.defaultProps = {
    readonly: false,
    shouldRefresh: false
};

export default DistributionConfigurationTable;
