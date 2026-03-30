import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Modal from 'common/component/modal/Modal';
import Button from 'common/component/button/Button';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles((theme) => ({
    configButtonContainer: {
        display: 'flex',
        justifyContent: 'end',
        paddingTop: '20px',
        marginTop: '10px',
        borderTop: `1px solid ${theme.colors.defaultBackgroundColor}`
    }
}));

const TestButton = ({ includeTest, testId, onTestClick, testLabel, isTestDisabled }) => {
    if (includeTest) {
        return (
            <Button
                id={testId}
                onClick={onTestClick}
                text={testLabel}
                isDisabled={isTestDisabled}
                buttonStyle="actionSecondary"
            />
        );
    }
    return null;
};

TestButton.propTypes = {
    testId: PropTypes.string,
    includeTest: PropTypes.bool,
    onTestClick: PropTypes.func,
    testLabel: PropTypes.string,
    isTestDisabled: PropTypes.bool
};

const SaveButton = ({ includeSave, submitId, submitLabel, isSaveDisabled }) => {
    if (includeSave) {
        return (
            <Button
                id={submitId}
                text={submitLabel}
                type="submit"
                isDisabled={isSaveDisabled}
                buttonStyle="action"
            />
        );
    }
    return null;
};

SaveButton.propTypes = {
    submitId: PropTypes.string,
    includeSave: PropTypes.bool,
    submitLabel: PropTypes.string,
    isSaveDisabled: PropTypes.bool
};

const CancelButton = ({ includeCancel, cancelId, onCancelClick, cancelLabel }) => {
    if (includeCancel) {
        return (
            <Button id={cancelId} onClick={onCancelClick} text={cancelLabel} buttonStyle="actionSecondary" />
        );
    }
    return null;
};

CancelButton.propTypes = {
    cancelId: PropTypes.string,
    includeCancel: PropTypes.bool,
    onCancelClick: PropTypes.func,
    cancelLabel: PropTypes.string
};

const DeleteButton = ({ includeDelete, deleteId, handleDelete, deleteLabel, isDeleteDisabled }) => {
    if (includeDelete) {
        return (
            <Button
                id={deleteId}
                onClick={handleDelete}
                text={deleteLabel}
                isDisabled={isDeleteDisabled}
                buttonStyle="actionSecondaryDelete"
            />
        );
    }
    return null;
};

DeleteButton.propTypes = {
    deleteId: PropTypes.string,
    includeDelete: PropTypes.bool,
    handleDelete: PropTypes.func,
    deleteLabel: PropTypes.string,
    isDeleteDisabled: PropTypes.bool
};

const ConfigButtons = ({
    cancelId, submitId, testId, deleteId, includeCancel, includeSave,
    includeTest, includeDelete, onCancelClick, onTestClick, onDeleteClick,
    performingAction, submitLabel, testLabel, cancelLabel, deleteLabel,
    confirmDeleteTitle, confirmDeleteMessage, isSaveDisabled,
    isDeleteDisabled, isTestDisabled
}) => {
    const classes = useStyles();
    const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false);

    if (!includeSave && !includeCancel && !includeTest && !includeDelete) {
        return null;
    }

    const handleDelete = () => {
        setShowDeleteConfirmation(true);
    };

    const handleDeleteConfirmed = () => {
        setShowDeleteConfirmation(false);
        onDeleteClick();
    };

    const handleDeleteCancelled = () => {
        setShowDeleteConfirmation(false);
    };

    return (
        <div className={classes.configButtonContainer}>
            <div className="progressContainer">
                <div className="progressIcon">
                    {performingAction
                    && <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />}
                </div>
            </div>
            <TestButton
                includeTest={includeTest}
                testId={testId}
                onTestClick={onTestClick}
                testLabel={testLabel}
                isTestDisabled={isTestDisabled}
            />
            <DeleteButton
                includeDelete={includeDelete}
                deleteId={deleteId}
                handleDelete={handleDelete}
                deleteLabel={deleteLabel}
                isDeleteDisabled={isDeleteDisabled}
            />
            <CancelButton
                includeCancel={includeCancel}
                cancelId={cancelId}
                onCancelClick={onCancelClick}
                cancelLabel={cancelLabel}
            />
            <SaveButton
                includeSave={includeSave}
                submitId={submitId}
                submitLabel={submitLabel}
                isSaveDisabled={isSaveDisabled}
            />
            <div>
                <Modal
                    isOpen={showDeleteConfirmation}
                    size="sm"
                    title={confirmDeleteTitle}
                    closeModal={handleDeleteCancelled}
                    handleCancel={handleDeleteCancelled}
                    handleSubmit={handleDeleteConfirmed}
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
};

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
    confirmDeleteTitle: PropTypes.string,
    confirmDeleteMessage: PropTypes.string,
    isSaveDisabled: PropTypes.bool,
    isDeleteDisabled: PropTypes.bool,
    isTestDisabled: PropTypes.bool
};

ConfigButtons.defaultProps = {
    cancelId: 'cancelButton',
    submitId: 'submitButton',
    testId: 'testButton',
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
    confirmDeleteTitle: 'Confirm Delete',
    confirmDeleteMessage: 'Are you sure you want to delete?'
};

export default ConfigButtons;
