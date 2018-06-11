import React from 'react';
import PropTypes from 'prop-types';

const RefreshTableCellFormatter = ({ handleButtonClicked, currentRowSelected, id }) => (
    <button
        id={id}
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
    id: PropTypes.string,
    currentRowSelected: PropTypes.object,
    handleButtonClicked: PropTypes.func.isRequired
};

export default RefreshTableCellFormatter;
