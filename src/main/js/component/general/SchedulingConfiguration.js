import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Select from 'react-select-2';

import { getSchedulingConfig, runSchedulingAccumulator, updateSchedulingConfig } from '../../store/actions/schedulingConfig';

import ConfigButtons from '../common/ConfigButtons';
import GeneralButton from '../../field/input/GeneralButton';

import { dailyDigestOptions, purgeOptions } from '../../util/scheduling-data';

class SchedulingConfiguration extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            accumulatorNextRun: 0
        };
        this.decreaseAccumulatorTime = this.decreaseAccumulatorTime.bind(this);
        this.handleDailyDigestChanged = this.handleDailyDigestChanged.bind(this);
        this.handlePurgeChanged = this.handlePurgeChanged.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        this.props.getConfig();
    }

    componentWillReceiveProps(nextProps) {
        this.cancelAutoTick();
        if (nextProps.accumulatorNextRun && nextProps.accumulatorNextRun !== '-1') {
            this.setState({
                accumulatorNextRun: parseInt(nextProps.accumulatorNextRun, 10)
            });
            this.startAutoTick();
        }

        const { dailyDigestHourOfDay, purgeDataFrequencyDays } = this.state;
        this.setState({ dailyDigestHourOfDay: dailyDigestHourOfDay || nextProps.dailyDigestHourOfDay || null });
        this.setState({ purgeDataFrequencyDays: purgeDataFrequencyDays || nextProps.purgeDataFrequencyDays || null });
    }

    componentWillUnmount() {
        this.cancelAutoTick();
    }

    startAutoTick() {
        // run every second
        this.tickInterval = setInterval(this.decreaseAccumulatorTime, 1000);
    }

    cancelAutoTick() {
        clearInterval(this.tickInterval);
        this.tickInterval = null;
    }


    decreaseAccumulatorTime() {
        if (this.state.accumulatorNextRun > 0) {
            this.setState({
                accumulatorNextRun: this.state.accumulatorNextRun - 1
            });
        } else {
            this.setState({
                accumulatorNextRun: 60
            });
            this.props.getConfig();
        }
    }

    handleSubmit(event) {
        event.preventDefault();
        event.stopPropagation();
        const { dailyDigestHourOfDay, purgeDataFrequencyDays } = this.state;
        this.props.updateConfig({ dailyDigestHourOfDay, purgeDataFrequencyDays });
    }

    handleDailyDigestChanged(option) {
        if (option) {
            this.setState({ dailyDigestHourOfDay: option.value });
        } else {
            this.setState({ dailyDigestHourOfDay: option });
        }
    }

    handlePurgeChanged(option) {
        if (option) {
            this.setState({ purgeDataFrequencyDays: option.value });
        } else {
            this.setState({ purgeDataFrequencyDays: option });
        }
    }


    render() {
        const {
            errorFields, errorMessage, updateStatus
        } = this.props;
        return (
            <div>
                <h1>
                    <span className="fa fa-clock-o" />
                    Scheduling
                </h1>
                { errorMessage && <div className="alert alert-danger">
                    { errorMessage }
                </div> }

                { updateStatus === 'UPDATED' && <div className="alert alert-success">
                    { 'Update successful' }
                </div> }

                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <div className="form-group">
                        <label className="col-sm-3 control-label">Collecting Hub notifications in</label>
                        <div className="col-sm-9">
                            <p className="form-control-static accumulator-countdown">
                                {this.state.accumulatorNextRun} seconds &nbsp;&nbsp;
                            </p>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="col-sm-3 control-label">Daily Digest Run Time</label>
                        <div className="col-sm-8">
                            <Select
                                id="scheduling-hour"
                                className="accumulatorTypeAheadField"
                                onChange={this.handleDailyDigestChanged}
                                searchable
                                options={dailyDigestOptions}
                                placeholder="Choose the hour of day"
                                value={this.state.dailyDigestHourOfDay}
                            />
                        </div>
                        { errorFields && errorFields.dailyDigestHourOfDay &&
                        <div className="col-sm-offset-3 col-sm-8">
                            <p className="fieldError">{errorFields.dailyDigestHourOfDay}</p>
                        </div> }
                    </div>

                    <div className="form-group">
                        <label className="col-sm-3 control-label">Daily Digest Cron Next Run</label>
                        <div className="col-sm-8">
                            <p className="form-control-static">{this.props.dailyDigestNextRun}</p>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="col-sm-3 control-label">Notification Purge Frequency</label>
                        <div className="col-sm-8">
                            <Select
                                id="scheduling-frequency"
                                className="accumulatorTypeAheadField"
                                onChange={this.handlePurgeChanged}
                                searchable
                                options={purgeOptions}
                                placeholder="Choose the frequency"
                                value={this.state.purgeDataFrequencyDays}
                            />
                        </div>
                        { errorFields && errorFields.purgeDataFrequencyDays &&
                        <div className="col-sm-offset-3 col-sm-8">
                            <p className="fieldError">{errorFields.purgeDataFrequencyDays}</p>
                        </div> }
                    </div>

                    <div className="form-group">
                        <label className="col-sm-3 control-label">Purge Cron Next Run</label>
                        <div className="col-sm-8">
                            <p className="form-control-static">{this.props.purgeDataNextRun}</p>
                        </div>
                    </div>

                    <ConfigButtons submitId="scheduling-submit" cancelId="scheduling-cancel" includeSave includeTest={false} />
                </form>
            </div>
        );
    }
}

SchedulingConfiguration.propTypes = {
    accumulatorNextRun: PropTypes.string,
    purgeDataNextRun: PropTypes.string,
    purgeDataFrequencyDays: PropTypes.number.isRequired,
    dailyDigestHourOfDay: PropTypes.number.isRequired,
    getConfig: PropTypes.func.isRequired,
    updateConfig: PropTypes.func.isRequired
};

SchedulingConfiguration.defaultProps = {
    accumulatorNextRun: '-1',
    purgeDataNextRun: '-'
};

const mapStateToProps = state => ({
    accumulatorNextRun: state.schedulingConfig.accumulatorNextRun,
    dailyDigestHourOfDay: state.schedulingConfig.dailyDigestHourOfDay,
    dailyDigestNextRun: state.schedulingConfig.dailyDigestNextRun,
    purgeDataFrequencyDays: state.schedulingConfig.purgeDataFrequencyDays,
    purgeDataNextRun: state.schedulingConfig.purgeDataNextRun,
    id: state.schedulingConfig.id,
    updateStatus: state.schedulingConfig.updateStatus,
    errorMessage: state.schedulingConfig.errorMessage,
    errorFields: state.schedulingConfig.errorFields
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getSchedulingConfig()),
    updateConfig: config => dispatch(updateSchedulingConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(SchedulingConfiguration);
