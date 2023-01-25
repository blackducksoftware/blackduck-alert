package com.synopsys.integration.alert.authentication.saml.validator;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.function.Function;

@Component
public class SAMLFileUploadValidator {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final FilePersistenceUtil filePersistenceUtil;

    @Autowired
    public SAMLFileUploadValidator(FilePersistenceUtil filePersistenceUtil) {
        this.filePersistenceUtil = filePersistenceUtil;
    }

    public ValidationResponseModel validateMetadataFile(Resource resource) {
        return validateFile(
            AuthenticationDescriptor.SAML_METADATA_FILE,
            resource,
            this::validateXMLFile
        );
    }

    public ValidationResponseModel validateCertFile(String fileName, Resource resource) {
        return validateFile(
            fileName,
            resource,
            this::validateCertFile
        );
    }

    private ValidationResponseModel validateFile(String fileName, Resource resource, Function<File, ValidationResult> validateFunction) {
        String tempFilename = "temp_" + fileName;
        try {
            filePersistenceUtil.writeFileToUploadsDirectory(tempFilename, resource.getInputStream());
            File tempFileToValidate = filePersistenceUtil.createUploadsFile(tempFilename);
            ValidationResult validationResult = validateFunction.apply(tempFileToValidate);
            filePersistenceUtil.delete(tempFileToValidate);
            if (validationResult.hasErrors()) {
                return ValidationResponseModel.generalError(validationResult.combineErrorMessages());
            }
        } catch (IOException ex) {
            logger.error(String.format("Error writing to or deleting %s, caused by: %s", tempFilename, ex.getMessage()));
            return ValidationResponseModel.generalError( "Error uploading file to server.");
        }
        return ValidationResponseModel.success();
    }

    private ValidationResult validateXMLFile(File file) {
        try (InputStream fileInputStream = new FileInputStream(file)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            XMLErrorHandler errorHandler = new XMLErrorHandler();
            builder.setErrorHandler(errorHandler);
            builder.parse(new InputSource(fileInputStream));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return ValidationResult.errors(String.format("XML file error: %s", ex.getMessage()));
        }
        return ValidationResult.success();
    }

    private ValidationResult validateCertFile(File file) {
        try (InputStream fileInputStream = new FileInputStream(file)) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            try (ByteArrayInputStream certInputStream = new ByteArrayInputStream(fileInputStream.readAllBytes())) {
                certFactory.generateCertificate(certInputStream);
            }
        } catch (IOException | CertificateException ex) {
            return ValidationResult.errors(String.format("Certificate file error: %s", ex.getMessage()));
        }
        return ValidationResult.success();
    }

    private class XMLErrorHandler implements ErrorHandler {
        @Override
        public void warning(SAXParseException exception) throws SAXException {
            logger.warn("File upload exception warning:", exception);
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            logger.error("File upload exception error:", exception);
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            logger.error("File upload exception fatal error:", exception);
        }
    }
}
