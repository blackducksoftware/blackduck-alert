import React from 'react';

import { checkboxInput } from '../../../css/field.css';

export default class CheckboxInput extends React.Component {
    constructor(props) {
        super(props);
    }
    
    render() {
        return (
            <input type="checkbox" className={checkboxInput} name={this.props.name} checked={this.props.isChecked} onChange={this.props.onChange} />
        )
    }
}