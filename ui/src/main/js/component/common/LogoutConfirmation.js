import React from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import { connect } from 'react-redux';
import { cancelLogout, logout } from 'store/actions/session';

function LogoutConfirmation({ showLogoutConfirm, cancelLogoutClick, logoutClick }) {
    return (
        <Modal show={showLogoutConfirm} onHide={cancelLogoutClick}>
            <Modal.Header closeButton>
                <Modal.Title>Confirm Logout</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                Are you sure you would like to logout?
            </Modal.Body>
            <Modal.Footer>
                <button id="logoutCancel" type="button" className="btn btn-link" onClick={cancelLogoutClick}>Cancel</button>
                <button id="logoutLogout" type="button" className="btn btn-danger" onClick={logoutClick}>Logout</button>
            </Modal.Footer>
        </Modal>
    );
}

LogoutConfirmation.propTypes = {
    showLogoutConfirm: PropTypes.bool.isRequired,
    cancelLogoutClick: PropTypes.func.isRequired,
    logoutClick: PropTypes.func.isRequired
};

const mapStateToProps = (state) => ({
    showLogoutConfirm: state.session.showLogoutConfirm
});

const mapDispatchToProps = (dispatch) => ({
    logoutClick: () => dispatch(logout()),
    cancelLogoutClick: () => dispatch(cancelLogout())
});

export default connect(mapStateToProps, mapDispatchToProps)(LogoutConfirmation);
