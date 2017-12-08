'use strict'
import React, { Component } from 'react';
import PropTypes from 'prop-types';

import { editJobButton } from '../../../css/distributionConfig.css';

class EditTableCellFormatter extends Component {
    constructor(props) {
        super(props)
        this.onClick = this.onClick.bind(this);
    }

    onClick() {
        const { setParentState, currentJobSelected } = this.props;
        setParentState('currentJobSelected', currentJobSelected);
    }

    render() {
        return (
            <input className={editJobButton} type='button' onClick={this.onClick} value='Edit'></input>
        );
    }
}

EditTableCellFormatter.PropTypes = {
    currentJobSelected: PropTypes.object,
    setParentState: PropTypes.func.isRequired
}

export default EditTableCellFormatter;
