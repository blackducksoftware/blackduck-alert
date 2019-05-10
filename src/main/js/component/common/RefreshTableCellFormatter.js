import React from 'react';
import PropTypes from 'prop-types';

const RefreshTableCellFormatter = ({ handleButtonClicked, currentRowSelected }) => (
    <button
        className="btn btn-link editJobButton"
        type="button"
        title="Refresh"
        onClick={(e) => {
            e.stopPropagation();
            handleButtonClicked(currentRowSelected);
        }}
    >
        <span className="fa fa-sync" />
    </button>
);

RefreshTableCellFormatter.propTypes = {
    currentRowSelected: PropTypes.object.isRequired,
    handleButtonClicked: PropTypes.func.isRequired
};

export default RefreshTableCellFormatter;
