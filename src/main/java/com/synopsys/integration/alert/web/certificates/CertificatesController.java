package com.synopsys.integration.alert.web.certificates;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.alert.web.model.CertificateModel;

@RestController
@RequestMapping(CertificatesController.API_BASE_URL)
public class CertificatesController extends BaseController {
    public static final String API_BASE_URL = "/certificates";

    private final ResponseFactory responseFactory;
    private final ContentConverter contentConverter;
    private final CertificateActions actions;

    @Autowired
    public CertificatesController(ResponseFactory responseFactory, ContentConverter contentConverter, CertificateActions actions) {
        this.responseFactory = responseFactory;
        this.contentConverter = contentConverter;
        this.actions = actions;
    }

    @GetMapping
    public ResponseEntity<String> readCertificates() {
        return responseFactory.createOkContentResponse(contentConverter.getJsonString(actions.readCertificates()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<String> readCertificate(@PathVariable Long id) {
        Optional<CertificateModel> certificate = actions.readCertificate(id);
        if (certificate.isPresent()) {
            return responseFactory.createOkContentResponse(contentConverter.getJsonString(certificate.get()));
        }
        return responseFactory.createNotFoundResponse("Certificate resource not found");
    }

    @PostMapping
    public ResponseEntity<String> importCertificate(@RequestBody CertificateModel certificateModel) {
        CertificateModel certificate = actions.importCertificate(certificateModel);
        return responseFactory.createOkContentResponse(contentConverter.getJsonString(certificate));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<String> updateCertificate(@PathVariable Long id) {
        Optional<CertificateModel> certificate = actions.updateCertificate(id);
        if (certificate.isPresent()) {
            return responseFactory.createOkContentResponse(contentConverter.getJsonString(certificate.get()));
        }
        return responseFactory.createNotFoundResponse("Certificate resource not found");
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteCertificate(@PathVariable Long id) {
        Optional<CertificateModel> certificate = actions.deleteCertificate(id);
        if (certificate.isPresent()) {
            return responseFactory.createOkContentResponse(contentConverter.getJsonString(certificate.get()));
        }
        return responseFactory.createNotFoundResponse("Certificate resource not found");
    }
}
