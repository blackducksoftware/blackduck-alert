import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import CheckboxInput from 'common/input/CheckboxInput';

const BetaPage = ({ disableBeta, children }) => {
    const [showBeta, setShowBeta] = useState(false);

    let pageContent = <div>Beta Page incorrectly set, need Array of 2 children</div>;
    if (Array.isArray(children) && children.length === 2) {
        pageContent = (showBeta) ? children[0] : children[1];
    }

    return !disableBeta && (
        <>
            <div className="topRight">
                <CheckboxInput
                    label="Use Beta Version"
                    labelClass="text-right"
                    id="beta-check-id"
                    isChecked={showBeta}
                    onChange={() => setShowBeta(!showBeta)}
                />
            </div>
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
