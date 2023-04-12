import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { cancelLogout, logout } from 'store/actions/session';
import Modal from 'common/component/modal/Modal';

const LogoutConfirmation = ({ cancelLogout, logout, showLogoutConfirm }) => {
    return (
        <Modal
            isOpen={showLogoutConfirm}
            size="sm"
            title="Confirm Logout"
            closeModal={cancelLogout}
            handleCancel={cancelLogout}
            handleSubmit={logout}
            submitText="Logout"
            style="delete"
        >
            <div className="modal-description">
                Are you sure you would like to logout?
            </div>
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
