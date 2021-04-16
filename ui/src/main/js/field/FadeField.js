import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';

const FadeField = ({ timeout, children }) => {
    const [showChildren, setShowChildren] = useState(true);
    const [removeChildren, setRemoveChildren] = useState(false);

    useEffect(() => {
        const displayTimer = setTimeout(() => {
            setShowChildren(false);
        }, timeout);
        const hideTimer = setTimeout(() => {
            setRemoveChildren(true);
        }, timeout + 2000);
        return () => {
            clearTimeout(displayTimer);
            clearTimeout(hideTimer);
        };
    }, [children]);

    const componentChildren = removeChildren ? null : children;
    const clazz = !showChildren ? 'fadingField' : 'visibleField';
    return (
        <div className={clazz}>
            {componentChildren}
        </div>
    );
};

FadeField.propTypes = {
    children: PropTypes.any,
    timeout: PropTypes.number
};

FadeField.defaultProps = {
    children: null,
    timeout: 5000
};

export default FadeField;
