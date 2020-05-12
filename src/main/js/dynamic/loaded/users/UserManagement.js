import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Tab, Tabs } from 'react-bootstrap';
import ConfigurationLabel from 'component/common/ConfigurationLabel';
import RoleTable from 'dynamic/loaded/users/RoleTable';
import UserTable from 'dynamic/loaded/users/UserTable';
import * as DescriptorUtilities from 'util/descriptorUtilities';

class UserManagement extends Component {

    constructor(props) {
        super(props);

    }

    render() {
        const { label, description, descriptors } = this.props;
        const descriptorList = DescriptorUtilities.findDescriptorByNameAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_USERS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL)
        let foundDescriptor = null;
        if (descriptorList && descriptorList.length > 0) {
            foundDescriptor = descriptorList[0];
        }

        const canCreate = DescriptorUtilities.isOperationAssigned(foundDescriptor, DescriptorUtilities.OPERATIONS.CREATE);
        const canDelete = DescriptorUtilities.isOperationAssigned(foundDescriptor, DescriptorUtilities.OPERATIONS.DELETE);
        return (
            <div>
                <ConfigurationLabel
                    configurationName={label}
                    description={description} />
                <Tabs defaultActiveKey={1} id="user-management-tabs">
                    <Tab eventKey={1} title="Users">
                        <UserTable canCreate={canCreate} canDelete={canDelete} />
                    </Tab>
                    <Tab eventKey={2} title="Roles">
                        <RoleTable canCreate={canCreate} canDelete={canDelete} />
                    </Tab>
                </Tabs>
            </div>
        );
    }
}

UserManagement.propTypes = {
    descriptors: PropTypes.array.isRequired,
    description: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired
};

const mapStateToProps = state => ({
    descriptors: state.descriptors.items
});

export default connect(mapStateToProps, null)(UserManagement);
