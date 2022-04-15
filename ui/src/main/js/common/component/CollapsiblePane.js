import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const CollapsiblePane = ({ children, id, title, expanded }) => {
    const [isExpanded, setIsExpanded] = useState(expanded);

    function toggleCollapsiblePane() {
        setIsExpanded(!isExpanded);
    }

    return (
        <div className="collapsiblePanel">
            <button
                id={`${id}-expand-button`}
                type="button"
                className="btn btn-link"
                onClick={toggleCollapsiblePane}
            >
                <FontAwesomeIcon icon={isExpanded ? 'minus' : 'plus'} className="icon" size="lg" />
                {title}
            </button>
            <div className={isExpanded ? 'shown' : 'hidden'}>
                {children}
            </div>
        </div>
    );
};

CollapsiblePane.defaultProps = {
    id: 'collapsiblePaneId',
    expanded: false
};

CollapsiblePane.propTypes = {
    children: PropTypes.oneOfType([
        PropTypes.array,
        PropTypes.object
    ]),
    expanded: PropTypes.bool,
    id: PropTypes.string,
    title: PropTypes.string.isRequired
};

export default CollapsiblePane;
