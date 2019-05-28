import React from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

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
        <FontAwesomeIcon icon="sync" className="alert-icon" size="lg" />
    </button>
);

RefreshTableCellFormatter.propTypes = {
    currentRowSelected: PropTypes.object.isRequired,
    handleButtonClicked: PropTypes.func.isRequired
};

export default RefreshTableCellFormatter;
