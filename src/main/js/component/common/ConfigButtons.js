import React, { Component } from 'react';
import PropTypes from 'prop-types';
import CancelButton from 'field/input/CancelButton';
import SubmitButton from 'field/input/SubmitButton';
import GeneralButton from 'field/input/GeneralButton';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

class ConfigButtons extends Component {
    createTestButton() {
        const { includeTest, onTestClick, testLabel } = this.props;
        if (includeTest) {
            return (<div style={{
                display: 'inline-block',
                paddingRight: '12px',
                marginRight: '12px',
                borderRight: '1px solid #aaa'
            }}
            >
                <GeneralButton id="generalButton" onClick={onTestClick}>{testLabel}</GeneralButton>
            </div>);
        }
        return null;
    }

    createSaveButton() {
        const { includeSave, submitLabel } = this.props;
        if (includeSave) {
            return (<SubmitButton id="submitButton">{submitLabel}</SubmitButton>);
        }
        return null;
    }

    createCancelButton() {
        const { includeCancel, onCancelClick, cancelLabel } = this.props;

        if (includeCancel) {
            return <CancelButton id="cancelButton" onClick={onCancelClick}>{cancelLabel}</CancelButton>;
        }
        return null;
    }

    createButtonContent() {
        const {
            isFixed, performingAction
        } = this.props;
        const testButton = this.createTestButton();
        const saveButton = this.createSaveButton();
        const cancelButton = this.createCancelButton();
        const buttonContainerClass = isFixed ? '' : 'configButtonContainer';
        return (
            <div className={buttonContainerClass}>
                <div className="progressContainer">
                    <div className="progressIcon">
                        {performingAction &&
                        <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />
                        }
                    </div>
                </div>
                {testButton}
                {saveButton}
                {cancelButton}
            </div>
        );
    }

    render() {
        const {
            isFixed
        } = this.props;

        let fixedStyle = '';
        if (isFixed) {
            fixedStyle = 'fixedButtonGroup';
        }
        const wrapperStyles = `${fixedStyle} d-inline-flex offset-sm-4 col-sm-8`;
        return (
            <div className="form-group">
                {isFixed &&
                <div className="fixedButtonGroupBuffer" />
                }
                <div className={wrapperStyles}>
                    {this.createButtonContent()}
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
    onCancelClick: () => true,
    onTestClick: (evt) => true,
    submitLabel: 'Save',
    testLabel: 'Test Configuration',
    cancelLabel: 'Cancel',
    isFixed: true
};

export default ConfigButtons;
