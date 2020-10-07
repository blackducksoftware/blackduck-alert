import React, { Component } from 'react';
import PropTypes from 'prop-types';
import ReadOnlyField from 'field/ReadOnlyField';

class CounterField extends Component {
    constructor(props) {
        super(props);

        this.state = {
            currentTime: this.props.value
        };
    }

    componentDidMount() {
        this.intervalId = setInterval(this.tickDown.bind(this), 1000);
    }

    componentWillUnmount() {
        clearInterval(this.intervalId);
    }

    tickDown() {
        const time = this.state.currentTime || this.props.value;
        const parsedTime = parseInt(time, 10);
        if (!Number.isNaN(parsedTime)) {
            const nextTime = (parsedTime <= 0) ? this.props.countdown : (parsedTime - 1);
            this.setState({
                currentTime: nextTime.toString()
            });
        }
    }

    render() {
        const updatedCount = { ...this.props, value: this.state.currentTime };
        return (
            <div>
                <ReadOnlyField {...updatedCount} />
            </div>
        );
    }
}

CounterField.propTypes = {
    countdown: PropTypes.number.isRequired,
    value: PropTypes.string
};

CounterField.defaultProps = {
    value: ''
};

export default CounterField;
