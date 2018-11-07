import React, {Component} from 'react';
import PropTypes from 'prop-types';

class SystemMessage extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        const {createdAt, content, severity} = this.props;
        return (<div>{severity} {createdAt} {content}</div>);
    }
}

SystemMessage.propTypes = {
    severity: PropTypes.string,
    createdAt: PropTypes.string,
    content: PropTypes.string
};

SystemMessage.defaultProps = {
    severity: 'INFO',
    createAt: '',
    content: ''
};

export default SystemMessage;

