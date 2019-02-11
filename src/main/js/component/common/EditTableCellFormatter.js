import React, { Component } from 'react';
import PropTypes from 'prop-types';

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
            <button className={buttonClass} type="button" title={this.props.buttonText} onClick={this.onClick}><span className="fa fa-pencil" /></button>
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
