import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { clearCertificateFieldErrors, deleteCertificate, fetchCertificates, saveCertificate } from 'store/actions/certificates';
import ConfigurationLabel from 'component/common/ConfigurationLabel';
import TableDisplay from 'field/TableDisplay';
import TextInput from 'field/input/TextInput';
import TextArea from 'field/input/TextArea';
import ReadOnlyField from 'field/ReadOnlyField';

class CertificatesPage extends Component {
    constructor(props) {
        super(props);
        this.retrieveData = this.retrieveData.bind(this);
        this.clearModalFieldState = this.clearModalFieldState.bind(this);
        this.createColumns = this.createColumns.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.onSave = this.onSave.bind(this);
        this.onConfigClose = this.onConfigClose.bind(this);
        this.onDelete = this.onDelete.bind(this);
        this.createModalFields = this.createModalFields.bind(this);
        this.onEdit = this.onEdit.bind(this);

        this.state = {
            certificate: {},
            saveCallback: () => null,
            deleteCallback: () => null
        };
    }

    componentDidUpdate(prevProps) {
        if (prevProps.saveStatus === 'SAVING' && (this.props.saveStatus === 'SAVED' || this.props.saveStatus === 'ERROR')) {
            this.state.saveCallback(true);
        }
        if (prevProps.inProgress && !prevProps.deleteSuccess && !this.props.inProgress && this.props.deleteSuccess) {
            this.state.deleteCallback();
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

    onConfigClose(callback) {
        this.props.clearFieldErrors();
        callback();
    }

    clearModalFieldState() {
        if (this.state.certificate && Object.keys(this.state.certificate).length > 0) {
            this.setState({
                certificate: {}
            });
        }
    }

    retrieveData() {
        this.props.getCertificates();
    }

    handleChange(e) {
        const { name, value, type, checked } = e.target;
        const { certificate } = this.state;

        const updatedValue = type === 'checkbox' ? checked.toString().toLowerCase() === 'true' : value;
        const newCertificate = Object.assign(certificate, { [name]: updatedValue });
        this.setState({
            certificate: newCertificate
        });
    }

    onSave(callback) {
        const { certificate } = this.state;
        this.props.saveCertificate(certificate);
        this.setState({
            saveCallback: callback
        });
        return true;
    }

    onDelete(certificatesToDelete, callback) {
        if (certificatesToDelete) {
            certificatesToDelete.forEach(certificateId => {
                this.props.deleteCertificate(certificateId);
            });
        }
        this.setState({
            deleteCallback: callback
        });
    }

    createModalFields() {
        const { certificate } = this.state;
        const { fieldErrors } = this.props;
        const aliasKey = 'alias';
        const certificateContentKey = 'certificateContent';
        return (
            <div>
                <ReadOnlyField label="Last Updated" name="lastUpdated" readOnly="true" value={certificate['lastUpdated']} />
                <TextInput
                    name={aliasKey} label="Alias" description="The certificate alias name."
                    required onChange={this.handleChange} value={certificate[aliasKey]}
                    errorName={aliasKey}
                    errorValue={fieldErrors[aliasKey]} />
                <TextArea
                    name={certificateContentKey} label="Certificate Content" description="The certificate content text."
                    required onChange={this.handleChange} value={certificate[certificateContentKey]}
                    errorName={certificateContentKey}
                    errorValue={fieldErrors[certificateContentKey]} />
            </div>
        );
    }

    onEdit(selectedRow, callback) {
        this.setState({
            certificate: selectedRow
        }, callback);
    }

    render() {
        const { fetching, inProgress, certificates, certificateDeleteError, label, description } = this.props;
        return (
            <div>
                <div>
                    <ConfigurationLabel configurationName={label} description={description} />
                </div>
                <div>
                    <TableDisplay
                        newConfigFields={this.createModalFields}
                        modalTitle="Certificate"
                        clearModalFieldState={this.clearModalFieldState}
                        onConfigSave={this.onSave}
                        onConfigDelete={this.onDelete}
                        onConfigClose={this.onConfigClose}
                        onEditState={this.onEdit}
                        refreshData={this.retrieveData}
                        data={certificates}
                        columns={this.createColumns()}
                        newButton={true}
                        deleteButton={true}
                        hasFieldErrors={false}
                        errorDialogMessage={certificateDeleteError}
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
    autoRefresh: PropTypes.bool,
    certificates: PropTypes.arrayOf(PropTypes.object),
    saveCertificate: PropTypes.func.isRequired,
    deleteCertificate: PropTypes.func.isRequired,
    getCertificates: PropTypes.func.isRequired,
    clearFieldErrors: PropTypes.func.isRequired,
    certificateDeleteError: PropTypes.string,
    inProgress: PropTypes.bool,
    deleteSuccess: PropTypes.bool,
    fetching: PropTypes.bool,
    fieldErrors: PropTypes.object,
    description: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    saveStatus: PropTypes.string
};

CertificatesPage.defaultProps = {
    inProgress: false,
    deleteSuccess: false,
    message: '',
    autoRefresh: true,
    fetching: false,
    certificates: [],
    fieldErrors: {},
    description: '',
    label: ''
};

const mapStateToProps = state => ({
    certificates: state.certificates.data,
    certificateDeleteError: state.certificates.certificateDeleteError,
    inProgress: state.certificates.inProgress,
    fetching: state.certificates.fetching,
    fieldErrors: state.users.fieldErrors,
    saveStatus: state.certificates.saveStatus,
    deleteSuccess: state.certificates.deleteSuccess
});

const mapDispatchToProps = dispatch => ({
    saveCertificate: certificate => dispatch(saveCertificate(certificate)),
    deleteCertificate: certificateId => dispatch(deleteCertificate(certificateId)),
    getCertificates: () => dispatch(fetchCertificates()),
    clearFieldErrors: () => dispatch(clearCertificateFieldErrors())
});

export default connect(mapStateToProps, mapDispatchToProps)(CertificatesPage);
