import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { updateRefresh } from 'store/actions/refresh';
import CheckboxInput from 'field/input/CheckboxInput';

class AutoRefresh extends Component {
    constructor(props) {
        super(props);
        this.handleAutoRefreshChange = this.handleAutoRefreshChange.bind(this);
    }

    handleAutoRefreshChange({ target }) {
        const { checked } = target;
        if (checked) {
            this.props.startAutoReload();
        } else {
            this.props.cancelAutoReload();
        }
        this.props.updateRefresh(checked);
    }

    render() {
        return (
            <CheckboxInput
                id="autoRefresh-id"
                label="Enable Auto-Refresh"
                name="autoRefresh"
                showDescriptionPlaceHolder={false}
                labelClass=""
                isChecked={this.props.autoRefresh}
                onChange={this.handleAutoRefreshChange}
            />
        );
    }
}

AutoRefresh.propTypes = {
    autoRefresh: PropTypes.bool.isRequired,
    startAutoReload: PropTypes.func.isRequired,
    cancelAutoReload: PropTypes.func.isRequired,
    updateRefresh: PropTypes.func.isRequired
};

const mapStateToProps = state => ({
    autoRefresh: state.refresh.autoRefresh
});
const mapDispatchToProps = dispatch => ({
    updateRefresh: checked => dispatch(updateRefresh(checked))
});
export default connect(mapStateToProps, mapDispatchToProps)(AutoRefresh);
