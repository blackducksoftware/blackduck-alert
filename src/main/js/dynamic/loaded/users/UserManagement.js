import React, { Component } from 'react';
import CollapsiblePane from "component/common/CollapsiblePane";
import { connect } from 'react-redux';
import ConfigurationLabel from "component/common/ConfigurationLabel";
import RoleTable from "dynamic/loaded/users/RoleTable";

class UserManagement extends Component {

    constructor(props) {
        super(props);
        this.state = {
            roleItems: [],
            currentPage: 1,
            currentPageSize: 10,
            searchTerm: '',
            sortField: 'lastSent',
            sortOrder: 'desc',
            currentRowSelected: {},
            showDetailModal: false
        };
    }

    render() {
        const expanded = true;
        return (
            <div>
                <ConfigurationLabel configurationName="User Management" />
                <CollapsiblePane
                    title='Users'
                    expanded={expanded}
                >
                </CollapsiblePane>
                <CollapsiblePane
                    title='Roles'
                    expanded={expanded}
                >
                    <RoleTable />
                </CollapsiblePane>
            </div>
        );
    }
}

UserManagement.defaultProps = {};

UserManagement.propTypes = {};

const mapStateToProps = () => ({});

const mapDispatchToProps = () => ({});

export default connect(mapStateToProps, mapDispatchToProps)(UserManagement);
