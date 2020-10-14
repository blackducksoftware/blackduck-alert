import React, { Component } from 'react';
import PropTypes from 'prop-types';

class FadeField extends Component {
    constructor(props) {
        super(props);

        this.state = {
            showChildren: true,
            removeChildren: false
        };

        this.displayTimer = null;
        this.hideTimer = null;
    }

    componentDidMount() {
        const self = this;
        this.displayTimer = setTimeout(() => {
            self.setState({
                showChildren: false
            });
        }, this.props.timeout);

        this.hideTimer = setTimeout(() => {
            self.setState({
                removeChildren: true
            });
        }, this.props.timeout + 2000);
    }

    componentDidUpdate(prevProps) {
        if (this.props.children !== prevProps.children) {
            if (this.displayTimer !== null) {
                clearTimeout(this.displayTimer);
            }
            if (this.hideTimer !== null) {
                clearTimeout(this.hideTimer);
            }

            const self = this;
            // This will trigger the fade after the specified time
            this.displayTimer = setTimeout(() => {
                self.setState({
                    showChildren: false
                });
            }, this.props.timeout);

            // This should remove the children after the fade
            this.hideTimer = setTimeout(() => {
                self.setState({
                    removeChildren: true
                });
            }, this.props.timeout + 2000);
        }
    }

    componentWillUnmount() {
        clearTimeout(this.displayTimer);
        clearTimeout(this.hideTimer);
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
