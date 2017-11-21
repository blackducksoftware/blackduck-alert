import React from 'react';

import { textInput } from '../../../css/main.css';

export default class TextInput extends React.Component {
    constructor(props) {
        super(props);
    }
    
    render() {
        return (
            <input type="text" className={textInput} name={this.props.name} value={this.props.value} onChange={this.props.onChange} />
        )
    }
}