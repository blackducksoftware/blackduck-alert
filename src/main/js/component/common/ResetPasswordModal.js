import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import TextInput from 'field/input/TextInput';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

class ResetPasswordModal extends Component {
    constructor(props) {
        super(props);
        this.state = {
            resetUsername: ''
        };

        this.handleChange = this.handleChange.bind(this);
        this.handlePasswordReset = this.handlePasswordReset.bind(this);
    }

    handleChange(event) {
        event.preventDefault();
        const { value } = event.target;
        this.setState({ resetUsername: value });
    }

    handlePasswordReset(event) {
        event.preventDefault();
        event.stopPropagation();
        this.props.resetPassword(this.state.resetUsername);
    }

    render() {
        return (
            <Modal show={this.props.showResetModal} onHide={this.props.cancelResetModal}>
                <Modal.Header closeButton>
                    <Modal.Title>Send Password Reset Email</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <TextInput id="resetUsername" label="Username" name="resetUsername" value={this.state.resetUsername} onChange={this.handleChange} />
                </Modal.Body>
                <Modal.Footer>
                    <button id="testCancel" type="button" className="btn btn-link" onClick={this.props.cancelResetModal}>Cancel</button>
                    <button id="testSend" type="button" className="btn btn-primary" onClick={this.handlePasswordReset}>Reset Password</button>
                    <div className="progressIcon">
                        {this.props.resettingPassword
                    && <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />}
                        {!this.props.resettingPassword
                    && <span>&nbsp;&nbsp;</span>}
                    </div>
                </Modal.Footer>
            </Modal>
        );
    }
}

ResetPasswordModal.propTypes = {
    showResetModal: PropTypes.bool.isRequired,
    cancelResetModal: PropTypes.func.isRequired,
    resetPassword: PropTypes.func.isRequired,
    resettingPassword: PropTypes.bool.isRequired
};

const mapStateToProps = (state) => ({
    showResetModal: state.system.showPasswordResetModal
});

export default ResetPasswordModal;
