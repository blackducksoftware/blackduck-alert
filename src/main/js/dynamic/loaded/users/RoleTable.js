import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
import RoleConfiguration from 'dynamic/loaded/users/RoleConfiguration';
import * as DescriptorUtilities from 'util/descriptorUtilities';

class RoleTable extends Component {

    constructor(props) {
        super(props);

        this.retrieveData = this.retrieveData.bind(this);
        this.createColumns = this.createColumns.bind(this);
        this.createConfiguration = this.createConfiguration.bind(this);
        this.checkPermissions = this.checkPermissions.bind(this);

        const descriptor = DescriptorUtilities.findDescriptorByNameAndContext(this.props.descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_USERS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL)[0];

        this.state = {
            items: [],
            currentPage: 1,
            currentPageSize: 10,
            searchTerm: '',
            sortField: 'lastSent',
            sortOrder: 'desc',
            currentRowSelected: {},
            showDetailModal: false,
            descriptor: descriptor
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

    createConfiguration() {
        return <RoleConfiguration />;
    }

    checkPermissions(operation) {
        const { descriptor } = this.state;
        console.log(descriptor);
        if (descriptor) {
            return DescriptorUtilities.isOperationAssigned(descriptor, operation)
        }
        return false;
    }

    render() {
        const canCreate = this.checkPermissions(DescriptorUtilities.OPERATIONS.CREATE);
        const canDelete = this.checkPermissions(DescriptorUtilities.OPERATIONS.DELETE);

        return (
            <div>
                <div>
                    <TableDisplay retrieveData={this.retrieveData} columns={this.createColumns()} createInsertFields={this.createConfiguration} newButton={canCreate} deleteButton={canDelete} />
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
    descriptors: state.descriptors.items
});

const mapDispatchToProps = dispatch => ({
    // createRole: (roleName) => dispatch(createNewRole(roleName)),
    // fetchRoles: () => dispatch(fetchRoles())
});

export default connect(mapStateToProps, mapDispatchToProps)(RoleTable);
