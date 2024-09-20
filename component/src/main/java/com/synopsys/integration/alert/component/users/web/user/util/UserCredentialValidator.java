package com.synopsys.integration.alert.component.users.web.user.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.component.users.web.user.UserActions;

@Component
public class UserCredentialValidator {
    public static final String PASSWORD_TOO_SHORT_ERROR_MESSAGE = "The password needs to be at least 8 characters long";
    public static final String PASSWORD_TOO_LONG_ERROR_MESSAGE = "The password needs to be less than 128 characters long";
    public static final String PASSWORD_NO_DIGIT_MESSAGE = "The password needs to contain at least one digit";
    public static final String PASSWORD_NO_UPPER_CASE_MESSAGE = "The password needs to contain at least one upper case character";
    public static final String PASSWORD_NO_LOWER_CASE_MESSAGE = "The password needs to contain at least one lower case character";
    public static final String PASSWORD_NO_SPECIAL_CHARACTER_MESSAGE = "The password needs to contain at least one special character";
    public static final String PASSWORD_TOO_SIMPLE_MESSAGE = "The password is too easy to guess, use a stronger password";

    private static final int DEFAULT_MINIMUM_PASSWORD_LENGTH = 8;
    private static final int DEFAULT_MAXIMUM_PASSWORD_LENGTH = 128;

    private final Logger logger = AlertLoggerFactory.getLogger(getClass());
    private final Set<String> alertPasswordDictionary = loadDictionary();

    public List<AlertFieldStatus> validatePassword(String passwordValue) {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        validatePasswordLength(fieldErrors, passwordValue);
        validatePasswordComplexity(fieldErrors, passwordValue);
        validateCommonPasswords(fieldErrors, passwordValue);
        return fieldErrors;
    }

    public void validatePasswordLength(List<AlertFieldStatus> fieldErrors, String passwordValue) {
        if (fieldErrors.isEmpty() && DEFAULT_MINIMUM_PASSWORD_LENGTH > passwordValue.length()) {
            fieldErrors.add(AlertFieldStatus.error(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, PASSWORD_TOO_SHORT_ERROR_MESSAGE));
        }
        if (fieldErrors.isEmpty() && DEFAULT_MAXIMUM_PASSWORD_LENGTH < passwordValue.length()) {
            fieldErrors.add(AlertFieldStatus.error(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, PASSWORD_TOO_LONG_ERROR_MESSAGE));
        }
    }

    public void validatePasswordComplexity(List<AlertFieldStatus> fieldErrors, String passwordValue) {
        boolean containsDigit = false;
        boolean containsUpper = false;
        boolean containsLower = false;
        boolean containsSpecial = false;
        String specialCharacters = " !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        for (char c : passwordValue.toCharArray()) {
            if (Character.isDigit(c)) {
                containsDigit = true;
            }
            if (Character.isUpperCase(c)) {
                containsUpper = true;
            }
            if (Character.isLowerCase(c)) {
                containsLower = true;
            }
            if (specialCharacters.contains(String.valueOf(c))) {
                containsSpecial = true;
            }
        }
        if (!containsDigit) {
            fieldErrors.add(AlertFieldStatus.error(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, PASSWORD_NO_DIGIT_MESSAGE));
        }
        if(!containsUpper) {
            fieldErrors.add(AlertFieldStatus.error(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, PASSWORD_NO_UPPER_CASE_MESSAGE));
        }
        if(!containsLower) {
            fieldErrors.add(AlertFieldStatus.error(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, PASSWORD_NO_LOWER_CASE_MESSAGE));
        }
        if(!containsSpecial) {
            fieldErrors.add(AlertFieldStatus.error(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, PASSWORD_NO_SPECIAL_CHARACTER_MESSAGE));
        }
    }

    public void validateCommonPasswords(List<AlertFieldStatus> fieldErrors, String passwordValue) {
        if (alertPasswordDictionary.contains(passwordValue)) {
            fieldErrors.add(AlertFieldStatus.error(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, PASSWORD_TOO_SIMPLE_MESSAGE));
        }
    }

    private Set<String> loadDictionary() {
        ClassPathResource classPathResource = new ClassPathResource("10k-most-common.txt");
        File dictionaryFile;
        try {
            dictionaryFile = classPathResource.getFile();
        } catch (IOException e) {
            logger.error("Failed to load alert dictionary.", e);
            return Set.of();
        }

        Set<String> result = new HashSet<>();
        if (dictionaryFile.exists()) {
            int count = 0;
            try (BufferedReader lines = new BufferedReader(new FileReader(dictionaryFile))) {
                String line;
                while ((line = lines.readLine()) != null) {
                    if (!line.isEmpty()) {
                        result.add(line.toLowerCase());
                        ++count;
                    }
                }
                logger.debug("Loaded {} entries into password dictionary from {}", count, dictionaryFile);
            } catch (IOException ex) {
                logger.error("Reading password dictionary file {}", dictionaryFile, ex);
            }
        }
        return result;
    }
}
