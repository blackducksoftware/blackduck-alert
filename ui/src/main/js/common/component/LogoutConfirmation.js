import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import { connect } from 'react-redux';
import { cancelLogout, logout } from 'store/actions/session';

class LogoutConfirmation extends Component {
    render() {
        return (
            <Modal show={this.props.showLogoutConfirm} onHide={this.props.cancelLogout}>
                <Modal.Header closeButton>
                    <Modal.Title>Confirm Logout</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Are you sure you would like to logout?
                </Modal.Body>
                <Modal.Footer>
                    <button id="logoutCancel" type="button" className="btn btn-link" onClick={this.props.cancelLogout}>Cancel</button>
                    <button id="logoutLogout" type="button" className="btn btn-danger" onClick={this.props.logout}>Logout</button>
                </Modal.Footer>
            </Modal>
        );
    }
}

LogoutConfirmation.propTypes = {
    showLogoutConfirm: PropTypes.bool.isRequired,
    cancelLogout: PropTypes.func.isRequired,
    logout: PropTypes.func.isRequired
};

const mapStateToProps = (state) => ({
    showLogoutConfirm: state.session.showLogoutConfirm
});

const mapDispatchToProps = (dispatch) => ({
    logout: () => dispatch(logout()),
    cancelLogout: () => dispatch(cancelLogout())
});

export default connect(mapStateToProps, mapDispatchToProps)(LogoutConfirmation);
