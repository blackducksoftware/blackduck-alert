import React, { Component } from 'react';
import PropTypes from 'prop-types';
import CancelButton from 'field/input/CancelButton';
import SubmitButton from 'field/input/SubmitButton';
import GeneralButton from 'field/input/GeneralButton';

class ConfigButtons extends Component {
    render() {
        const { isFixed, performingAction, includeTest, onTestClick, testLabel, includeSave, submitLabel, includeCancel, onCancelClick, cancelLabel } = this.props;

        let fixedStyle = null;
        if (isFixed) {
            fixedStyle = 'fixedButtonGroup';
        }
        const wrapperStyles = `${fixedStyle} d-inline-flex offset-sm-3 col-sm-8`;
        return (
            <div className="form-group">
                {isFixed &&
                <div className="fixedButtonGroupBuffer" />
                }
                <div className={wrapperStyles}>
                    <div className="progressContainer">
                        <div className="progressIcon">
                            {performingAction &&
                            <span className="fa fa-spinner fa-spin" aria-hidden="true" />
                            }
                        </div>
                    </div>
                    {includeTest &&
                    <div style={{
                        display: 'inline-block', paddingRight: '12px', marginRight: '12px', borderRight: '1px solid #aaa'
                    }}
                    >
                        <GeneralButton id="generalButton" onClick={onTestClick}>{testLabel}</GeneralButton>
                    </div>
                    }
                    {includeSave &&
                    <SubmitButton id="submitButton">{submitLabel}</SubmitButton>
                    }
                    {includeCancel &&
                    <CancelButton id="cancelButton" onClick={onCancelClick}>{cancelLabel}</CancelButton>
                    }
                </div>
            </div>
        );
    }
}


ConfigButtons.propTypes = {
    includeCancel: PropTypes.bool,
    includeSave: PropTypes.bool,
    includeTest: PropTypes.bool,
    onCancelClick: PropTypes.func,
    onTestClick: PropTypes.func,
    performingAction: PropTypes.bool,
    submitLabel: PropTypes.string,
    testLabel: PropTypes.string,
    cancelLabel: PropTypes.string,
    isFixed: PropTypes.bool
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
    cancelLabel: 'Cancel',
    isFixed: true
};

export default ConfigButtons;
