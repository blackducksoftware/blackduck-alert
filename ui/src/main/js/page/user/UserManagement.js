import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Tab, Tabs } from 'react-bootstrap';
import ConfigurationLabel from 'common/ConfigurationLabel';
import RoleTable from 'page/user/RoleTable';
import UserTable from 'page/user/UserTable';
import * as DescriptorUtilities from 'common/util/descriptorUtilities';
import { USER_MANAGEMENT_INFO } from 'page/user/UserModel';

function UserManagement(props) {
    const { descriptors } = props;
    const descriptor = DescriptorUtilities.findFirstDescriptorByNameAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_USERS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
    const canCreate = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.CREATE);
    const canDelete = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.DELETE);

    return (
        <div>
            <ConfigurationLabel
                configurationName={USER_MANAGEMENT_INFO.label}
                description="This page allows you to configure users and roles for Alert."
            />
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

UserManagement.propTypes = {
    descriptors: PropTypes.array.isRequired
};

const mapStateToProps = (state) => ({
    descriptors: state.descriptors.items
});

export default connect(mapStateToProps, null)(UserManagement);
