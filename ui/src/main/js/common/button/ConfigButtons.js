import React, { Component } from 'react';
import PropTypes from 'prop-types';
import CancelButton from 'common/button/CancelButton';
import SubmitButton from 'common/button/SubmitButton';
import GeneralButton from 'common/button/GeneralButton';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ConfirmModal from 'common/ConfirmModal';

class ConfigButtons extends Component {
    constructor(props) {
        super();
        this.handleDelete = this.handleDelete.bind(this);
        this.handleDeleteConfirmed = this.handleDeleteConfirmed.bind(this);
        this.handleDeleteCancelled = this.handleDeleteCancelled.bind(this);
        this.state = {
            showDeleteConfirmation: false
        };
    }

    createTestButton() {
        const {
            includeTest, onTestClick, testLabel, testId
        } = this.props;
        if (includeTest) {
            return (
                <div style={{
                    display: 'inline-block',
                    paddingRight: '12px',
                    marginRight: '12px',
                    borderRight: '1px solid #aaa'
                }}
                >
                    <GeneralButton id={testId} onClick={onTestClick}>{testLabel}</GeneralButton>
                </div>
            );
        }
        return null;
    }

    createSaveButton() {
        const { includeSave, submitLabel, submitId } = this.props;
        if (includeSave) {
            return (<SubmitButton id={submitId}>{submitLabel}</SubmitButton>);
        }
        return null;
    }

    createCancelButton() {
        const {
            includeCancel, onCancelClick, cancelLabel, cancelId
        } = this.props;

        if (includeCancel) {
            return (<CancelButton id={cancelId} onClick={onCancelClick}>{cancelLabel}</CancelButton>);
        }
        return null;
    }

    createDeleteButton() {
        const {
            includeDelete, includeSave, deleteLabel, deleteId
        } = this.props;
        const borderLeft = includeSave ? '1px solid #aaa' : 'none';
        const style = {
            display: 'inline-block',
            paddingLeft: '12px',
            marginLeft: '12px'
        };
        if (includeDelete) {
            return (
                <div style={Object.assign(style, { borderLeft })}>
                    <GeneralButton id={deleteId} onClick={this.handleDelete}>{deleteLabel}</GeneralButton>
                </div>
            );
        }
        return null;
    }

    handleDelete() {
        this.setState({
            showDeleteConfirmation: true
        });
    }

    handleDeleteConfirmed() {
        this.setState({
            showDeleteConfirmation: false
        });
        this.props.onDeleteClick();
    }

    handleDeleteCancelled() {
        this.setState({
            showDeleteConfirmation: false
        });
    }

    createButtonContent() {
        const {
            isFixed, performingAction, confirmDeleteMessage, confirmDeleteTitle
        } = this.props;

        const { showDeleteConfirmation } = this.state;
        const testButton = this.createTestButton();
        const saveButton = this.createSaveButton();
        const cancelButton = this.createCancelButton();
        const deleteButton = this.createDeleteButton();
        const buttonContainerClass = isFixed ? '' : 'configButtonContainer';
        return (
            <div className={buttonContainerClass}>
                <div className="progressContainer">
                    <div className="progressIcon">
                        {performingAction
                        && <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />}
                    </div>
                </div>
                {testButton}
                {saveButton}
                {deleteButton}
                {cancelButton}
                <div>
                    <ConfirmModal
                        showModal={showDeleteConfirmation}
                        title={confirmDeleteTitle}
                        affirmativeAction={this.handleDeleteConfirmed}
                        negativeAction={this.handleDeleteCancelled}
                    >
                        <div>
                            {confirmDeleteMessage}
                        </div>
                    </ConfirmModal>
                </div>
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
                {isFixed
                && <div className="fixedButtonGroupBuffer" />}
                <div className={wrapperStyles}>
                    {this.createButtonContent()}
                </div>
            </div>
        );
    }
}

ConfigButtons.propTypes = {
    cancelId: PropTypes.string,
    submitId: PropTypes.string,
    testId: PropTypes.string,
    deleteId: PropTypes.string,
    includeCancel: PropTypes.bool,
    includeSave: PropTypes.bool,
    includeTest: PropTypes.bool,
    includeDelete: PropTypes.bool,
    onCancelClick: PropTypes.func,
    onTestClick: PropTypes.func,
    onDeleteClick: PropTypes.func,
    performingAction: PropTypes.bool,
    submitLabel: PropTypes.string,
    testLabel: PropTypes.string,
    cancelLabel: PropTypes.string,
    deleteLabel: PropTypes.string,
    isFixed: PropTypes.bool,
    confirmDeleteTitle: PropTypes.string,
    confirmDeleteMessage: PropTypes.string
};

ConfigButtons.defaultProps = {
    cancelId: 'cancelButton',
    submitId: 'submitButton',
    testId: 'generalButton',
    deleteId: 'deleteButton',
    includeCancel: false,
    includeSave: true,
    includeTest: false,
    includeDelete: false,
    performingAction: false,
    onCancelClick: () => true,
    onTestClick: (evt) => true,
    onDeleteClick: () => true,
    submitLabel: 'Save',
    testLabel: 'Test Configuration',
    deleteLabel: 'Delete',
    cancelLabel: 'Cancel',
    isFixed: true,
    confirmDeleteTitle: 'Confirm Delete',
    confirmDeleteMessage: 'Are you sure you want to delete?'
};

export default ConfigButtons;
