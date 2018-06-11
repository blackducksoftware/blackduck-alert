import React, { Component } from 'react';
import PropTypes from 'prop-types';
import CancelButton from '../../field/input/CancelButton';
import SubmitButton from '../../field/input/SubmitButton';
import GeneralButton from '../../field/input/GeneralButton';

const ConfigButtons = props => (
    <div className="form-group">
        <div className="col-sm-3" />
        <div className="col-sm-8">
            { props.includeTest &&
            <div style={{
                display: 'inline-block', paddingRight: '12px', marginRight: '12px', borderRight: '1px solid #aaa'
            }}
            >
                <GeneralButton onClick={props.onTestClick}>Test Configuration</GeneralButton>
            </div>
            }
            { props.includeSave &&
            <SubmitButton id={this.props.submitId}>Save</SubmitButton>
            }
            { props.includeCancel &&
            <CancelButton id={this.props.cancelId} onClick={props.onCancelClick} />
            }
        </div>
    </div>
);

ConfigButtons.propTypes = {
    cancelId: PropTypes.string,
    submitId: PropTypes.string,
    includeCancel: PropTypes.bool,
    includeSave: PropTypes.bool,
    includeTest: PropTypes.bool,
    onCancelClick: PropTypes.func,
    onTestClick: PropTypes.func
};

ConfigButtons.defaultProps = {
    cancelId: null,
    submitId: null,
    includeCancel: false,
    includeSave: true,
    includeTest: false,
    onCancelClick: () => {},
    onTestClick: () => {}
};

export default ConfigButtons;
