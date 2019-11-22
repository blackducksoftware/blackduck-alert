import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
import RoleConfiguration from 'dynamic/loaded/users/RoleConfiguration';

class RoleTable extends Component {

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
        return [{
            header: 'name',
            headerLabel: 'Name',
            isKey: true
        }];
    }

    retrieveData() {
        return [];
    }

    createConfiguration() {
        return <RoleConfiguration />;
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

RoleTable.defaultProps = {
    canCreate: true,
    canDelete: true
};

RoleTable.propTypes = {
    canCreate: PropTypes.bool,
    canDelete: PropTypes.bool
};

export default RoleTable;
