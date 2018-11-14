import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import Select from 'react-select';

import {getSchedulingConfig, updateSchedulingConfig} from '../../store/actions/schedulingConfig';

import ConfigButtons from '../common/ConfigButtons';

import {dailyDigestOptions, purgeOptions} from '../../util/scheduling-data';

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

        var nextRun = nextProps.accumulatorNextRun;
        if (!nextRun) {
            nextRun = 60;
        } else {
            nextRun = parseInt(nextRun, 10);
            if (nextRun <= 0) {
                nextRun = 60;
            }
        }
        this.setState({
            accumulatorNextRun: nextRun
        });
        this.startAutoTick();

        const {dailyDigestHourOfDay, purgeDataFrequencyDays} = this.state;
        this.setState({dailyDigestHourOfDay: dailyDigestHourOfDay || nextProps.dailyDigestHourOfDay || null});
        this.setState({purgeDataFrequencyDays: purgeDataFrequencyDays || nextProps.purgeDataFrequencyDays || null});
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
        if (this.state.accumulatorNextRun <= 0) {
            this.props.getConfig();
        } else {
            this.setState({
                accumulatorNextRun: this.state.accumulatorNextRun - 1
            });
        }
    }

    handleSubmit(event) {
        event.preventDefault();
        event.stopPropagation();
        const {dailyDigestHourOfDay, purgeDataFrequencyDays} = this.state;
        this.props.updateConfig({dailyDigestHourOfDay, purgeDataFrequencyDays});
    }

    handleDailyDigestChanged(option) {
        if (option) {
            this.setState({dailyDigestHourOfDay: option.value});
        } else {
            this.setState({dailyDigestHourOfDay: option});
        }
    }

    handlePurgeChanged(option) {
        if (option) {
            this.setState({purgeDataFrequencyDays: option.value});
        } else {
            this.setState({purgeDataFrequencyDays: option});
        }
    }

    render() {
        const {
            errorFields, errorMessage, updateStatus
        } = this.props;
        return (
            <div>
                <h1>
                    <span className="fa fa-clock-o"/>
                    Scheduling
                </h1>
                {errorMessage && <div className="alert alert-danger">
                    {errorMessage}
                </div>}

                {updateStatus === 'UPDATED' && <div className="alert alert-success">
                    {'Update successful'}
                </div>}

                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <div className="form-group">
                        <label className="col-sm-4 col-form-label text-right">Collecting Black Duck notifications in</label>
                        <div className="d-inline-flex p-2 col-sm-9">
                            <p className="form-control-static accumulator-countdown">
                                {this.state.accumulatorNextRun} seconds &nbsp;&nbsp;
                            </p>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="col-sm-4 col-form-label text-right">Daily Digest Run Time</label>
                        <div className="d-inline-flex p-2 col-sm-9">
                            <Select
                                id="schedulingConfigurationHour"
                                className="accumulatorTypeAheadField"
                                onChange={this.handleDailyDigestChanged}
                                isSearchable={true}
                                options={dailyDigestOptions}
                                placeholder="Choose the hour of day"
                                value={dailyDigestOptions.find(option => option.value === this.state.dailyDigestHourOfDay)}
                            />
                        </div>
                        {errorFields && errorFields.dailyDigestHourOfDay &&
                        <div className="offset-sm-3 col-sm-9">
                            <p className="fieldError">{errorFields.dailyDigestHourOfDay}</p>
                        </div>}
                    </div>

                    <div className="form-group">
                        <label className="col-sm-4 col-form-label text-right">Daily Digest Cron Next Run</label>
                        <div className="d-inline-flex p-2 col-sm-9">
                            <p className="form-control-static">{this.props.dailyDigestNextRun}</p>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="col-sm-4 col-form-label text-right">Notification Purge Frequency</label>
                        <div className="d-inline-flex p-2 col-sm-9">
                            <Select
                                id="schedulingConfigurationFrequency"
                                className="accumulatorTypeAheadField"
                                onChange={this.handlePurgeChanged}
                                isSearchable={true}
                                options={purgeOptions}
                                placeholder="Choose the frequency"
                                value={purgeOptions.find(option => option.value === this.state.purgeDataFrequencyDays)}
                            />
                        </div>
                        {errorFields && errorFields.purgeDataFrequencyDays &&
                        <div className="offset-sm-3 col-sm-9">
                            <p className="fieldError">{errorFields.purgeDataFrequencyDays}</p>
                        </div>}
                    </div>

                    <div className="form-group">
                        <label className="col-sm-4 col-form-label text-right">Purge Cron Next Run</label>
                        <div className="d-inline-flex p-2 col-sm-9">
                            <p className="form-control-static">{this.props.purgeDataNextRun}</p>
                        </div>
                    </div>

                    <ConfigButtons submitId="scheduling-submit" cancelId="scheduling-cancel" includeSave includeTest={false}/>
                </form>
            </div>
        );
    }
}

SchedulingConfiguration.propTypes = {
    accumulatorNextRun: PropTypes.string,
    purgeDataNextRun: PropTypes.string,
    purgeDataFrequencyDays: PropTypes.string,
    dailyDigestHourOfDay: PropTypes.string,
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
