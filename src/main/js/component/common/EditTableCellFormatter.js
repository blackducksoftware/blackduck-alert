import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

class EditTableCellFormatter extends Component {
    constructor(props) {
        super(props);
        this.onClick = this.onClick.bind(this);
    }

    onClick() {
        const { handleButtonClicked, currentRowSelected } = this.props;
        handleButtonClicked(currentRowSelected);
    }

    render() {
        let { buttonClass } = this.props;

        if (buttonClass) {
            buttonClass = `${buttonClass} tableButton`;
        } else {
            buttonClass = 'btn btn-link editJobButton';
        }

        return (
            <button className={buttonClass} type="button" title={this.props.buttonText} onClick={this.onClick}><FontAwesomeIcon icon="pencil-alt" className="alert-icon" size="lg" /></button>
        );
    }
}

EditTableCellFormatter.propTypes = {
    currentRowSelected: PropTypes.object.isRequired,
    handleButtonClicked: PropTypes.func.isRequired,
    buttonClass: PropTypes.string,
    buttonText: PropTypes.string
};

EditTableCellFormatter.defaultProps = {
    buttonText: 'Edit',
    buttonClass: null
};

export default EditTableCellFormatter;
