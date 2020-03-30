import React, { Component } from 'react';
import PropTypes from 'prop-types';
import AuditPage from 'dynamic/loaded/audit/AuditPage';
import UserManagement from 'dynamic/loaded/users/UserManagement';
import CertificatesPage from 'dynamic/loaded/certificates/CertificatesPage';
import TaskManagement from 'dynamic/loaded/tasks/TaskManagement';

const ComponentRegistry = {
    'audit.AuditPage': AuditPage,
    'users.UserManagement': UserManagement,
    'certificates.CertificatesPage': CertificatesPage,
    'tasks.TaskManagement': TaskManagement
};

export class DescriptorContentLoader extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const { componentNamespace, description, label } = this.props;
        const Component = ComponentRegistry[componentNamespace];
        return (
            <Component description={description} label={label} />
        );
    }
}

DescriptorContentLoader.propTypes = {
    componentNamespace: PropTypes.string.isRequired,
    description: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired
};

export default DescriptorContentLoader;
