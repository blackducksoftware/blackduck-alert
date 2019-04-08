import React, { Component } from 'react';
import PropTypes from 'prop-types';

class FadeField extends Component {
    constructor(props) {
        super(props);

        this.state = {
            showChildren: true,
            removeChildren: false
        };
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.children !== prevProps.children) {
            const self = this;
            // This will trigger the fade after the specified time
            setTimeout(function () {
                self.setState({
                    showChildren: false
                });
            }, this.props.timeout);

            // This should remove the children after the fade
            setTimeout(function () {
                self.setState({
                    removeChildren: true
                });
            }, this.props.timeout + 2000);
        }
    }

    render() {
        let { children } = this.props;
        let clazz = 'visibleField';
        if (!this.state.showChildren) {
            clazz = 'fadingField';
        }
        if (this.state.removeChildren) {
            children = null;
        }
        return (
            <div className={clazz}>
                {children}
            </div>
        );
    }
}

FadeField.propTypes = {
    children: PropTypes.any,
    timeout: PropTypes.number
};

FadeField.defaultProps = {
    children: null,
    timeout: 5000
};

export default FadeField;
