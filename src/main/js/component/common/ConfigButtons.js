import React from 'react';
import PropTypes from 'prop-types';
import CancelButton from 'field/input/CancelButton';
import SubmitButton from 'field/input/SubmitButton';
import GeneralButton from 'field/input/GeneralButton';

const ConfigButtons = props => (
    <div className="form-group">
        <div className="d-inline-flex offset-sm-3 col-sm-8">
            <div className="progressContainer">
                <div className="progressIcon">
                    {props.performingAction &&
                    <span className="fa fa-spinner fa-pulse" aria-hidden="true" />
                    }
                </div>
            </div>
            {props.includeTest &&
            <div style={{
                display: 'inline-block', paddingRight: '12px', marginRight: '12px', borderRight: '1px solid #aaa'
            }}
            >
                <GeneralButton id="generalButton" onClick={props.onTestClick}>{props.testLabel}</GeneralButton>
            </div>
            }
            {props.includeSave &&
            <SubmitButton id="submitButton">{props.submitLabel}</SubmitButton>
            }
            {props.includeCancel &&
            <CancelButton id="cancelButton" onClick={props.onCancelClick}>{props.cancelLabel}</CancelButton>
            }
        </div>
    </div>
);

ConfigButtons.propTypes = {
    includeCancel: PropTypes.bool,
    includeSave: PropTypes.bool,
    includeTest: PropTypes.bool,
    onCancelClick: PropTypes.func,
    onTestClick: PropTypes.func,
    performingAction: PropTypes.bool,
    submitLabel: PropTypes.string,
    testLabel: PropTypes.string,
    cancelLabel: PropTypes.string
};

ConfigButtons.defaultProps = {
    includeCancel: false,
    includeSave: true,
    includeTest: false,
    performingAction: false,
    onCancelClick: () => {
    },
    onTestClick: (evt) => {
    },
    submitLabel: 'Save',
    testLabel: 'Test Configuration',
    cancelLabel: 'Cancel'
};

export default ConfigButtons;
