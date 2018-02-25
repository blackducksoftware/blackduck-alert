import React, { Component } from 'react';
import PropTypes from 'prop-types';

import { tableButton, editJobButton } from '../../../css/table.css';

class EditTableCellFormatter extends Component {
    constructor(props) {
        super(props)
        this.onClick = this.onClick.bind(this);
    }

    onClick() {
        const { handleButtonClicked, currentRowSelected } = this.props;
        handleButtonClicked(currentRowSelected);
    }

    render() {
        const buttonText = this.props.buttonText || "Edit";
        var buttonClass = this.props.buttonClass;
        
        if (buttonClass) {
            buttonClass = `${buttonClass} ${tableButton}`;
        } else {
            buttonClass = `btn btn-link ${editJobButton}`;
        }

        return (
            <button className={buttonClass} type='button' title={buttonText} onClick={this.onClick}><span className="fa fa-pencil"></span></button>
        );
    }
}

EditTableCellFormatter.PropTypes = {
    currentRowSelected: PropTypes.object,
    handleButtonClicked: PropTypes.func.isRequired
}

export default EditTableCellFormatter;
