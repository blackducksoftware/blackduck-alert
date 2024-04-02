import React, { useState } from 'react';
import PropTypes from 'prop-types';
import AuditFailureModal from 'page/audit/AuditFailureModal';
import IconButton from 'common/component/button/IconButton';

const ViewFailureCell = ({ data }) => {
    const [showModal, setShowModal] = useState(false);

    function handleClick() {
        setShowModal(true);
    }

    return (
        <>
            <IconButton icon="eye" onClick={() => handleClick()} />
            { showModal && (
                <AuditFailureModal
                    data={data}
                    isOpen={showModal}
                    toggleModal={setShowModal}
                />
            )}
        </>

    );
};

ViewFailureCell.propTypes = {
    data: PropTypes.object
};

export default ViewFailureCell;
