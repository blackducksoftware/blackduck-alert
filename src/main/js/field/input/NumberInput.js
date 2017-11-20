import React from 'react';

import { numberInput } from '../../../css/main.css';

export default class NumberInput extends React.Component {
    render() {
        return (
            <input type="number" className={numberInput} name={this.props.name} value={this.props.value} onChange={this.props.onChange} />
        )
    }
}