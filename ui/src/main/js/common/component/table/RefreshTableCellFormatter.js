import React from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const RefreshTableCellFormatter = ({ id, handleButtonClicked, currentRowSelected }) => (
    <button
        id={id}
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
    id: PropTypes.string,
    currentRowSelected: PropTypes.object.isRequired,
    handleButtonClicked: PropTypes.func.isRequired
};

RefreshTableCellFormatter.defaultProps = {
    id: 'refreshTableCellFormatterId'
};

export default RefreshTableCellFormatter;
