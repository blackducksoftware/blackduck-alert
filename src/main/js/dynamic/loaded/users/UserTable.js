import React, { Component } from 'react';
import PropTypes from 'prop-types'
import TableDisplay from 'field/TableDisplay';
import UserConfiguration from 'dynamic/loaded/users/UserConfiguration';

class UserTable extends Component {

    constructor(props) {
        super(props);

        this.retrieveData = this.retrieveData.bind(this);
        this.createColumns = this.createColumns.bind(this);
        this.createConfiguration = this.createConfiguration.bind(this);
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

        const email = {
            header: 'email',
            headerLabel: 'email',
            isKey: false
        }

        return [nameColumn, email];
    }

    retrieveData() {
        return [];
    }

    createConfiguration() {
        return <UserConfiguration />;
    }

    render() {
        const { canCreate, canDelete } = this.props;

        return (
            <div>
                <div>
                    <TableDisplay retrieveData={this.retrieveData} columns={this.createColumns()} createInsertFields={this.createConfiguration} newButton={canCreate} deleteButton={canDelete} />
                </div>
            </div>
        );
    }
}

UserTable.defaultProps = {
    canCreate: true,
    canDelete: true
};

UserTable.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool
};

export default UserTable;
