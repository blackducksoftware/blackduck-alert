import React, { Component } from 'react';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import ConfigurationLabel from 'component/common/ConfigurationLabel';
import PropTypes from "prop-types";
import { getAllConfigs } from 'store/actions/globalConfiguration';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import { BootstrapTable, DeleteButton, InsertButton, TableHeaderColumn } from "react-bootstrap-table";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import AutoRefresh from "../component/common/AutoRefresh";
import IconTableCellFormatter from "../component/common/IconTableCellFormatter";
import * as FieldModelUtilities from "../util/fieldModelUtilities";

const providerModificationState = {
    EDIT: 'EDIT',
    COPY: 'COPY'
};

const ProviderCommonKeys = {
    KEY_NAME: 'provider.common.config.name',
    KEY_ENABLED: 'provider.common.config.enabled'
}

class ProviderTable extends Component {
    constructor(props) {
        super(props);
        this.createTableData = this.createTableData.bind(this);
        this.createCustomModal = this.createCustomModal.bind(this);
        this.createCustomButtonGroup = this.createCustomButtonGroup.bind(this);
        this.cancelRowSelect = this.cancelRowSelect.bind(this);
        this.editButtonClicked = this.editButtonClicked.bind(this);
        this.editButtonClick = this.editButtonClick.bind(this);
        this.copyButtonClicked = this.copyButtonClicked.bind(this);
        this.copyButtonClick = this.copyButtonClick.bind(this);
        this.customConfigDeletionConfirm = this.customConfigDeletionConfirm.bind(this);
        this.reloadData = this.reloadData.bind(this);
        this.saveBtn = this.saveBtn.bind(this);
        this.onDeleteClose = this.onDeleteClose.bind(this);
        this.onDeleteSubmit = this.onDeleteSubmit.bind(this);

        this.state = {
            descriptor: null
        };
    }

    componentDidMount() {
        const descriptor = this.props.descriptors.find(descriptor => descriptor.name === DescriptorUtilities.DESCRIPTOR_NAME.PROVIDER_BLACKDUCK)
        if (descriptor) {
            this.setState({
                descriptor
            });
            this.props.getAllConfigs(descriptor.name);
        }
    }

    onDeleteSubmit() {
        this.state.nextDelete();
    }

    onDeleteClose() {
        this.setState({
            showDeleteModal: false,
            nextDelete: null,
            itemsToDelete: [],
            modificationState: providerModificationState.EDIT
        });
    }

    getCurrentConfig() {
        const { currentRowSelected, modificationState } = this.state;
        if (currentRowSelected != null) {
            const { id } = currentRowSelected;
            return (<div>Modal goes here</div>);
        }
        return null;
    }

    createCustomModal(onModalClose) {
        return (
            <div>Modal goes here</div>
        );
    }

    saveBtn() {
        this.cancelRowSelect();
        this.reloadData();
    }

    reloadData() {
        const { descriptor } = this.state;
        this.props.getAllConfigs(descriptor.name);
    }

    cancelRowSelect() {
        this.refs.table.cleanSelected();
        this.setState({
            currentRowSelected: null,
            modificationState: providerModificationState.EDIT
        });
    }

    customConfigDeletionConfirm(next, dropRowKeys) {
        const { providerConfigs } = this.props;
        const matchingConfigs = providerConfigs.filter(config => dropRowKeys.includes(config.id));
        this.props.openJobDeleteModal();
        this.setState({
            showDeleteModal: true,
            nextDelete: next,
            itemsToDelete: matchingConfigs,
            modificationState: providerModificationState.EDIT
        });
    }

    editButtonClicked(currentRowSelected) {
        this.setState({
            currentRowSelected,
            modificationState: providerModificationState.EDIT
        });
    }

    editButtonClick(cell, row) {
        return <IconTableCellFormatter handleButtonClicked={this.editButtonClicked} currentRowSelected={row} buttonIconName="pencil-alt" buttonText="Edit" />;
    }

    copyButtonClicked(currentRowSelected) {
        this.setState({
            currentRowSelected,
            modificationState: providerModificationState.COPY
        });
    }

    copyButtonClick(cell, row) {
        return <IconTableCellFormatter handleButtonClicked={this.copyButtonClicked} currentRowSelected={row} buttonIconName="copy" buttonText="Copy" />;
    }

    enabledState(cell) {
        const icon = (cell == 'true') ? 'check' : 'times';
        const color = (cell == 'true') ? 'synopsysGreen' : 'synopsysRed';
        const className = `alert-icon ${color}`;

        return (
            <div className="btn btn-link jobIconButton">
                <FontAwesomeIcon icon={icon} className={className} size="lg" />
            </div>
        );

    }

    createCustomButtonGroup(buttons) {
        const classes = 'btn btn-md btn-info react-bs-table-add-btn tableButton';
        const insertOnClick = buttons.insertBtn ? buttons.insertBtn.props.onClick : null;
        const deleteOnClick = buttons.deleteBtn ? buttons.deleteBtn.props.onClick : null;
        let refreshButton = !this.props.autoRefresh && (
            <button type="button" className={classes} onClick={this.reloadData}>
                <FontAwesomeIcon icon="sync" className="alert-icon" size="lg" />Refresh
            </button>
        );
        return (
            <div>
                {buttons.insertBtn
                && <InsertButton className="addJobButton btn-md" onClick={insertOnClick}>
                    <FontAwesomeIcon icon="plus" className="alert-icon" size="lg" />
                    New
                </InsertButton>
                }
                {buttons.deleteBtn
                && <DeleteButton className="deleteJobButton btn-md" onClick={deleteOnClick}>
                    <FontAwesomeIcon icon="trash" className="alert-icon" size="lg" />
                    Delete
                </DeleteButton>
                }
                {refreshButton}
            </div>
        );
    }

    checkJobPermissions(operation) {
        const { descriptors } = this.props;
        if (descriptors) {
            const descriptorList = DescriptorUtilities.findDescriptorByNameAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_NAME.PROVIDER_BLACKDUCK, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
            if (descriptorList) {
                return descriptorList.some(descriptor => DescriptorUtilities.isOperationAssigned(descriptor, operation));
            }
        }
        return false;
    }

    createTableData(configs) {
        const tableData = [];
        if (configs) {
            configs.forEach((config) => {
                if (config) {
                    const id = config.id;
                    const name = FieldModelUtilities.getFieldModelSingleValue(config, ProviderCommonKeys.KEY_NAME);
                    const enabled = FieldModelUtilities.getFieldModelSingleValue(config, ProviderCommonKeys.KEY_ENABLED);
                    const createdAt = config.createdAt;
                    const lastUpdated = config.lastUpdated;
                    const entry = Object.assign({}, {
                        id,
                        name,
                        createdAt,
                        lastUpdated,
                        enabled
                    });
                    tableData.push(entry);
                }
            });
        }
        return tableData;
    }

    render() {
        const { descriptor } = this.state;
        const { providerConfigs } = this.props;
        const descriptorHeader = descriptor && (
            <div>
                <ConfigurationLabel configurationName={descriptor.label} description={descriptor.description} />
            </div>
        );

        const tableOptions = {
            btnGroup: this.createCustomButtonGroup,
            noDataText: 'No providers configured',
            clearSearch: true,
            insertModal: this.createCustomModal,
            handleConfirmDeleteRow: this.customConfigDeletionConfirm,
            defaultSortName: 'name',
            defaultSortOrder: 'asc',
            onRowDoubleClick: this.editButtonClicked
        };

        const selectRowProp = {
            mode: 'checkbox',
            clickToSelect: true,
            bgColor(row, isSelect) {
                if (isSelect) {
                    return '#e8e8e8';
                }
                return null;
            }
        };

        const canCreate = this.checkJobPermissions(DescriptorUtilities.OPERATIONS.CREATE);
        const canDelete = this.checkJobPermissions(DescriptorUtilities.OPERATIONS.DELETE);
        const data = this.createTableData(providerConfigs);
        const content = (
            <div>
                {this.getCurrentConfig()}
                <BootstrapTable
                    version="4"
                    hover
                    condensed
                    data={data}
                    containerClass="table"
                    insertRow={canCreate}
                    deleteRow={canDelete}
                    selectRow={selectRowProp}
                    options={tableOptions}
                    search
                    trClassName="tableRow"
                    headerContainerClass="scrollable"
                    bodyContainerClass="tableScrollableBody"
                    ref="table"
                >
                    <TableHeaderColumn dataField="id" isKey hidden>Job Id</TableHeaderColumn>
                    <TableHeaderColumn dataField="name" dataSort columnTitle columnClassName="tableCell">Distribution Job</TableHeaderColumn>
                    <TableHeaderColumn dataField="createdAt" dataSort columnTitle columnClassName="tableCell">Created At</TableHeaderColumn>
                    <TableHeaderColumn dataField="lastUpdated" dataSort columnTitle columnClassName="tableCell">Last Updated</TableHeaderColumn>
                    <TableHeaderColumn dataField="enabled" width="96" dataSort columnTitle columnClassName="tableCell" dataFormat={this.enabledState}>Enabled</TableHeaderColumn>
                    <TableHeaderColumn dataField="" width="48" columnClassName="tableCell" dataFormat={this.editButtonClick} thStyle={{ textAlign: 'center' }}>Edit</TableHeaderColumn>
                    <TableHeaderColumn dataField="" width="48" columnClassName="tableCell" dataFormat={this.copyButtonClick} thStyle={{ textAlign: 'center' }}>Copy</TableHeaderColumn>
                </BootstrapTable>

                {this.props.inProgress &&
                <div className="progressIcon">
                    <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />
                </div>
                }

                <p name="providerConfigTableMessage">{this.props.providerConfigTableMessage}</p>
            </div>
        );

        return (
            <div>
                {descriptorHeader}
                <div>
                    Configuration table goes here!
                    Configs Found = {providerConfigs && providerConfigs.length}
                    <div className="pull-right">
                        <AutoRefresh startAutoReload={this.reloadData} autoRefresh={this.props.autoRefresh} />
                    </div>
                    {content}
                </div>
            </div>
        );
    }
}


ProviderTable.propTypes = {
    autoRefresh: PropTypes.bool,
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    descriptorFetching: PropTypes.bool.isRequired,
    inProgress: PropTypes.bool.isRequired,
    providerConfigs: PropTypes.arrayOf(PropTypes.object).isRequired,
    providerConfigTableMessage: PropTypes.string,
    getAllConfigs: PropTypes.func.isRequired
};

ProviderTable.defaultProps = {
    autoRefresh: true,
    descriptors: [],
    inProgress: false,
    providerConfigTableMessage: ''
};

const mapStateToProps = state => ({
    descriptors: state.descriptors.items,
    descriptorFetching: state.descriptors.fetching,
    providerConfigs: state.globalConfiguration.allConfigs
});

const mapDispatchToProps = dispatch => ({
    getAllConfigs: descriptorName => dispatch(getAllConfigs(descriptorName))
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(ProviderTable));
