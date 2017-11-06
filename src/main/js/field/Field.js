import React from 'react';
import Field from '../field/Field';

class Field extends React.Component {
    render() {
        let inputDiv = null;
        if (this.props.type == 'text' || this.props.type == 'password' || this.props.type == 'number'){
            inputDiv = <input type={this.props.type} name={this.props.name} value={this.props.value} onChange={this.props.onChange}></input>
        } else if (this.props.type == 'checkbox'){
            let isChecked = false;
            let value = this.props.value;
            if (value){
                	 isChecked = JSON.parse(value);
            }
            inputDiv = <input type={this.props.type} name={this.props.name} checked={isChecked} onChange={this.props.onChange}></input>
        }
        
        let errorDiv = null;
        if (this.props.errorName && this.props.errorValue) {
            errorDiv = <p name={this.props.errorName}>{this.props.errorValue}</p>;
        }
        return (
             <div>
                 <label>{this.props.label}</label>
                 {inputDiv}
                 {errorDiv}
             </div>
         )
    }
}

export default Field;