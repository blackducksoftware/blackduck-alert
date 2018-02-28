import React from 'react';
import PropTypes from 'prop-types';

const AutoRefresh = ({autoRefresh, handleAutoRefreshChange}) => (
    <label className="refreshCheckbox"><input name="autoRefresh" type="checkbox" checked={autoRefresh} onChange={handleAutoRefreshChange} /> Enable Auto-Refresh</label>
);

AutoRefresh.propTypes = {
    autoRefresh: PropTypes.bool.isRequired,
    handleAutoRefreshChange: PropTypes.func.isRequired
}

export default AutoRefresh;