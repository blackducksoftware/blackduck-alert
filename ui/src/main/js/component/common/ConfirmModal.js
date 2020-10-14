import React, { Component } from 'react';
import { Modal } from 'react-bootstrap';
import PropTypes from 'prop-types';

class ConfirmModal extends Component {
    constructor(props) {
        super(props);
        this.handleAffirmativeClick = this.handleAffirmativeClick.bind(this);
        this.handleNegativeClick = this.handleNegativeClick.bind(this);
        this.resetState = this.resetState.bind(this);
        this.state = {
            show: props.showModal,
            confirmed: false
        };
    }

    componentDidUpdate(prevProps) {
        if (prevProps.showModal != this.props.showModal) {
            this.setState({
                show: this.props.showModal
            });
        }
    }

    handleAffirmativeClick(event) {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        this.props.affirmativeAction();
        this.resetState();
    }

    handleNegativeClick(event) {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }
        this.props.negativeAction();
        this.resetState();
    }

    resetState() {
        this.setState({
            show: false,
            confirmed: false
        });
    }

    render() {
        const {
            id, affirmativeButtonText, message, negativeButtonText, title
        } = this.props;
        return (
            <Modal id={id} show={this.state.show} onHide={this.handleNegativeClick}>
                <Modal.Header closeButton>
                    <Modal.Title>{title}</Modal.Title>
                </Modal.Header>
                {message && (
                    <Modal.Body>
                        {message}
                    </Modal.Body>
                )}
                <Modal.Footer>
                    <button
                        id="confirmNegative"
                        type="button"
                        className="btn btn-link"
                        onClick={this.handleNegativeClick}
                    >
                        {negativeButtonText}
                    </button>
                    <button
                        id="confirmAffirmative"
                        type="button"
                        className="btn btn-danger"
                        onClick={this.handleAffirmativeClick}
                    >
                        {affirmativeButtonText}
                    </button>
                </Modal.Footer>
            </Modal>
        );
    }
}

ConfirmModal.propTypes = {
    id: PropTypes.string,
    affirmativeAction: PropTypes.func.isRequired,
    affirmativeButtonText: PropTypes.string,
    message: PropTypes.string.isRequired,
    negativeAction: PropTypes.func,
    negativeButtonText: PropTypes.string,
    showModal: PropTypes.bool.isRequired,
    title: PropTypes.string.isRequired
};

ConfirmModal.defaultProps = {
    id: 'confirmModal',
    affirmativeButtonText: 'Yes',
    negativeButtonText: 'No',
    negativeAction: () => true
};

export default ConfirmModal;
