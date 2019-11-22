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
        this.retrieveData();
    }

    createColumns() {
        return [
            {
                header: 'username',
                headerLabel: 'Username',
                isKey: true
            },
            {
                header: 'email_address',
                headerLabel: 'Email',
                isKey: false
            },
            {
                header: 'expired',
                headerLabel: 'Expired',
                isKey: false
            },
            {
                header: 'locked',
                headerLabel: 'Locked',
                isKey: false
            },
            {
                header: 'password_expired',
                headerLabel: 'Password Expired',
                isKey: false
            },
            {
                header: 'enabled',
                headerLabel: 'Enabled',
                isKey: false
            }
        ];
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
