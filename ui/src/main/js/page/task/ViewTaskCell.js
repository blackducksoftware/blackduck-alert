import React, { useState } from 'react';
import PropTypes from 'prop-types';
import TaskModal from 'page/task/TaskModal';
import IconButton from 'common/component/button/IconButton';

const ViewTaskCell = ({ data }) => {
    const [showModal, setShowModal] = useState(false);

    function handleClick() {
        setShowModal(true);
    }

    return (
        <>
            <IconButton icon="eye" onClick={() => handleClick()} />
            { showModal ? (
                <TaskModal
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                />
            ) : null }
        </>

    );
};

ViewTaskCell.propTypes = {
    data: PropTypes.shape({
        fullyQualifiedType: PropTypes.string,
        nextRunTime: PropTypes.string,
        properties: PropTypes.array,
        type: PropTypes.string
    })
};

export default ViewTaskCell;
