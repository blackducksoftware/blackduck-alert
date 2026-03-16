import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Tab, Tabs } from 'react-bootstrap';
import PageLayout from 'common/component/PageLayout';
import UserTable from 'page/usermgmt/user/UserTable';
import RoleTable from 'page/usermgmt/roles/RoleTable';
import * as DescriptorUtilities from 'common/util/descriptorUtilities';
import { USER_MANAGEMENT_INFO } from 'page/usermgmt/UserModel';

const UserManagement = ({ descriptors }) => {
    const descriptor = DescriptorUtilities.findFirstDescriptorByNameAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_USERS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
    const canCreate = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.CREATE);
    const canDelete = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.DELETE);

    return (
        <PageLayout
            title={USER_MANAGEMENT_INFO.label}
            description="This page allows you to configure users and roles for Alert."
            headerIcon="user"
        >
            <Tabs defaultActiveKey={1} id="user-management-tabs">
                <Tab eventKey={1} title="Users">
                    <UserTable canCreate={canCreate} canDelete={canDelete} />
                </Tab>
                <Tab eventKey={2} title="Roles">
                    <RoleTable canCreate={canCreate} canDelete={canDelete} />
                </Tab>
            </Tabs>
        </PageLayout>
    );
};

UserManagement.propTypes = {
    descriptors: PropTypes.array.isRequired
};

const mapStateToProps = (state) => ({
    descriptors: state.descriptors.items
});

export default connect(mapStateToProps, null)(UserManagement);
