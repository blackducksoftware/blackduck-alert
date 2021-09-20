import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { clearCertificateFieldErrors, deleteCertificate, fetchCertificates, saveCertificate, validateCertificate } from 'store/actions/certificates';
import ConfigurationLabel from 'common/ConfigurationLabel';
import TableDisplay from 'common/table/TableDisplay';
import TextInput from 'common/input/TextInput';
import TextArea from 'common/input/TextArea';
import ReadOnlyField from 'common/input/field/ReadOnlyField';
import * as DescriptorUtilities from 'common/util/descriptorUtilities';
import { CERTIFICATE_INFO } from 'page/certificates/CertificateModel';

class CertificatesPage extends Component {
    constructor(props) {
        super(props);
        this.retrieveData = this.retrieveData.bind(this);
        this.clearModalFieldState = this.clearModalFieldState.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onSave = this.onSave.bind(this);
        this.onConfigClose = this.onConfigClose.bind(this);
        this.onDelete = this.onDelete.bind(this);
        this.createModalFields = this.createModalFields.bind(this);
        this.onEdit = this.onEdit.bind(this);
        this.onCopy = this.onCopy.bind(this);

        this.state = {
            certificate: {},
            validateCallback: () => null,
            saveCallback: () => null,
            deleteCallback: () => null
        };
    }

    componentDidUpdate(prevProps) {
        const { validateCallback, saveCallback, deleteCallback } = this.state;
        const { saveStatus, deleteSuccess, inProgress } = this.props;
        if (prevProps.saveStatus === 'VALIDATING' && saveStatus === 'VALIDATED') {
            validateCallback(true);
        }
        if (prevProps.saveStatus === 'SAVING' && (saveStatus === 'SAVED' || saveStatus === 'ERROR')) {
            saveCallback(true);
        }
        if (prevProps.inProgress && !prevProps.deleteSuccess && !inProgress && deleteSuccess) {
            deleteCallback();
        }
    }

    onConfigClose(callback) {
        const { clearFieldErrors } = this.props;
        clearFieldErrors();
        callback();
    }

    onSave(callback) {
        const { validateCertificateAction, saveCertificateAction } = this.props;
        const { certificate } = this.state;

        validateCertificateAction(certificate);
        this.setState({
            validateCallback: () => saveCertificateAction(certificate),
            saveCallback: callback
        });
        return true;
    }

    onDelete(certificatesToDelete, callback) {
        const { deleteCertificateAction } = this.props;
        if (certificatesToDelete) {
            certificatesToDelete.forEach((certificateId) => {
                deleteCertificateAction(certificateId);
            });
        }
        this.setState({
            deleteCallback: callback
        });
    }

    onEdit(selectedRow, callback) {
        this.setState({
            certificate: selectedRow
        }, callback);
    }

    onCopy(selectedRow, callback) {
        const copy = JSON.parse(JSON.stringify(selectedRow));
        copy.id = null;
        this.setState({
            certificate: copy
        }, callback);
    }

    createModalFields() {
        const { certificate } = this.state;
        const { fieldErrors } = this.props;

        const aliasKey = 'alias';
        const certificateContentKey = 'certificateContent';
        return (
            <div>
                <ReadOnlyField
                    id="lastUpdated"
                    label="Last Updated"
                    name="lastUpdated"
                    readOnly="true"
                    value={certificate.lastUpdated}
                />
                <TextInput
                    id={aliasKey}
                    name={aliasKey}
                    label="Alias"
                    description="The certificate alias name."
                    required
                    onChange={this.handleChange}
                    value={certificate[aliasKey]}
                    errorName={aliasKey}
                    errorValue={fieldErrors[aliasKey]}
                />
                <TextArea
                    id={certificateContentKey}
                    name={certificateContentKey}
                    label="Certificate Content"
                    description="The certificate content text."
                    required
                    onChange={this.handleChange}
                    value={certificate[certificateContentKey]}
                    errorName={certificateContentKey}
                    errorValue={fieldErrors[certificateContentKey]}
                />
            </div>
        );
    }

    handleChange(e) {
        const {
            name, value, type, checked
        } = e.target;
        const { certificate } = this.state;

        const updatedValue = type === 'checkbox' ? checked.toString()
            .toLowerCase() === 'true' : value;
        const newCertificate = Object.assign(certificate, { [name]: updatedValue });
        this.setState({
            certificate: newCertificate
        });
    }

    retrieveData() {
        const { getCertificates } = this.props;
        getCertificates();
    }

    clearModalFieldState() {
        const { certificate } = this.state;
        if (certificate && Object.keys(certificate).length > 0) {
            this.setState({
                certificate: {}
            });
        }
    }

    createColumns() {
        return [
            {
                header: 'id',
                headerLabel: 'Id',
                isKey: true,
                hidden: true
            },
            {
                header: 'alias',
                headerLabel: 'Alias',
                isKey: false,
                hidden: false
            },
            {
                header: 'lastUpdated',
                headerLabel: 'Last Updated',
                isKey: false,
                hidden: false
            }

        ];
    }

    render() {
        const {
            fetching, inProgress, certificates, errorMessage, fieldErrors, descriptors, autoRefresh
        } = this.props;

        const descriptor = DescriptorUtilities.findFirstDescriptorByNameAndContext(descriptors, DescriptorUtilities.DESCRIPTOR_NAME.COMPONENT_CERTIFICATES, DescriptorUtilities.CONTEXT_TYPE.GLOBAL);
        const hasFieldErrors = fieldErrors && Object.keys(fieldErrors).length > 0;
        const canCreate = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.CREATE);
        const canDelete = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.DELETE);
        const canSave = DescriptorUtilities.isOperationAssigned(descriptor, DescriptorUtilities.OPERATIONS.WRITE);
        return (
            <div>
                <div>
                    <ConfigurationLabel configurationName={CERTIFICATE_INFO.label} description="This page allows you to configure certificates for Alert to establish secure communication." />
                </div>
                <div>
                    <TableDisplay
                        autoRefresh={autoRefresh}
                        id="certificates"
                        newConfigFields={this.createModalFields}
                        modalTitle="Certificate"
                        clearModalFieldState={this.clearModalFieldState}
                        onConfigSave={this.onSave}
                        onConfigDelete={this.onDelete}
                        onConfigClose={this.onConfigClose}
                        onConfigCopy={this.onCopy}
                        onEditState={this.onEdit}
                        refreshData={this.retrieveData}
                        data={certificates}
                        columns={this.createColumns()}
                        newButton={canCreate}
                        deleteButton={canDelete}
                        saveButton={canSave}
                        hasFieldErrors={hasFieldErrors}
                        errorDialogMessage={errorMessage}
                        inProgress={inProgress}
                        fetching={fetching}
                        enableCopy={false}
                    />
                </div>
            </div>
        );
    }
}

CertificatesPage.propTypes = {
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    certificates: PropTypes.arrayOf(PropTypes.object),
    validateCertificateAction: PropTypes.func.isRequired,
    saveCertificateAction: PropTypes.func.isRequired,
    deleteCertificateAction: PropTypes.func.isRequired,
    getCertificates: PropTypes.func.isRequired,
    clearFieldErrors: PropTypes.func.isRequired,
    errorMessage: PropTypes.string,
    inProgress: PropTypes.bool,
    deleteSuccess: PropTypes.bool,
    fetching: PropTypes.bool,
    fieldErrors: PropTypes.object,
    saveStatus: PropTypes.string.isRequired,
    autoRefresh: PropTypes.bool
};

CertificatesPage.defaultProps = {
    inProgress: false,
    deleteSuccess: false,
    errorMessage: '',
    fetching: false,
    certificates: [],
    fieldErrors: {},
    autoRefresh: true
};

const mapStateToProps = (state) => ({
    descriptors: state.descriptors.items,
    certificates: state.certificates.data,
    inProgress: state.certificates.inProgress,
    fetching: state.certificates.fetching,
    errorMessage: state.certificates.error.message,
    fieldErrors: state.certificates.error.fieldErrors,
    saveStatus: state.certificates.saveStatus,
    deleteSuccess: state.certificates.deleteSuccess,
    autoRefresh: state.refresh.autoRefresh
});

const mapDispatchToProps = (dispatch) => ({
    validateCertificateAction: (certificate) => dispatch(validateCertificate(certificate)),
    saveCertificateAction: (certificate) => dispatch(saveCertificate(certificate)),
    deleteCertificateAction: (certificateId) => dispatch(deleteCertificate(certificateId)),
    getCertificates: () => dispatch(fetchCertificates()),
    clearFieldErrors: () => dispatch(clearCertificateFieldErrors())
});

export default connect(mapStateToProps, mapDispatchToProps)(CertificatesPage);
