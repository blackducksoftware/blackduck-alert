import React from 'react';
import PropTypes from 'prop-types';
import Modal from 'common/component/modal/Modal';
import ReadOnlyField from 'common/component/input/field/ReadOnlyField';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    content: {
        display: 'flex',
        flexDirection: 'column',
        minHeight: '175px'
    }
});

const TaskModal = ({ data, isOpen, toggleModal }) => {
    const classes = useStyles();
    const { fullyQualifiedType, nextRunTime, properties, type } = data;

    function handleClose() {
        toggleModal(false);
    }

    function setId(identifier) {
        return `${identifier}-readOnlyFieldId`;
    }

    return (
        <Modal
            isOpen={isOpen}
            size="lg"
            title="Task Details"
            closeModal={handleClose}
            handleSubmit={handleClose}
            submitText="Close"
        >
            <div className={classes.content}>
                <ReadOnlyField
                    id={setId('type')}
                    label="Task Name"
                    value={type}
                />
                <ReadOnlyField
                    id={setId('fullyQualifiedType')}
                    label="Task Full Name"
                    value={fullyQualifiedType}
                />
                <ReadOnlyField
                    id={setId('nextRunTime')}
                    label="Task Next Run Time"
                    value={nextRunTime}
                />


                {/* If properties array has content, Provider will be present in first position */}
                { properties && properties.map(property => (
                        <ReadOnlyField
                            id={setId(property.key)}
                            label={property.displayName}
                            value={property.value}
                        />
                    ))
                }
            </div>
        </Modal>
    );
};

TaskModal.propTypes = {
    data: PropTypes.shape({
        fullyQualifiedType: PropTypes.string,
        nextRunTime: PropTypes.string,
        properties: PropTypes.array,
        type: PropTypes.string
    }),
    isOpen: PropTypes.bool,
    toggleModal: PropTypes.func
};

export default TaskModal;
