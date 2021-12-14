import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import CheckboxInput from 'common/input/CheckboxInput';
import BetaComponent from 'common/beta/BetaComponent';
import CurrentComponent from 'common/beta/CurrentComponent';

const BetaPage = ({ disableBeta, children, betaSelected }) => {
    const [showBeta, setShowBeta] = useState(betaSelected);

    const foundBetaComponent = children.find((child) => child.type === BetaComponent);
    const foundCurrentComponent = children.find((child) => child.type === CurrentComponent);

    if (!foundBetaComponent || !foundCurrentComponent) {
        throw Error('Missing BetaComponent or CurrentComponent. Please ensure both are set as BetaPage children.');
    }

    const pageContent = (showBeta) ? foundBetaComponent : foundCurrentComponent;

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
    betaSelected: PropTypes.bool,
    disableBeta: PropTypes.bool,
    children: PropTypes.array.isRequired
};

BetaPage.defaultProps = {
    betaSelected: false,
    disableBeta: false
};

export default BetaPage;
