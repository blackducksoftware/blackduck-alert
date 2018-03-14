import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { getSchedulingConfig, runSchedulingAccumulator, updateSchedulingConfig } from '../../store/actions/schedulingConfig';

import ConfigButtons from '../common/ConfigButtons';
import GeneralButton from '../../field/input/GeneralButton';

import { dailyDigestOptions, purgeOptions } from '../../util/scheduling-data';

import Select from 'react-select-2';

class SchedulingConfiguration extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            inProgress: false
        };

        this.decreaseAccumulatorTime = this.decreaseAccumulatorTime.bind(this);
        this.handleDailyDigestChanged = this.handleDailyDigestChanged.bind(this);
        this.handlePurgeChanged = this.handlePurgeChanged.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentWillUnmount() {
		 this.cancelAutoTick();
    }

    componentWillReceiveProps(nextProps) {
        this.cancelAutoTick();
        if (nextProps.accumulatorNextRun && nextProps.accumulatorNextRun !== '-1') {
            this.setState({
                accumulatorNextRun: parseInt(nextProps.accumulatorNextRun)
            });
            this.startAutoTick();
        }

        const { dailyDigestHourOfDay, purgeDataFrequencyDays } = this.state;
        this.setState({ dailyDigestHourOfDay: dailyDigestHourOfDay || nextProps.dailyDigestHourOfDay || null });
        this.setState({ purgeDataFrequencyDays: purgeDataFrequencyDays || nextProps.purgeDataFrequencyDays || null });
    }

    componentDidMount() {
        this.props.getConfig();
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
            errorFields, errorMessage, updateStatus, runSchedulingAccumulator
        } = this.props;
        return (
            <div>
                <h1>Scheduling</h1>
                <form className="form-horizontal" onSubmit={this.handleSubmit}>

                    { errorMessage && <div className="alert alert-danger">
                        { errorMessage }
                    </div> }

                    { updateStatus === 'UPDATED' && <div className="alert alert-success">
                        { 'Update successful' }
                                                    </div> }

                    <div className="form-group">
                        <label className="col-sm-3 control-label">Collecting Hub notifications in</label>
                        <div className="col-sm-9">
                            <p className="form-control-static accumulator-countdown">
                                {this.state.accumulatorNextRun} seconds &nbsp;&nbsp;
                                <GeneralButton className="btn-xs btn-danger" onClick={runSchedulingAccumulator}>Run now</GeneralButton>
                            </p>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="col-sm-3 control-label">Daily Digest Frequency</label>
                        <div className="col-sm-8">
                            <Select
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
                            <p className="form-control-static">{this.props.purgeDataFrequencyDays}</p>
                        </div>
                    </div>

                    <ConfigButtons includeSave includeTest={false} />
                </form>
            </div>
        );
    }
}

SchedulingConfiguration.propTypes = {
    accumulatorNextRun: PropTypes.string,
    getConfig: PropTypes.func.isRequired,
    runSchedulingAccumulator: PropTypes.func.isRequired
};

SchedulingConfiguration.defaultProps = {
    accumulatorNextRun: '-1'
};

const mapStateToProps = state => ({
    accumulatorNextRun: state.schedulingConfig.accumulatorNextRun,
    dailyDigestHourOfDay: state.schedulingConfig.dailyDigestHourOfDay,
    dailyDigestNextRun: state.schedulingConfig.dailyDigestNextRun,
    purgeDataFrequencyDays: state.schedulingConfig.purgeDataFrequencyDays,
    purgeDataFrequencyDays: state.schedulingConfig.purgeDataFrequencyDays,
    id: state.schedulingConfig.id,
    updateStatus: state.schedulingConfig.updateStatus,
    errorMessage: state.schedulingConfig.errorMessage,
    errorFields: state.schedulingConfig.errorFields
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getSchedulingConfig()),
    runSchedulingAccumulator: () => dispatch(runSchedulingAccumulator()),
    updateConfig: config => dispatch(updateSchedulingConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(SchedulingConfiguration);
