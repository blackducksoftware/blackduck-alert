// Audit Actions
export const AUDIT_FETCHING = 'AUDIT_FETCHING';
export const AUDIT_FETCHED = 'AUDIT_FETCHED';
export const AUDIT_FETCH_ERROR = 'AUDIT_FETCH_ERROR';
export const AUDIT_RESEND_START = 'AUDIT_RESEND_START';
export const AUDIT_RESEND_COMPLETE = 'AUDIT_RESEND_COMPLETE';
export const AUDIT_RESEND_ERROR = 'AUDIT_RESEND_ERROR';

// Global Config Actions
export const CONFIG_FETCHING = 'CONFIG_FETCHING';
export const CONFIG_FETCHED = 'CONFIG_FETCHED';
export const CONFIG_ALL_FETCHED = 'CONFIG_ALL_FETCHED';
export const CONFIG_REFRESH = 'CONFIG_REFRESH';
export const CONFIG_REFRESHING = 'CONFIG_REFRESHING';
export const CONFIG_UPDATE_ERROR = 'CONFIG_UPDATE_ERROR';
export const CONFIG_UPDATING = 'CONFIG_UPDATING';
export const CONFIG_UPDATED = 'CONFIG_UPDATED';
export const CONFIG_TESTING = 'CONFIG_TESTING';
export const CONFIG_TEST_SUCCESS = 'CONFIG_TEST_SUCCESS';
export const CONFIG_TEST_FAILED = 'CONFIG_TEST_FAILED';
export const CONFIG_DELETED = 'CONFIG_DELETED';
export const CONFIG_DELETING = 'CONFIG_DELETING';
export const CONFIG_CLEAR_FIELD_ERRORS = 'CONFIG_CLEAR_FIELD_ERRORS';

// Auth related Actions
export const SESSION_INITIALIZING = 'SESSION_INITIALIZING';
export const SESSION_LOGGING_IN = 'SESSION_LOGGING_IN';
export const SESSION_LOGGED_IN = 'SESSION_LOGGED_IN';
export const SESSION_LOGGED_OUT = 'SESSION_LOGGED_OUT';
export const SESSION_LOGIN_ERROR = 'SESSION_LOGIN_ERROR';
export const SESSION_CANCEL_LOGOUT = 'SESSION_CANCEL_LOGOUT';
export const SESSION_CONFIRM_LOGOUT = 'SESSION_CONFIRM_LOGOUT';
export const SESSION_LOGOUT = 'SESSION_LOGOUT';
export const SAML_ENABLED = 'SAML_ENABLED';
export const SERIALIZE = 'SERIALIZE';

// About related Actions
export const ABOUT_INFO_FETCHING = 'ABOUT_INFO_FETCHING';
export const ABOUT_INFO_FETCHED = 'ABOUT_INFO_FETCHED';
export const ABOUT_INFO_FETCH_ERROR = 'ABOUT_INFO_FETCH_ERROR';

// Refresh related Actions
export const REFRESH_ENABLE = 'REFRESH_ENABLE';
export const REFRESH_DISABLE = 'REFRESH_DISABLE';

// descriptor Actions
export const DESCRIPTORS_FETCHING = 'DESCRIPTORS_FETCHING';
export const DESCRIPTORS_FETCHED = 'DESCRIPTORS_FETCHED';
export const DESCRIPTORS_FETCH_ERROR = 'DESCRIPTORS_FETCH_ERROR';

export const DESCRIPTORS_DISTRIBUTION_FETCHING = 'DESCRIPTORS_DISTRIBUTION_FETCHING';
export const DESCRIPTORS_DISTRIBUTION_FETCHED = 'DESCRIPTORS_DISTRIBUTION_FETCHED';
export const DESCRIPTORS_DISTRIBUTION_FETCH_ERROR = 'DESCRIPTORS_DISTRIBUTION_FETCH_ERROR';
export const DESCRIPTORS_DISTRIBUTION_RESET = 'DESCRIPTORS_DISTRIBUTION_RESET';

// distribution job Actions
export const DISTRIBUTION_JOB_UPDATE_AUDIT_INFO = 'DISTRIBUTION_JOB_UPDATE_AUDIT_INFO';
export const DISTRIBUTION_JOB_FETCHING = 'DISTRIBUTION_JOB_FETCHING';
export const DISTRIBUTION_JOB_FETCHED = 'DISTRIBUTION_JOB_FETCHED';
export const DISTRIBUTION_JOB_FETCH_ERROR = 'DITRIBUTION_JOB_FETCH_ERROR';
export const DISTRIBUTION_JOB_SAVING = 'DISTRIBUTION_JOB_UPDATING';
export const DISTRIBUTION_JOB_SAVED = 'DISTRIBUTION_JOB_UPDATED';
export const DISTRIBUTION_JOB_SAVE_ERROR = 'DISTRIBUTION_JOB_UPDATE_ERROR';
export const DISTRIBUTION_JOB_UPDATING = 'DISTRIBUTION_JOB_UPDATING';
export const DISTRIBUTION_JOB_UPDATED = 'DISTRIBUTION_JOB_UPDATED';
export const DISTRIBUTION_JOB_UPDATE_ERROR = 'DISTRIBUTION_JOB_UPDATE_ERROR';
export const DISTRIBUTION_JOB_TESTING = 'DISTRIBUTION_JOB_TESTING';
export const DISTRIBUTION_JOB_TEST_SUCCESS = 'DISTRIBUTION_JOB_TEST_SUCCESS';
export const DISTRIBUTION_JOB_TEST_FAILURE = 'DISTRIBUTION_JOB_TEST_FAILURE';
export const DISTRIBUTION_JOB_CHECK_DESCRIPTOR = 'DISTRIBUTION_JOB_CHECK_DESCRIPTOR';
export const DISTRIBUTION_JOB_CHECK_DESCRIPTOR_SUCCESS = 'DISTRIBUTION_JOB_CHECK_DESCRIPTOR_SUCCESS';
export const DISTRIBUTION_JOB_CHECK_DESCRIPTOR_FAILURE = 'DISTRIBUTION_JOB_CHECK_DESCRIPTOR_FAILURE';
export const DISTRIBUTION_JOB_DELETING = 'DISTRIBUTION_JOB_DELETING';
export const DISTRIBUTION_JOB_DELETED = 'DISTRIBUTION_JOB_DELETED';
export const DISTRIBUTION_JOB_DELETE_ERROR = 'DISTRIBUTION_JOB_DELETE_ERROR';
export const DISTRIBUTION_JOB_FETCHING_ALL = 'DISTRIBUTION_JOB_FETCHING_ALL';
export const DISTRIBUTION_JOB_FETCHED_ALL = 'DISTRIBUTION_JOB_FETCHED_ALL';
export const DISTRIBUTION_JOB_FETCH_ERROR_ALL = 'DISTRIBUTION_JOB_FETCH_ERROR_ALL';
export const DISTRIBUTION_JOB_FETCH_ALL_NONE_FOUND = 'DISTRIBUTION_JOB_FETCH_ALL_NONE_FOUND';
export const DISTRIBUTION_JOB_DELETE_OPEN_MODAL = 'DISTRIBUTION_JOB_DELETE_OPEN_MODAL';
export const DISTRIBUTION_JOB_VALIDATE_ALL_FETCHING = 'DISTRIBUTION_JOB_VALIDATE_ALL_FETCHING';
export const DISTRIBUTION_JOB_VALIDATE_ALL_FETCHED = 'DISTRIBUTION_JOB_VALIDATE_ALL_FETCHED';
export const DISTRIBUTION_JOB_VALIDATE_ALL_ERROR = 'DISTRIBUTION_JOB_VALIDATE_ALL_ERROR';
// system message actions

export const SYSTEM_LATEST_MESSAGES_FETCHING = 'SYSTEM_LATEST_MESSAGES_FETCHING';
export const SYSTEM_LATEST_MESSAGES_FETCHED = 'SYSTEM_LATEST_MESSAGES_FETCHED';
export const SYSTEM_LATEST_MESSAGES_FETCH_ERROR = 'SYSTEM_LATEST_MESSAGES_FETCH_ERROR';

// user managemention actions

// role actions
export const USER_MANAGEMENT_ROLE_FETCHING_ALL = 'USER_MANAGEMENT_ROLE_FETCHING_ALL';
export const USER_MANAGEMENT_ROLE_FETCHED_ALL = 'USER_MANAGEMENT_ROLE_FETCHED_ALL';
export const USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL = 'USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL';
export const USER_MANAGEMENT_ROLE_SAVING = 'USER_MANAGEMENT_ROLE_SAVING';
export const USER_MANAGEMENT_ROLE_SAVED = 'USER_MANAGEMENT_ROLE_SAVED';
export const USER_MANAGEMENT_ROLE_SAVE_ERROR = 'USER_MANAGEMENT_ROLE_SAVE_ERROR';
export const USER_MANAGEMENT_ROLE_DELETING = 'USER_MANAGEMENT_ROLE_DELETING';
export const USER_MANAGEMENT_ROLE_DELETED = 'USER_MANAGEMENT_ROLE_DELETED';
export const USER_MANAGEMENT_ROLE_DELETE_ERROR = 'USER_MANAGEMENT_ROLE_DELETE_ERROR';
export const USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS = 'USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS';
// user actions
export const USER_MANAGEMENT_USER_FETCHING_ALL = 'USER_MANAGEMENT_USER_FETCHING_ALL';
export const USER_MANAGEMENT_USER_FETCHED_ALL = 'USER_MANAGEMENT_USER_FETCHED_ALL';
export const USER_MANAGEMENT_USER_FETCH_ERROR_ALL = 'USER_MANAGEMENT_USER_FETCH_ERROR_ALL';
export const USER_MANAGEMENT_USER_SAVING = 'USER_MANAGEMENT_USER_SAVING';
export const USER_MANAGEMENT_USER_SAVED = 'USER_MANAGEMENT_USER_SAVED';
export const USER_MANAGEMENT_USER_SAVE_ERROR = 'USER_MANAGEMENT_USER_SAVE_ERROR';
export const USER_MANAGEMENT_USER_DELETING = 'USER_MANAGEMENT_USER_DELETING';
export const USER_MANAGEMENT_USER_DELETED = 'USER_MANAGEMENT_USER_DELETED';
export const USER_MANAGEMENT_USER_DELETE_ERROR = 'USER_MANAGEMENT_USER_DELETE_ERROR';
export const USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS = 'USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS';
// certificates
export const CERTIFICATES_FETCHING_ALL = 'CERTIFICATES_FETCHING_ALL';
export const CERTIFICATES_FETCHED_ALL = 'CERTIFICATES_FETCHED_ALL';
export const CERTIFICATES_FETCH_ERROR_ALL = 'CERTIFICATES_FETCH_ERROR_ALL';
export const CERTIFICATES_SAVING = 'CERTIFICATE_SAVING';
export const CERTIFICATES_SAVED = 'CERTIFICATE_SAVED';
export const CERTIFICATES_SAVE_ERROR = 'CERTIFICATE_SAVE_ERROR';
export const CERTIFICATES_DELETING = 'CERTIFICATE_DELETING';
export const CERTIFICATES_DELETED = 'CERTIFICATE_DELETED';
export const CERTIFICATES_DELETE_ERROR = 'CERTIFICATE_DELETE_ERROR';
export const CERTIFICATES_CLEAR_FIELD_ERRORS = 'CERTIFICATES_CLEAR_FIELD_ERRORS';

