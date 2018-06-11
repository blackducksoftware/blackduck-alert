import React from 'react';

import LabeledField from '../LabeledField';

export default class PasswordInput extends LabeledField {
    render() {
        const { isSet, inputClass } = this.props;

        const placeholderText = (isSet) ? '***********' : null;
        const className = inputClass || 'form-control';

        if (this.props.readOnly) {
            return super.render(<div className="col-sm-8">
                <input id={this.props.id} type="password" readOnly className={className} name={this.props.name} value={this.props.value} placeholder={placeholderText} />
            </div>);
        }

        return super.render(<div className="col-sm-8">
            <input id={this.props.id} type="password" className={className} name={this.props.name} value={this.props.value} onChange={this.props.onChange} placeholder={placeholderText} />
        </div>);
    }
}
