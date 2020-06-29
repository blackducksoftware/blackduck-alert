import React from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const GeneralButton = ({
                           onClick, children, className, id, disabled, performingAction
                       }) => (
    <div>
        <button
            id={id}
            className={`btn btn-primary ${className}`}
            type="button"
            onClick={onClick}
            disabled={disabled}
        >{children}
        </button>
        {performingAction &&
        <div className="progressContainer">
            <div className="progressIcon">
                <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />
            </div>
        </div>
        }
    </div>

);

GeneralButton.defaultProps = {
    children: 'Click Me',
    className: 'btn-md',
    id: 'generalButtonId',
    disabled: false,
    performingAction: false
};

GeneralButton.propTypes = {
    children: PropTypes.string,
    className: PropTypes.string,
    id: PropTypes.string,
    onClick: PropTypes.func.isRequired,
    disabled: PropTypes.bool,
    performingAction: PropTypes.bool
};

export default GeneralButton;
