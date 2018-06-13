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
                <GeneralButton id="generalButton" onClick={props.onTestClick}>Test Configuration</GeneralButton>
            </div>
            }
            { props.includeSave &&
            <SubmitButton id="submitButton">Save</SubmitButton>
            }
            { props.includeCancel &&
            <CancelButton id="cancelButton" onClick={props.onCancelClick} />
            }
        </div>
    </div>
);

ConfigButtons.propTypes = {
    includeCancel: PropTypes.bool,
    includeSave: PropTypes.bool,
    includeTest: PropTypes.bool,
    onCancelClick: PropTypes.func,
    onTestClick: PropTypes.func
};

ConfigButtons.defaultProps = {
    includeCancel: false,
    includeSave: true,
    includeTest: false,
    onCancelClick: () => {},
    onTestClick: () => {}
};

export default ConfigButtons;
