import React from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const GeneralButton = ({
    id, children, className, disabled, onClick, performingAction
}) => (
    <div>
        <button
            id={id}
            className={`btn btn-primary ${className}`}
            type="button"
            onClick={onClick}
            disabled={disabled}
        >
            {children}
        </button>
        {performingAction
        && (
            <div className="progressContainer">
                <div className="progressIcon">
                    <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />
                </div>
            </div>
        )}
    </div>

);

GeneralButton.defaultProps = {
    children: 'Click Me',
    className: 'btn-md',
    disabled: false,
    id: 'generalButtonId',
    performingAction: false
};

GeneralButton.propTypes = {
    children: PropTypes.string,
    className: PropTypes.string,
    disabled: PropTypes.bool,
    id: PropTypes.string,
    onClick: PropTypes.func.isRequired,
    performingAction: PropTypes.bool
};

export default GeneralButton;
