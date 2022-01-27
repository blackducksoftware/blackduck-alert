import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React from 'react';
import PropTypes from 'prop-types';

export const ProgressIcon = ({
    inProgress, icon
}) => inProgress && (
    <div className="progressIcon">
        <span className="fa-layers fa-fw">
            <FontAwesomeIcon icon={icon} className="alert-icon" size="lg" spin />
        </span>
    </div>
);

ProgressIcon.propTypes = {
    inProgress: PropTypes.bool.isRequired,
    icon: PropTypes.string
};

ProgressIcon.defaultProps = {
    icon: 'spinner'
};
