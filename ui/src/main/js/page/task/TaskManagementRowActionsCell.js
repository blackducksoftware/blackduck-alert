import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Dropdown from 'react-bootstrap/Dropdown';
import TaskModal from 'page/task/TaskModal';
import RowActionsCell from 'common/component/table/cell/RowActionsCell';

const TaskManagementRowActionsCell = ({ data }) => {
    const [showModal, setShowModal] = useState(false);

    function handleClick() {
        setShowModal(true);
    }

    return (
        <>
            <RowActionsCell>
                <Dropdown.Item as="button" onClick={handleClick}>
                    View Details
                </Dropdown.Item>
            </RowActionsCell>

            { showModal && (
                <TaskModal
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                />
            )}
        </>
    );
};

TaskManagementRowActionsCell.propTypes = {
    data: PropTypes.shape({
        fullyQualifiedType: PropTypes.string,
        nextRunTime: PropTypes.string,
        properties: PropTypes.array,
        type: PropTypes.string
    })
};

export default TaskManagementRowActionsCell;
