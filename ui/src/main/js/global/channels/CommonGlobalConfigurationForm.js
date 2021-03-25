import React from 'react';
import * as PropTypes from 'prop-types';
import ConfigButtons from 'component/common/ConfigButtons';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
// import ChannelTestModal from 'dynamic/ChannelTestModal';

const CommonGlobalConfigurationForm = ({
    formData, csrfToken, displaySave, displayTest, displayDelete, children
}) => {
    const testRequest = () => ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, FieldModelUtilities.getFieldModelId(formData));
    const saveRequest = (event) => {
        event.preventDefault();
        event.stopPropagation();
        const id = FieldModelUtilities.getFieldModelId(formData);
        if (id) {
            return ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, id, formData);
        }

        return ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, formData);
    };
    const deleteRequest = () => ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, FieldModelUtilities.getFieldModelId(formData));

    return (
        <form className="form-horizontal" onSubmit={saveRequest} noValidate>
            <div>
                {children}
            </div>
            <ConfigButtons
                includeSave={displaySave}
                includeTest={displayTest}
                includeDelete={displayDelete}
                type="submit"
                onTestClick={testRequest}
                onDeleteClick={deleteRequest}
                confirmDeleteMessage="Are you sure you want to delete the configuration?"
            />
            {/* <ChannelTestModal */}
            {/*    sendTestMessage={testConfigAction} */}
            {/*    showTestModal={showTest} */}
            {/*    handleCancel={this.handleTestCancel} */}
            {/*    fieldModel={currentConfig} */}
            {/*    testFields={testFields} */}
            {/* /> */}
        </form>
    );
};

CommonGlobalConfigurationForm.propTypes = {
    children: PropTypes.node.isRequired,
    formData: PropTypes.object.isRequired,
    csrfToken: PropTypes.string.isRequired,
    displaySave: PropTypes.bool,
    displayTest: PropTypes.bool,
    displayDelete: PropTypes.bool
};

CommonGlobalConfigurationForm.defaultProps = {
    displaySave: true,
    displayTest: true,
    displayDelete: true
};

export default CommonGlobalConfigurationForm;
