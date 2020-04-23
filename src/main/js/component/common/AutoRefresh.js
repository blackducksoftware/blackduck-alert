import React, { Component } from 'react';
import PropTypes from 'prop-types';
import CheckboxInput from 'field/input/CheckboxInput';
import { connect } from 'react-redux';
import { updateRefresh } from 'store/actions/refresh';

class AutoRefresh extends Component {
    constructor(props) {
        super(props);

        this.state = ({
            refresh: this.props.autoRefresh
        });

        this.toggleTimer = this.toggleTimer.bind(this);

        if (this.props.autoRefresh) {
            this.toggleTimer();
        }
    }

    componentDidUpdate(prevProps, prevState) {
        const { autoRefresh } = this.props;
        if ((prevProps.autoRefresh !== autoRefresh)) {
            this.setState({
                refresh: this.props.autoRefresh
            });
            this.toggleTimer();
        }

        if (prevState.refresh !== this.state.refresh) {
            this.toggleTimer();
            this.props.updateRefresh(this.state.refresh);
        }
    }

    componentWillUnmount() {
        clearInterval(this.timer);
    }

    toggleTimer() {
        if (this.state.refresh) {
            clearInterval(this.timer);
            this.timer = setInterval(() => this.props.startAutoReload(), this.props.refreshRate);
        } else {
            clearInterval(this.timer);
        }
    }

    render() {
        const { refresh } = this.state;
        return (
            <CheckboxInput
                id="autoRefresh-id"
                label={this.props.label}
                name="autoRefresh"
                showDescriptionPlaceHolder={false}
                labelClass="tableCheckbox"
                isChecked={refresh}
                onChange={() => this.setState({ refresh: !refresh })}
            />
        );
    }
}

AutoRefresh.propTypes = {
    startAutoReload: PropTypes.func.isRequired,
    updateRefresh: PropTypes.func.isRequired,
    autoRefresh: PropTypes.bool,
    refreshRate: PropTypes.number,
    label: PropTypes.string
};

AutoRefresh.defaultProps = {
    autoRefresh: true,
    refreshRate: 10000,
    label: 'Enable Auto-Refresh'
};

const mapStateToProps = state => ({
    autoRefresh: state.refresh.autoRefresh
});

const mapDispatchToProps = dispatch => ({
    updateRefresh: checked => dispatch(updateRefresh(checked))
});

export default connect(mapStateToProps, mapDispatchToProps)(AutoRefresh);
