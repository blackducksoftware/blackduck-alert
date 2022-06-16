import React from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import { connect } from 'react-redux';
import { cancelLogout, logout } from 'store/actions/session';

const LogoutConfirmation = ({ cancelLogout, logout, showLogoutConfirm }) => {
    return (
        <Modal show={showLogoutConfirm} onHide={cancelLogout}>
            <Modal.Header closeButton>
                <Modal.Title>Confirm Logout</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                Are you sure you would like to logout?
            </Modal.Body>
            <Modal.Footer>
                <button id="logoutCancel" type="button" className="btn btn-link" onClick={cancelLogout}>Cancel</button>
                <button id="logoutLogout" type="button" className="btn btn-danger" onClick={logout}>Logout</button>
            </Modal.Footer>
        </Modal>
    );
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
