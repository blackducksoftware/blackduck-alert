import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {updateRefresh} from '../../store/actions/refresh';
import {connect} from "react-redux";

class AutoRefresh extends Component {
    constructor(props) {
        super(props);
        this.handleAutoRefreshChange = this.handleAutoRefreshChange.bind(this);
    }

    handleAutoRefreshChange({target}) {
        const {name, checked} = target;
        if (checked) {
            this.props.startAutoReload();
        } else {
            this.props.cancelAutoReload();
        }
        this.props.updateRefresh(checked);
    }


    render() {
        return (
            <label className="refreshCheckbox"><input name="autoRefresh" type="checkbox" checked={this.props.autoRefresh} onChange={this.handleAutoRefreshChange}/> Enable Auto-Refresh</label>
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
    updateRefresh: (checked) => dispatch(updateRefresh(checked))
});

export default connect(mapStateToProps, mapDispatchToProps)(AutoRefresh);
