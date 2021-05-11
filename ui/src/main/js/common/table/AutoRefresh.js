import React, { Component } from 'react';
import PropTypes from 'prop-types';
import CheckboxInput from 'common/input/CheckboxInput';
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
        const { refresh } = this.state;
        const { autoRefresh, isEnabled } = this.props;

        if ((prevProps.autoRefresh !== autoRefresh)) {
            this.setState({
                refresh: autoRefresh
            });
            this.toggleTimer();
        }

        if (prevState.refresh !== refresh) {
            this.toggleTimer();
            this.props.updateRefresh(refresh);
        }

        if (prevState.isEnabled !== isEnabled) {
            this.toggleTimer();
        }
    }

    componentWillUnmount() {
        clearInterval(this.timer);
    }

    toggleTimer() {
        const { refresh } = this.state;
        const { refreshRate, isEnabled } = this.props;

        if (refresh && isEnabled) {
            clearInterval(this.timer);
            this.timer = setInterval(() => this.props.startAutoReload(), refreshRate);
        } else {
            clearInterval(this.timer);
        }
    }

    render() {
        const { refresh } = this.state;
        const { label } = this.props;
        return (
            <CheckboxInput
                id="autoRefresh-id"
                label={label}
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
    isEnabled: PropTypes.bool,
    refreshRate: PropTypes.number,
    label: PropTypes.string
};

AutoRefresh.defaultProps = {
    autoRefresh: true,
    isEnabled: true,
    refreshRate: 30000,
    label: 'Enable Auto-Refresh'
};

const mapStateToProps = (state) => ({
    autoRefresh: state.refresh.autoRefresh
});

const mapDispatchToProps = (dispatch) => ({
    updateRefresh: (checked) => dispatch(updateRefresh(checked))
});

export default connect(mapStateToProps, mapDispatchToProps)(AutoRefresh);
