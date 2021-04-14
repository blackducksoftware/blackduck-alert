import React from 'react';
import PropTypes from 'prop-types';
import PopUp from 'field/PopUp';

const FieldsPopUp = ({
    cancelLabel, children, handleSubmit, okLabel, onCancel, show, title
}) => (
    <div>
        <PopUp
            show={show}
            title={title}
            cancelLabel={cancelLabel}
            okLabel={okLabel}
            onCancel={onCancel}
            handleSubmit={handleSubmit}
        >
            {children}
        </PopUp>
    </div>
);

FieldsPopUp.propTypes = {
    cancelLabel: PropTypes.string,
    children: PropTypes.node,
    handleSubmit: PropTypes.func.isRequired,
    okLabel: PropTypes.string,
    onCancel: PropTypes.func.isRequired,
    show: PropTypes.bool,
    title: PropTypes.string
};

FieldsPopUp.defaultProps = {
    cancelLabel: 'Cancel',
    children: null,
    okLabel: 'Ok',
    show: true,
    title: 'Pop up'
};

export default FieldsPopUp;
