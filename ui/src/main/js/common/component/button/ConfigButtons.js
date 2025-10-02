import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Modal from 'common/component/modal/Modal';
import Button from 'common/component/button/Button';

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
            includeTest, onTestClick, testLabel, testId, isTestDisabled
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
                    <Button id={testId} onClick={onTestClick} text={testLabel} isDisabled={isTestDisabled} />
                </div>
            );
        }
        return null;
    }

    createSaveButton() {
        const { includeSave, submitLabel, submitId, isSaveDisabled } = this.props;
        if (includeSave) {
            return (
                <Button id={submitId} text={submitLabel} type="submit" isDisabled={isSaveDisabled} />
            );
        }
        return null;
    }

    createCancelButton() {
        const {
            includeCancel, onCancelClick, cancelLabel, cancelId
        } = this.props;

        if (includeCancel) {
            return (
                <Button id={cancelId} onClick={onCancelClick} text={cancelLabel} buttonStyle="transparent" />
            );
        }
        return null;
    }

    createDeleteButton() {
        const {
            includeDelete, includeSave, deleteLabel, deleteId, isDeleteDisabled
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
                    <Button id={deleteId} onClick={this.handleDelete} text={deleteLabel} isDisabled={isDeleteDisabled} />
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
            performingAction, confirmDeleteMessage, confirmDeleteTitle
        } = this.props;

        const { showDeleteConfirmation } = this.state;
        const testButton = this.createTestButton();
        const saveButton = this.createSaveButton();
        const cancelButton = this.createCancelButton();
        const deleteButton = this.createDeleteButton();
        return (
            <div className="configButtonContainer">
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
                    <Modal
                        isOpen={showDeleteConfirmation}
                        size="sm"
                        title={confirmDeleteTitle}
                        closeModal={this.handleDeleteCancelled}
                        handleCancel={this.handleDeleteCancelled}
                        handleSubmit={this.handleDeleteConfirmed}
                        submitText="Delete"
                        style="delete"
                    >
                        <div className="modal-description">
                            {confirmDeleteMessage}
                        </div>
                    </Modal>
                </div>
            </div>
        );
    }

    render() {
        const {
            isFixed
        } = this.props;
        
        const wrapperStyles = isFixed ? 'fixedButtonGroup' : `d-inline-flex offset-sm-4 col-sm-8`;

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
    confirmDeleteMessage: PropTypes.string,
    isSaveDisabled: PropTypes.bool,
    isDeleteDisabled: PropTypes.bool,
    isTestDisabled: PropTypes.bool
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
