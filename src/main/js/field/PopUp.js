import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import FieldsPanel from './FieldsPanel';

class PopUp extends Component {
    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
        this.internalCancel = this.internalCancel.bind(this);
        this.internalOk = this.internalOk.bind(this);

        this.state = {
            modalConfig: {},
            fieldErrors: {}
        };
    }

    handleChange({ target }) {
        FieldModelUtilities.handleChange(this, target, 'modalConfig');
    }

    internalCancel() {
        this.props.onCancel();
        this.setState({
            modalConfig: {}
        });
    }

    internalOk(modalConfig) {
        this.props.onOk(modalConfig);
        this.internalCancel();
    }

    render() {
        const {
            fields, show, title, cancelLabel, okLabel
        } = this.props;
        const { modalConfig, fieldErrors } = this.state;

        return (
            <div>
                <Modal show={show} onHide={this.internalCancel}>
                    <Modal.Header closeButton>
                        <Modal.Title>{title}</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <FieldsPanel
                            descriptorFields={fields}
                            currentConfig={modalConfig}
                            fieldErrors={fieldErrors}
                            handleChange={this.handleChange}
                        />
                    </Modal.Body>
                    <Modal.Footer>
                        <button id="popUpCancel" type="button" className="btn btn-link" onClick={this.internalCancel}>
                            {cancelLabel}
                        </button>
                        <button
                            id="popUpOk"
                            type="button"
                            className="btn btn-primary"
                            onClick={() => this.internalOk(modalConfig)}
                        >
                            {okLabel}
                        </button>
                        {show &&
                        <div className="progressIcon">
                            <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />
                        </div>
                        }
                    </Modal.Footer>
                </Modal>
            </div>
        );
    }
}

PopUp.propTypes = {
    onCancel: PropTypes.func.isRequired,
    onOk: PropTypes.func.isRequired,
    fields: PropTypes.array.isRequired,
    show: PropTypes.bool,
    title: PropTypes.string,
    cancelLabel: PropTypes.string,
    okLabel: PropTypes.string
};

PopUp.defaultProps = {
    show: true,
    title: 'Pop up',
    cancelLabel: 'Cancel',
    okLabel: 'Ok'
};

export default PopUp;
