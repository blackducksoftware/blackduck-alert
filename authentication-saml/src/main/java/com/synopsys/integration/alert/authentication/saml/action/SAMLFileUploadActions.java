package com.synopsys.integration.alert.authentication.saml.action;

import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.validator.SAMLConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.api.FileUploadHelper;
import com.synopsys.integration.alert.common.rest.model.ExistenceModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

@Component
public class SAMLFileUploadActions {
    private final FileUploadHelper fileUploadHelper;
    private final FilePersistenceUtil filePersistenceUtil;

    private final SAMLConfigAccessor configurationAccessor;
    private final SAMLConfigurationValidator configurationValidator;

    @Autowired
    public SAMLFileUploadActions(AuthorizationManager authorizationManager, SAMLConfigAccessor configurationAccessor, SAMLConfigurationValidator configurationValidator, AuthenticationDescriptorKey authenticationDescriptorKey, FilePersistenceUtil filePersistenceUtil) {
        this.fileUploadHelper = new FileUploadHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
        this.filePersistenceUtil = filePersistenceUtil;
        this.configurationAccessor = configurationAccessor;
        this.configurationValidator = configurationValidator;
    }

    public ActionResponse<ExistenceModel> metadataFileExists() {
        return fileUploadHelper.fileExists(
            () -> fileFromConfigFieldExists(samlConfigModel -> samlConfigModel.getMetadataFilePath().orElse(""))
        );
    }

//    public ActionResponse<ExistenceModel> encryptionCertificateExists() {
//        return fileUploadHelper.fileExists(
//            () -> fileFromConfigFieldExists(samlConfigModel -> samlConfigModel.getEncryptionCertificate().orElse(""))
//        );
//    }

    private ExistenceModel fileFromConfigFieldExists(Function<SAMLConfigModel, String> filePathFromConfigMapper) {
        Boolean exists = Boolean.FALSE;
        Optional<SAMLConfigModel> optionalSAMLConfigModel = configurationAccessor.getConfiguration();
        Optional<String> optionalFilePath = optionalSAMLConfigModel.map(filePathFromConfigMapper).filter(StringUtils::isNotBlank);
        if (optionalFilePath.isPresent()) {
            exists = filePersistenceUtil.uploadFileExists(optionalFilePath.get());
        }
        return new ExistenceModel(exists);
    }
}
