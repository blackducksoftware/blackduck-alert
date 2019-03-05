import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import Tooltip from 'react-bootstrap/Tooltip';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';


import { getSchedulingConfig, updateSchedulingConfig } from 'store/actions/schedulingConfig';

import ConfigButtons from 'component/common/ConfigButtons';

import { dailyDigestOptions, purgeOptions } from 'util/scheduling-data';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import SelectInput from 'field/input/SelectInput';

const KEY_DAILY_DIGEST_HOUR_OF_DAY = 'scheduling.daily.processor.hour';
const KEY_PURGE_DATA_FREQUENCY_DAYS = 'scheduling.purge.data.frequency';
const KEY_ACCUMULATOR_NEXT_RUN = 'scheduling.accumulator.next.run';
const KEY_DAILY_DIGEST_NEXT_RUN = 'scheduling.daily.processor.next.run';
const KEY_PURGE_DATA_NEXT_RUN = 'scheduling.purge.data.next.run';


const fieldDescriptions = {
    [KEY_DAILY_DIGEST_HOUR_OF_DAY]: 'Select the hour of the day to run the the daily digest distribution jobs.',
    [KEY_PURGE_DATA_FREQUENCY_DAYS]: 'Choose a frequency for cleaning up provider data; the default value is three days. When the purge runs, it deletes all data that is older than the selected value. EX: data older than 3 days will be deleted.',
    [KEY_ACCUMULATOR_NEXT_RUN]: 'By default, Alert collects data every 60 seconds. This value indicates the number of seconds until the next time Alert pulls data from the Providers.',
    [KEY_DAILY_DIGEST_NEXT_RUN]: 'This is the next time daily digest distribution jobs will run.',
    [KEY_PURGE_DATA_NEXT_RUN]: 'This is the next time Alert will purge provider data.'
};

const fieldNames = [KEY_ACCUMULATOR_NEXT_RUN, KEY_DAILY_DIGEST_HOUR_OF_DAY, KEY_DAILY_DIGEST_NEXT_RUN, KEY_PURGE_DATA_FREQUENCY_DAYS, KEY_PURGE_DATA_NEXT_RUN];

class SchedulingConfiguration extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            currentConfig: FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_SCHEDULING)
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
        const newState = FieldModelUtilities.checkModelOrCreateEmpty(nextProps.currentConfig, fieldNames);
        this.setState({
            currentConfig: newState
        });

        let nextRun = FieldModelUtilities.getFieldModelSingleValue(newState, KEY_ACCUMULATOR_NEXT_RUN);
        if (!nextRun) {
            nextRun = 60;
        } else {
            nextRun = parseInt(nextRun, 10);
            if (nextRun <= 0) {
                nextRun = 60;
            }
        }
        const updatedState = FieldModelUtilities.updateFieldModelSingleValue(newState, KEY_ACCUMULATOR_NEXT_RUN, nextRun);
        this.setState({
            currentConfig: updatedState
        });
        this.startAutoTick();
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
        const nextRunString = FieldModelUtilities.getFieldModelSingleValue(this.state.currentConfig, KEY_ACCUMULATOR_NEXT_RUN);
        const nextRun = parseInt(nextRunString, 10);
        if (nextRun <= 0) {
            this.props.getConfig();
        } else {
            const decrementedValue = nextRun - 1;
            const updatedState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, KEY_ACCUMULATOR_NEXT_RUN, decrementedValue);
            this.setState({
                currentConfig: updatedState
            });
        }
    }

    handleSubmit(event) {
        event.preventDefault();
        event.stopPropagation();
        this.props.updateConfig(this.state.currentConfig);
    }

    handleDailyDigestChanged(option) {
        if (option) {
            const selected = option.value;
            const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, KEY_DAILY_DIGEST_HOUR_OF_DAY, selected);
            this.setState({
                currentConfig: newState
            });
        } else {
            const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, KEY_DAILY_DIGEST_HOUR_OF_DAY, null);
            this.setState({
                currentConfig: newState
            });
        }
    }

    handlePurgeChanged(option) {
        if (option) {
            const selected = option.value;
            const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, KEY_PURGE_DATA_FREQUENCY_DAYS, selected);
            this.setState({
                currentConfig: newState
            });
        } else {
            const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentConfig, KEY_PURGE_DATA_FREQUENCY_DAYS, null);
            this.setState({
                currentConfig: newState
            });
        }
    }

    render() {
        const fieldModel = this.state.currentConfig;
        const { fieldErrors, errorMessage, updateStatus } = this.props;
        return (
            <div>
                <h1>
                    <span className="fa fa-clock-o" />
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
                        <label className="col-sm-4 col-form-label text-right">Collecting Provider data in</label>
                        <div className="d-inline-flex">
                            <OverlayTrigger
                                key="top"
                                placement="top"
                                delay={{ show: 200, hide: 100 }}
                                overlay={
                                    <Tooltip id="description-tooltip">
                                        {FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_ACCUMULATOR_NEXT_RUN)}
                                    </Tooltip>
                                }
                            >
                                <span className="fa fa-question-circle" />
                            </OverlayTrigger>
                        </div>
                        <div className="d-inline-flex p-2 col-sm-4">
                            <p className="form-control-static accumulator-countdown">
                                {FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_ACCUMULATOR_NEXT_RUN)} seconds &nbsp;&nbsp;
                            </p>
                        </div>
                    </div>

                    <SelectInput
                        label="Daily Digest Run Time"
                        onChange={this.handleDailyDigestChanged}
                        id={KEY_DAILY_DIGEST_HOUR_OF_DAY}
                        className="accumulatorTypeAheadField"
                        labelClass="col-sm-4"
                        description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_DAILY_DIGEST_HOUR_OF_DAY)}
                        options={dailyDigestOptions}
                        isSearchable
                        placeholder="Choose the hour of day"
                        value={dailyDigestOptions.find((option) => {
                            const runHour = FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_DAILY_DIGEST_HOUR_OF_DAY);
                            return option.value === runHour;
                        })}
                        errorName="Daily Digest errors"
                        errorValue={fieldErrors.dailyDigestHourOfDay}
                    />

                    <div className="form-group">
                        <label className="col-sm-4 col-form-label text-right">Daily Digest Cron Next Run</label>
                        <div className="d-inline-flex">
                            <OverlayTrigger
                                key="top"
                                placement="top"
                                delay={{ show: 200, hide: 100 }}
                                overlay={
                                    <Tooltip id="description-tooltip">
                                        {FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_DAILY_DIGEST_NEXT_RUN)}
                                    </Tooltip>
                                }
                            >
                                <span className="fa fa-question-circle" />
                            </OverlayTrigger>
                        </div>
                        <div className="d-inline-flex p-2 col-sm-4">
                            <p className="form-control-static">
                                {FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_DAILY_DIGEST_NEXT_RUN)}
                            </p>
                        </div>
                    </div>

                    <SelectInput
                        label="Data Purge Frequency"
                        onChange={this.handlePurgeChanged}
                        id={KEY_PURGE_DATA_FREQUENCY_DAYS}
                        className="accumulatorTypeAheadField"
                        labelClass="col-sm-4"
                        description={FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_PURGE_DATA_FREQUENCY_DAYS)}
                        options={purgeOptions}
                        isSearchable
                        placeholder="Choose the frequency"
                        value={purgeOptions.find(option => option.value === FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_PURGE_DATA_FREQUENCY_DAYS))}
                        errorName="Purge errors"
                        errorValue={fieldErrors.purgeDataFrequencyDays}
                    />

                    <div className="form-group">
                        <label className="col-sm-4 col-form-label text-right">Purge Cron Next Run</label>
                        <div className="d-inline-flex">
                            <OverlayTrigger
                                key="top"
                                placement="top"
                                delay={{ show: 200, hide: 100 }}
                                overlay={
                                    <Tooltip id="description-tooltip">
                                        {FieldModelUtilities.getFieldDescription(fieldDescriptions, KEY_PURGE_DATA_NEXT_RUN)}
                                    </Tooltip>
                                }
                            >
                                <span className="fa fa-question-circle" />
                            </OverlayTrigger>
                        </div>
                        <div className="d-inline-flex p-2 col-sm-4">
                            <p className="form-control-static">
                                {FieldModelUtilities.getFieldModelSingleValue(fieldModel, KEY_PURGE_DATA_NEXT_RUN)}
                            </p>
                        </div>
                    </div>
                    <ConfigButtons submitId="scheduling-submit" cancelId="scheduling-cancel" includeSave includeTest={false} />
                </form>
            </div>
        );
    }
}

SchedulingConfiguration.propTypes = {
    currentConfig: PropTypes.object,
    fieldErrors: PropTypes.object,
    errorMessage: PropTypes.string,
    updateStatus: PropTypes.string,
    getConfig: PropTypes.func.isRequired,
    updateConfig: PropTypes.func.isRequired
};

SchedulingConfiguration.defaultProps = {
    currentConfig: {},
    fieldErrors: {},
    errorMessage: '',
    updateStatus: ''
};

const mapStateToProps = state => ({
    currentConfig: state.schedulingConfig.config,
    updateStatus: state.schedulingConfig.updateStatus,
    errorMessage: state.schedulingConfig.error.message,
    fieldErrors: state.schedulingConfig.error.fieldErrors
});

// Mapping redux actions -> react props
const mapDispatchToProps = dispatch => ({
    getConfig: () => dispatch(getSchedulingConfig()),
    updateConfig: config => dispatch(updateSchedulingConfig(config))
});

export default connect(mapStateToProps, mapDispatchToProps)(SchedulingConfiguration);
