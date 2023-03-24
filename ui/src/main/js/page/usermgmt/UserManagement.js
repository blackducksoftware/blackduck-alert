import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Tab, Tabs } from 'react-bootstrap';
import PageHeader from 'common/component/navigation/PageHeader';
// This line below will be deleted before merge, along with the Beta component code below
import { default as RoleTableStagedForDelete } from 'page/usermgmt/RoleTable';
import { default as UserTableStagedForDelete } from 'page/usermgmt/UserTable';
import * as DescriptorUtilities from 'common/util/descriptorUtilities';
import { USER_MANAGEMENT_INFO } from 'page/usermgmt/UserModel';

import BetaPage from 'common/component/beta/BetaPage';
import BetaComponent from 'common/component/beta/BetaComponent';
import CurrentComponent from 'common/component/beta/CurrentComponent';
import UserTable from 'page/usermgmt/user/UserTable';
import RolesTable from 'page/usermgmt/roles/RolesTable';

const UserManagement = ({ descriptors }) => {
    const descriptor = DescriptorUtilities.findFirstDescriptorByNameAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_USERS, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
    const canCreate = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.CREATE);
    const canDelete = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.DELETE);

    return (
        <div>
            <PageHeader
                title={USER_MANAGEMENT_INFO.label}
                description="This page allows you to configure users and roles for Alert."
                icon="user"
            />
            <Tabs defaultActiveKey={1} id="user-management-tabs">
                <Tab eventKey={1} title="Users">
                    <BetaPage betaSelected>
                        <BetaComponent>
                            <UserTable canCreate={canCreate} canDelete={canDelete} />
                        </BetaComponent>
                        <CurrentComponent>
                            <UserTableStagedForDelete canCreate={canCreate} canDelete={canDelete} />
                        </CurrentComponent>
                    </BetaPage>
                </Tab>
                <Tab eventKey={2} title="Roles">
                    <BetaPage betaSelected>
                        <BetaComponent>
                            <RolesTable canCreate={canCreate} canDelete={canDelete} />
                        </BetaComponent>
                        <CurrentComponent>
                            <RoleTableStagedForDelete canCreate={canCreate} canDelete={canDelete} />
                        </CurrentComponent>
                    </BetaPage>
                </Tab>
            </Tabs>
        </div>
    );
};

UserManagement.propTypes = {
    descriptors: PropTypes.array.isRequired
};

const mapStateToProps = (state) => ({
    descriptors: state.descriptors.items
});

export default connect(mapStateToProps, null)(UserManagement);
