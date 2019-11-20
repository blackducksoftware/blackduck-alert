import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
import RoleConfiguration from "./RoleConfiguration";

class RoleTable extends Component {

    constructor(props) {
        super(props);

        this.retrieveData = this.retrieveData.bind(this);
        this.createColumns = this.createColumns.bind(this);

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
        // this.props.fetchRoles();
    }

    createColumns() {
        const nameColumn = {
            header: 'name',
            headerLabel: 'Name',
            isKey: true
        }

        const permissions = {
            header: 'permissions',
            headerLabel: 'Permissions',
            isKey: false
        }

        return [nameColumn, permissions];
    }

    retrieveData() {
        return [];
    }

    render() {
        return (
            <div>
                <div>
                    <TableDisplay retrieveData={this.retrieveData} columns={this.createColumns()} insertModal={() => <RoleConfiguration />} />
                </div>
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
    // inProgress: state.roles.inProgress,
    // items: state.roles.data,
    // descriptors: state.descriptors.items
});

const mapDispatchToProps = dispatch => ({
    // createRole: (roleName) => dispatch(createNewRole(roleName)),
    // fetchRoles: () => dispatch(fetchRoles())
});

export default connect(mapStateToProps, mapDispatchToProps)(RoleTable);
