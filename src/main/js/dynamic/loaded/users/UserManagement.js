import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ConfigurationLabel from 'component/common/ConfigurationLabel';
import RoleTable from 'dynamic/loaded/users/RoleTable';
import UserTable from 'dynamic/loaded/users/UserTable';
import CollapsiblePane from 'component/common/CollapsiblePane';
import * as DescriptorUtilities from 'util/descriptorUtilities';

class UserManagement extends Component {

    constructor(props) {
        super(props);

        const descriptor = DescriptorUtilities.findDescriptorByNameAndContext(this.props.descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_USERS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL)[0];

        this.state = {
            descriptor: descriptor
        };
    }

    checkPermissions(operation) {
        const { descriptor } = this.state;
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
                <ConfigurationLabel configurationName="User Management" description="Create, edit, or delete Users and Role to customize what the user can do in Alert." />
                <UserTable canCreate={canCreate} canDelete={canDelete} />
                <CollapsiblePane title="Modify Roles">
                    <RoleTable canCreate={canCreate} canDelete={canDelete} />
                </CollapsiblePane>
            </div>
        );
    }
}

UserManagement.defaultProps = {};

UserManagement.propTypes = {
    descriptors: PropTypes.array
};

const mapStateToProps = state => ({
    descriptors: state.descriptors.items
});

const mapDispatchToProps = () => ({});

export default connect(mapStateToProps, mapDispatchToProps)(UserManagement);
