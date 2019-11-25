import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TableDisplay from 'field/TableDisplay';
import TextInput from 'field/input/TextInput';
import { connect } from 'react-redux';
import { createNewRole, deleteRole, fetchRoles } from 'store/actions/roles';

class RoleTable extends Component {
    constructor(props) {
        super(props);

        this.retrieveData = this.retrieveData.bind(this);
        this.createColumns = this.createColumns.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onSave = this.onSave.bind(this);

        this.state = {};
    }

    componentDidMount() {
        this.retrieveData();
    }

    handleChange(e) {
        const { name, value, type, checked } = e.target;
        const updatedValue = type === 'checkbox' ? checked.toString().toLowerCase() === 'true' : value;
        this.setState({
            [name]: updatedValue
        });
    }

    createPermissionsColumns() {
        return [
            {
                header: 'descriptorName',
                headerLabel: 'Descriptor',
                isKey: true
            }, {
                header: 'context',
                headerLabel: 'Context',
                isKey: false
            }, {
                header: 'createOperation',
                headerLabel: 'Create',
                isKey: false
            }, {
                header: 'deleteOperation',
                headerLabel: 'Delete',
                isKey: false
            }, {
                header: 'readOperation',
                headerLabel: 'Read',
                isKey: false
            }, {
                header: 'writeOperation',
                headerLabel: 'Write',
                isKey: false
            }, {
                header: 'executeOperation',
                headerLabel: 'Execute',
                isKey: false
            }, {
                header: 'uploadFileReadOperation',
                headerLabel: 'Upload File Read',
                isKey: false
            }, {
                header: 'uploadFileWriteOperation',
                headerLabel: 'Upload File Write',
                isKey: false
            }, {
                header: 'uploadFileDeleteOperation',
                headerLabel: 'Upload File Delete',
                isKey: false
            }
        ];
    }

    createColumns() {
        return [{
            header: 'name',
            headerLabel: 'Name',
            isKey: true
        }];
    }

    retrievePermissionsData() {
        return [];
    }

    retrieveData() {
        this.props.getRoles();
        return this.props.roles;
    }

    onSave() {
        this.props.createRole(this.state['rolename']);
    }

    render() {
        const { canCreate, canDelete } = this.props;

        const roleNameKey = 'rolename';
        const roleNameValue = this.state[roleNameKey];

        return (
            <div>
                <div>
                    <TableDisplay modalTitle="Role" onConfigSave={this.onSave} retrieveData={this.retrieveData} columns={this.createColumns()} newButton={canCreate} deleteButton={canDelete}>
                        <TextInput name={roleNameKey} label="Role Name" description="The name of the role." onChange={this.handleChange} value={roleNameValue} />
                        <TableDisplay columns={this.createPermissionsColumns()} retrieveData={this.retrievePermissionsData} deleteButton={false} newButton={false} />
                    </TableDisplay>
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

const mapStateToProps = state => ({
    roles: state.roles.data
});

const mapDispatchToProps = dispatch => ({
    createRole: rolename => dispatch(createNewRole(rolename)),
    deleteRole: rolename => dispatch(deleteRole(rolename)),
    getRoles: () => dispatch(fetchRoles())
});

export default connect(mapStateToProps, mapDispatchToProps)(RoleTable);
