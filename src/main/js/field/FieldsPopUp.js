import React, { Component } from 'react';
import PropTypes from 'prop-types';
import FieldsPanel from 'field/FieldsPanel';
import PopUp from 'field/PopUp';

class FieldsPopUp extends Component {
    constructor(props) {
        super(props);

        this.internalCancel = this.internalCancel.bind(this);
        this.internalHandleSubmit = this.internalHandleSubmit.bind(this);

        this.state = {
            modalConfig: {},
            fieldErrors: {}
        };
    }

    internalCancel() {
        this.props.onCancel();
        this.setState({
            modalConfig: {}
        });
    }

    internalHandleSubmit(event) {
        event.preventDefault();
        const { modalConfig } = this.state;
        this.props.handleSubmit(event, modalConfig);
        this.internalCancel();
    }

    render() {
        const {
            fields, show, title, cancelLabel, okLabel
        } = this.props;
        const { modalConfig, fieldErrors } = this.state;

        const fieldsPanel = (<FieldsPanel
            descriptorFields={fields}
            currentConfig={modalConfig}
            fieldErrors={fieldErrors}
            self={this}
            stateName="modalConfig"
        />);
        return (
            <div>
                <PopUp
                    show={show}
                    title={title}
                    cancelLabel={cancelLabel}
                    okLabel={okLabel}
                    onCancel={this.internalCancel}
                    handleSubmit={this.internalHandleSubmit}
                >{fieldsPanel}</PopUp>
            </div>
        );
    }
}

FieldsPopUp.propTypes = {
    onCancel: PropTypes.func.isRequired,
    handleSubmit: PropTypes.func.isRequired,
    fields: PropTypes.array.isRequired,
    show: PropTypes.bool,
    title: PropTypes.string,
    cancelLabel: PropTypes.string,
    okLabel: PropTypes.string
};

FieldsPopUp.defaultProps = {
    show: true,
    title: 'Pop up',
    cancelLabel: 'Cancel',
    okLabel: 'Ok'
};

export default FieldsPopUp;
