import React from 'react';
import PropTypes from 'prop-types';
import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { cancelLogout, logout } from 'store/actions/session';
import Modal from 'common/component/modal/Modal';

const LogoutConfirmation = ({ showLogoutConfirm }) => {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    
    const handleLogout = () => {
        dispatch(logout(navigate));
    };

    const handleCancelLogout = () => {
        dispatch(cancelLogout());
    }

    return (
        <Modal
            isOpen={showLogoutConfirm}
            size="sm"
            title="Confirm Logout"
            closeModal={handleCancelLogout}
            handleCancel={handleCancelLogout}
            handleSubmit={handleLogout}
            submitText="Logout"
            buttonStyle="action"
        >
            <div>
                Are you sure you would like to logout?
            </div>
        </Modal>
    );
};

LogoutConfirmation.propTypes = {
    showLogoutConfirm: PropTypes.bool.isRequired
};

export default LogoutConfirmation;
