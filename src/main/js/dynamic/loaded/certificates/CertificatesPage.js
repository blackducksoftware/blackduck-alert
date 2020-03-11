import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from "prop-types";
import { clearCertificateFieldErrors, deleteCertificate, fetchCertificates, saveCertificate } from "store/actions/certificates";
import ConfigurationLabel from "component/common/ConfigurationLabel";
import TableDisplay from "field/TableDisplay";
import TextInput from "field/input/TextInput";
import TextArea from "field/input/TextArea";

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
            certificate: {}
        };
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
            }
        ];
    }

    onConfigClose() {
        this.props.clearFieldErrors();
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

    onSave() {
        const { certificate } = this.state;
        this.props.saveCertificate(certificate);
        return true;
    }

    onDelete(certificatesToDelete) {
        if (certificatesToDelete) {
            certificatesToDelete.forEach(certificateId => {
                this.props.deleteCertificate(certificateId);
            });
        }
        this.retrieveData();
    }

    createModalFields() {
        const { certificate } = this.state;
        const { fieldErrors } = this.props;
        const aliasKey = 'alias';
        const certificateContentKey = 'certificateContent';
        return (
            <div>
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

    onEdit(selectedRow) {
        this.setState({
            certificate: selectedRow
        });
    }

    render() {
        const { fetching, inProgress, certificates, certificateDeleteError } = this.props;
        return (
            <div>
                <div>
                    <ConfigurationLabel configurationName="Certificates" />
                </div>
                <div>
                    <TableDisplay
                        newConfigFields={this.createModalFields}
                        modalTitle="Certificate"
                        clearModalFieldState={this.clearModalFieldState}
                        onConfigSave={this.onSave}
                        onConfigDelete={this.onDelete}
                        onConfigClose={this.onConfigClose}
                        refreshData={this.retrieveData}
                        editState={this.onEdit}
                        data={this.props.certificates}
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
    fetching: PropTypes.bool,
    fieldErrors: PropTypes.object,
};

CertificatesPage.defaultProps = {
    inProgress: false,
    message: '',
    autoRefresh: true,
    fetching: false,
    certificates: [],
    fieldErrors: {}
};

const mapStateToProps = state => ({
    certificates: state.certificates.data,
    certificateDeleteError: state.certificates.certificateDeleteError,
    inProgress: state.certificates.inProgress,
    fetching: state.certificates.fetching,
    fieldErrors: state.users.fieldErrors
});

const mapDispatchToProps = dispatch => ({
    saveCertificate: certificate => dispatch(saveCertificate(certificate)),
    deleteCertificate: certificateId => dispatch(deleteCertificate(certificateId)),
    getCertificates: () => dispatch(fetchCertificates()),
    clearFieldErrors: () => dispatch(clearCertificateFieldErrors())
});

export default connect(mapStateToProps, mapDispatchToProps)(CertificatesPage);
