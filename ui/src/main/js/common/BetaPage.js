import React, { useState } from 'react';
import * as PropTypes from 'prop-types';

const BetaPage = ({ disableBeta, children }) => {
    const [showBeta, setShowBeta] = useState(false);

    let pageContent = <div>Beta Page incorrectly set, need Array of 2 children</div>;
    if (Array.isArray(children) && children.length === 2) {
        pageContent = (showBeta) ? children[0] : children[1];
    }

    return !disableBeta && (
        <>
            Use Beta version
            <input
                type="checkbox"
                isChecked={showBeta}
                onChange={() => setShowBeta(!showBeta)}
            />
            {pageContent}
        </>
    );
};

BetaPage.propTypes = {
    disableBeta: PropTypes.bool,
    children: PropTypes.array.isRequired
};

BetaPage.defaultProps = {
    disableBeta: false
};

export default BetaPage;
