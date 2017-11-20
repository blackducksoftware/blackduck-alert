import React from 'react';

import { checkboxInput } from '../../../css/main.css';

export default class CheckboxInput extends React.Component {
    render() {
        return (
            <input type="checkbox" className={checkboxInput} name={this.props.name} checked={this.props.isChecked} onChange={this.props.onChange} />
        )
    }
}