import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from "prop-types";
import { createNewRole, fetchRoles } from 'store/actions/roles';

class RoleTable extends Component {

    constructor(props) {
        super(props);
        this.state = {
            items: [],
            currentPage: 1,
            currentPageSize: 10,
            searchTerm: '',
            sortField: 'lastSent',
            sortOrder: 'desc',
            currentRowSelected: {},
            showDetailModal: false
        };
    }

    componentDidMount() {
        this.props.fetchRoles();
    }

    render() {
        return (
            <div>
                <div>Role Table Goes Here....</div>
            </div>
        );
    }
}

RoleTable.defaultProps = {
    inProgress: false,
    message: '',
    autoRefresh: true,
    fetching: false,
    totalPageCount: 0,
    descriptors: [],
    items: []
};

RoleTable.propTypes = {
    inProgress: PropTypes.bool,
    autoRefresh: PropTypes.bool,
    items: PropTypes.arrayOf(PropTypes.object),
    descriptors: PropTypes.arrayOf(PropTypes.object)
};

const mapStateToProps = state => ({
    inProgress: state.roles.inProgress,
    items: state.roles.data,
    descriptors: state.descriptors.items
});

const mapDispatchToProps = dispatch => ({
    createRole: (roleName) => dispatch(createNewRole(roleName)),
    fetchRoles: () => dispatch(fetchRoles())
});

export default connect(mapStateToProps, mapDispatchToProps)(RoleTable);
