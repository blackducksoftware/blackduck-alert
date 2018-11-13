import React, {Component} from 'react';
import PropTypes from 'prop-types';
import '../../../css/messages.scss';

class SystemMessage extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        const {createdAt, content, type} = this.props;
        return (<div className="messageHeader">
            <span className={this.getIcon()} aria-hidden="true" title={type}/><span className="messageDate">{createdAt}</span>
            <div>{content}</div>
        </div>);
    }

    getIcon() {
        const {type} = this.props;
        const errorIcon = "fa fa-exclamation-triangle ";
        if (type == 'ERROR') {
            return `${errorIcon} errorStatus`;
        } else if (type == 'WARNING') {
            return `${errorIcon} warningStatus`
        } else {
            return "fa fa-check-circle validStatus"
        }
    }
}

SystemMessage.propTypes = {
    type: PropTypes.string,
    createdAt: PropTypes.string,
    content: PropTypes.string
};

SystemMessage.defaultProps = {
    type: 'INFO',
    createAt: '',
    content: ''
};

export default SystemMessage;

