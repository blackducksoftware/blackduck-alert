import React from 'react';
import LabeledField from '../LabeledField';

export default class NumberInput extends LabeledField {
    render() {
        const { inputClass, id } = this.props;
        const className = inputClass || 'form-control';

        let inputDiv = null;
        if (this.props.readOnly) {
            inputDiv = <div className="col-sm-3"><input id={id} type="number" readOnly className={className} name={this.props.name} value={this.props.value} /></div>;
        } else {
            inputDiv = <div className="col-sm-3"><input id={id} type="number" className={className} name={this.props.name} value={this.props.value} onChange={this.props.onChange} /></div>;
        }
        return (
            super.render(inputDiv)
        );
    }
}
