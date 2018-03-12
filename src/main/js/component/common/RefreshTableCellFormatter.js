import React from 'react';
import PropTypes from 'prop-types';

const RefreshTableCellFormatter = ({ handleButtonClicked, currentRowSelected }) => (
    <button
        className="btn btn-link editJobButton"
        type="button"
        title="Refresh"
        onClick={() => {
            handleButtonClicked(currentRowSelected);
        }}
    >
        <span className="fa fa-refresh" />
    </button>
);

RefreshTableCellFormatter.PropTypes = {
    currentRowSelected: PropTypes.object,
    handleButtonClicked: PropTypes.func.isRequired
};

export default RefreshTableCellFormatter;
