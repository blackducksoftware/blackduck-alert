import React from 'react';
import PropTypes from 'prop-types';
import FieldsPanel from 'field/FieldsPanel';
import PopUp from 'field/PopUp';

const FieldsPopUp = ({
    fields, show, title, cancelLabel, okLabel, csrfToken, onCancel, handleSubmit
}) => {
    const [modalConfig, setModalConfig] = useState({});
    const [fieldErrors, setFieldErrors] = useState({});

    const internalCancel = () => {
        onCancel();
        setModalConfig({});
    };

    const internalHandleSubmit = (event) => {
        event.preventDefault();
        handleSubmit(event, modalConfig);
        internalCancel();
    };

    return (
        <div>
            <PopUp
                show={show}
                title={title}
                cancelLabel={cancelLabel}
                okLabel={okLabel}
                onCancel={internalCancel}
                handleSubmit={internalHandleSubmit}
            >
                <FieldsPanel
                    descriptorFields={fields}
                    currentConfig={modalConfig}
                    fieldErrors={fieldErrors}
                    self={this}
                    stateName="modalConfig"
                    csrfToken={csrfToken}
                />
            </PopUp>
        </div>
    );
};

FieldsPopUp.propTypes = {
    onCancel: PropTypes.func.isRequired,
    handleSubmit: PropTypes.func.isRequired,
    fields: PropTypes.array.isRequired,
    show: PropTypes.bool,
    title: PropTypes.string,
    cancelLabel: PropTypes.string,
    okLabel: PropTypes.string,
    csrfToken: PropTypes.string.isRequired
};

FieldsPopUp.defaultProps = {
    show: true,
    title: 'Pop up',
    cancelLabel: 'Cancel',
    okLabel: 'Ok'
};

export default FieldsPopUp;
