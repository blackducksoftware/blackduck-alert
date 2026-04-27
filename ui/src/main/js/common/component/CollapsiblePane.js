import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Button from 'common/component/button/Button';

const CollapsiblePane = ({ children, id, title, expanded, isDisabled }) => {
    const [isExpanded, setIsExpanded] = useState(expanded);

    function toggleCollapsiblePane() {
        setIsExpanded(!isExpanded);
    }

    return (
        <div className="collapsiblePanel">
            <Button
                id={id}
                onClick={toggleCollapsiblePane}
                text={title}
                icon={isExpanded ? 'angle-down' : 'angle-right'}
                buttonStyle="actionSecondary"
                isDisabled={isDisabled}
            />
            <div className={isExpanded ? 'shown' : 'hidden'}>
                {children}
            </div>
        </div>
    );
};

CollapsiblePane.defaultProps = {
    id: 'collapsiblePaneId',
    isDisabled: false,
    expanded: false
};

CollapsiblePane.propTypes = {
    children: PropTypes.oneOfType([
        PropTypes.array,
        PropTypes.object
    ]),
    expanded: PropTypes.bool,
    id: PropTypes.string,
    isDisabled: PropTypes.bool,
    title: PropTypes.string.isRequired
};

export default CollapsiblePane;
