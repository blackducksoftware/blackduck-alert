// Audit Actions
export const AUDIT_RESEND_ERROR = 'AUDIT_RESEND_ERROR';
export const AUDIT_GET_REQUEST = 'AUDIT_GET_REQUEST';
export const AUDIT_GET_SUCCESS = 'AUDIT_GET_SUCCESS';
export const AUDIT_GET_FAIL = 'AUDIT_GET_FAIL';
export const AUDIT_NOTIFICATION_PUT_REQUEST = 'AUDIT_NOTIFICATION_PUT_REQUEST';
export const AUDIT_NOTIFICATION_PUT_SUCCESS = 'AUDIT_NOTIFICATION_PUT_SUCCESS';
export const AUDIT_NOTIFICATION_PUT_FAIL = 'AUDIT_NOTIFICATION_PUT_FAIL';
export const AUDIT_JOB_PUT_REQUEST = 'AUDIT_JOB_PUT_REQUEST';
export const AUDIT_JOB_PUT_SUCCESS = 'AUDIT_JOB_PUT_SUCCESS';
export const AUDIT_JOB_PUT_FAIL = 'AUDIT_JOB_PUT_FAIL';

// Global Config Actions
export const CONFIG_FETCH_ERROR = 'CONFIG_FETCH_ERROR';
export const CONFIG_FETCH_ALL_ERROR = 'CONFIG_FETCH_ALL_ERROR';
export const CONFIG_FETCHING = 'CONFIG_FETCHING';
export const CONFIG_FETCHED = 'CONFIG_FETCHED';
export const CONFIG_ALL_FETCHED = 'CONFIG_ALL_FETCHED';
export const CONFIG_REFRESH_ERROR = 'CONFIG_REFRESH_ERROR';
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
export const CONFIG_DELETE_ERROR = 'CONFIG_DELETE_ERROR';
export const CONFIG_CLEAR_FIELD_ERRORS = 'CONFIG_CLEAR_FIELD_ERRORS';
export const CONFIG_VALIDATING = 'CONFIG_VALIDATING';
export const CONFIG_VALIDATED = 'CONFIG_VALIDATED';
export const CONFIG_VALIDATE_ERROR = 'CONFIG_VALIDATE_ERROR';

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
export const DISTRIBUTION_JOB_VALIDATING = 'DISTRIBUTION_JOB_VALIDATING';
export const DISTRIBUTION_JOB_VALIDATED = 'DISTRIBUTION_JOB_VALIDATED';
export const DISTRIBUTION_JOB_VALIDATE_ERROR = 'DISTRIBUTION_JOB_VALIDATE_ERROR';

export const DISTRIBUTION_GET_REQUEST = 'DISTRIBUTION_GET_REQUEST';
export const DISTRIBUTION_GET_SUCCESS = 'DISTRIBUTION_GET_SUCCESS';
export const DISTRIBUTION_GET_FAIL = 'DISTRIBUTION_GET_FAIL';
export const DISTRIBUTION_DELETE_REQUEST = 'DISTRIBUTION_DELETE_REQUEST';
export const DISTRIBUTION_DELETE_SUCCESS = 'DISTRIBUTION_DELETE_SUCCESS';
export const DISTRIBUTION_DELETE_FAIL = 'DISTRIBUTION_DELETE_FAIL';

// system message actions
export const SYSTEM_LATEST_MESSAGES_FETCHING = 'SYSTEM_LATEST_MESSAGES_FETCHING';
export const SYSTEM_LATEST_MESSAGES_FETCHED = 'SYSTEM_LATEST_MESSAGES_FETCHED';
export const SYSTEM_LATEST_MESSAGES_FETCH_ERROR = 'SYSTEM_LATEST_MESSAGES_FETCH_ERROR';

// role actions
export const USER_MANAGEMENT_ROLE_GET_REQUEST = 'USER_MANAGEMENT_ROLE_GET_REQUEST';
export const USER_MANAGEMENT_ROLE_GET_SUCCESS = 'USER_MANAGEMENT_ROLE_GET_SUCCESS';
export const USER_MANAGEMENT_ROLE_GET_FAIL = 'USER_MANAGEMENT_ROLE_GET_FAIL';
export const USER_MANAGEMENT_ROLE_VALIDATING = 'USER_MANAGEMENT_ROLE_VALIDATING';
export const USER_MANAGEMENT_ROLE_VALIDATED = 'USER_MANAGEMENT_ROLE_VALIDATED';
export const USER_MANAGEMENT_ROLE_VALIDATION_ERROR = 'USER_MANAGEMENT_ROLE_VALIDATION_ERROR';
export const USER_MANAGEMENT_ROLE_SAVING = 'USER_MANAGEMENT_ROLE_SAVING';
export const USER_MANAGEMENT_ROLE_SAVED = 'USER_MANAGEMENT_ROLE_SAVED';
export const USER_MANAGEMENT_ROLE_SAVE_ERROR = 'USER_MANAGEMENT_ROLE_SAVE_ERROR';
export const USER_MANAGEMENT_ROLE_DELETING = 'USER_MANAGEMENT_ROLE_DELETING';
export const USER_MANAGEMENT_ROLE_DELETED = 'USER_MANAGEMENT_ROLE_DELETED';
export const USER_MANAGEMENT_ROLE_DELETE_ERROR = 'USER_MANAGEMENT_ROLE_DELETE_ERROR';
export const USER_MANAGEMENT_ROLE_DELETING_LIST = 'USER_MANAGEMENT_ROLE_DELETING_LIST';
export const USER_MANAGEMENT_ROLE_DELETED_LIST = 'USER_MANAGEMENT_ROLE_DELETED_LIST';
export const USER_MANAGEMENT_ROLE_DELETE_LIST_ERROR = 'USER_MANAGEMENT_ROLE_DELETE_LIST_ERROR';
export const USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS = 'USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS';
export const USER_MANAGEMENT_USER_BULK_DELETE_FETCH = 'USER_MANAGEMENT_USER_BULK_DELETE_FETCH';
export const USER_MANAGEMENT_USER_BULK_DELETE_SUCCESS = 'USER_MANAGEMENT_USER_BULK_DELETE_SUCCESS';
export const USER_MANAGEMENT_USER_BULK_DELETE_FAIL = 'USER_MANAGEMENT_USER_BULK_DELETE_FAIL';

// user management actions
export const USER_MANAGEMENT_GET_REQUEST = 'USER_MANAGEMENT_GET_REQUEST';
export const USER_MANAGEMENT_GET_SUCCESS = 'USER_MANAGEMENT_GET_SUCCESS';
export const USER_MANAGEMENT_GET_FAIL = 'USER_MANAGEMENT_GET_FAIL';
export const USER_MANAGEMENT_USER_SAVING = 'USER_MANAGEMENT_USER_SAVING';
export const USER_MANAGEMENT_USER_SAVED = 'USER_MANAGEMENT_USER_SAVED';
export const USER_MANAGEMENT_USER_SAVE_ERROR = 'USER_MANAGEMENT_USER_SAVE_ERROR';
export const USER_MANAGEMENT_USER_DELETING = 'USER_MANAGEMENT_USER_DELETING';
export const USER_MANAGEMENT_USER_DELETED = 'USER_MANAGEMENT_USER_DELETED';
export const USER_MANAGEMENT_USER_DELETE_ERROR = 'USER_MANAGEMENT_USER_DELETE_ERROR';
export const USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS = 'USER_MANAGEMENT_USER_CLEAR_FIELD_ERRORS';
export const USER_MANAGEMENT_USER_VALIDATING = 'USER_MANAGEMENT_USER_VALIDATING';
export const USER_MANAGEMENT_USER_VALIDATED = 'USER_MANAGEMENT_USER_VALIDATED';
export const USER_MANAGEMENT_USER_VALIDATE_ERROR = 'USER_MANAGEMENT_USER_VALIDATE_ERROR';

// certificates
export const CERTIFICATES_GET_REQUEST = 'CERTIFICATES_GET_REQUEST';
export const CERTIFICATES_GET_SUCCESS = 'CERTIFICATES_GET_SUCCESS';
export const CERTIFICATES_GET_FAIL = 'CERTIFICATES_GET_FAIL';
export const CERTIFICATE_VALIDATING = 'CERTIFICATE_VALIDATING';
export const CERTIFICATE_VALIDATED = 'CERTIFICATE_VALIDATED';
export const CERTIFICATE_VALIDATE_ERROR = 'CERTIFICATE_VALIDATE_ERROR';
export const CERTIFICATES_SAVING = 'CERTIFICATE_SAVING';
export const CERTIFICATES_SAVED = 'CERTIFICATE_SAVED';
export const CERTIFICATES_SAVE_ERROR = 'CERTIFICATE_SAVE_ERROR';
export const CERTIFICATES_DELETING = 'CERTIFICATE_DELETING';
export const CERTIFICATES_DELETED = 'CERTIFICATE_DELETED';
export const CERTIFICATES_DELETE_ERROR = 'CERTIFICATE_DELETE_ERROR';
export const CERTIFICATES_CLEAR_FIELD_ERRORS = 'CERTIFICATES_CLEAR_FIELD_ERRORS';

// tasks
export const TASKS_GET_REQUEST = 'TASKS_GET_REQUEST';
export const TASKS_GET_SUCCESS = 'TASKS_GET_SUCCESS';
export const TASKS_GET_FAIL = 'TASKS_GET_FAIL';

// Providers
export const PROVIDER_DELETE_REQUEST = 'PROVIDER_DELETE_REQUEST';
export const PROVIDER_DELETE_FAIL = 'PROVIDER_DELETE_FAIL';
export const PROVIDER_DELETE_SUCCESS = 'PROVIDER_DELETE_SUCCESS';
export const PROVIDER_GET_REQUEST = 'PROVIDER_GET_REQUEST';
export const PROVIDER_GET_FAIL = 'PROVIDER_GET_FAIL';
export const PROVIDER_GET_SUCCESS = 'PROVIDER_GET_SUCCESS';
export const PROVIDER_POST_REQUEST = 'PROVIDER_POST_REQUEST';
export const PROVIDER_POST_FAIL = 'PROVIDER_POST_FAIL';
export const PROVIDER_POST_SUCCESS = 'PROVIDER_POST_SUCCESS';
export const PROVIDER_VALIDATE_REQUEST = 'PROVIDER_VALIDATE_REQUEST';
export const PROVIDER_VALIDATE_FAIL = 'PROVIDER_VALIDATE_FAIL';
export const PROVIDER_VALIDATE_SUCCESS = 'PROVIDER_VALIDATE_SUCCESS';
export const PROVIDER_TEST_REQUEST = 'PROVIDER_TEST_REQUEST';
export const PROVIDER_TEST_FAIL = 'PROVIDER_TEST_FAIL';
export const PROVIDER_TEST_SUCCESS = 'PROVIDER_TEST_SUCCESS';
export const PROVIDER_CLEAR_FIELD_ERRORS = 'PROVIDER_CLEAR_FIELD_ERRORS';

// azureBoards
export const AZURE_BOARDS_GET_REQUEST = 'AZURE_BOARDS_GET_REQUEST';
export const AZURE_BOARDS_GET_SUCCESS = 'AZURE_BOARDS_GET_SUCCESS';
export const AZURE_BOARDS_GET_FAIL = 'AZURE_BOARDS_GET_FAIL';
export const AZURE_BOARDS_VALIDATE_REQUEST = 'AZURE_BOARDS_VALIDATE_REQUEST';
export const AZURE_BOARDS_VALIDATE_SUCCESS = 'AZURE_BOARDS_VALIDATE_SUCCESS';
export const AZURE_BOARDS_VALIDATE_FAIL = 'AZURE_BOARDS_VALIDATE_FAIL';
export const AZURE_BOARDS_SAVE_REQUEST = 'AZURE_BOARDS_SAVE_REQUEST';
export const AZURE_BOARDS_SAVE_SUCCESS = 'AZURE_BOARDS_SAVE_SUCCESS';
export const AZURE_BOARDS_SAVE_FAIL = 'AZURE_BOARDS_SAVE_FAIL';
export const AZURE_BOARDS_DELETE_REQUEST = 'AZURE_BOARDS_DELETE_REQUEST';
export const AZURE_BOARDS_DELETE_FAIL = 'AZURE_BOARDS_DELETE_FAIL';
export const AZURE_BOARDS_DELETE_SUCCESS = 'AZURE_BOARDS_DELETE_SUCCESS';
export const AZURE_BOARDS_TEST_REQUEST = 'AZURE_BOARDS_TEST_REQUEST';
export const AZURE_BOARDS_TEST_FAIL = 'AZURE_BOARDS_TEST_FAIL';
export const AZURE_BOARDS_TEST_SUCCESS = 'AZURE_BOARDS_TEST_SUCCESS';
export const AZURE_BOARDS_OAUTH_REQUEST = 'AZURE_BOARDS_OAUTH_REQUEST';
export const AZURE_BOARDS_OAUTH_SUCCESS = 'AZURE_BOARDS_OAUTH_SUCCESS';
export const AZURE_BOARDS_OAUTH_FAIL = 'AZURE_BOARDS_OAUTH_FAIL';
export const AZURE_BOARDS_CLEAR_FIELD_ERRORS = 'AZURE_BOARDS_CLEAR_FIELD_ERRORS';

// Jira Server
export const JIRA_SERVER_GET_REQUEST = 'JIRA_SERVER_GET_REQUEST';
export const JIRA_SERVER_GET_SUCCESS = 'JIRA_SERVER_GET_SUCCESS';
export const JIRA_SERVER_GET_FAIL = 'JIRA_SERVER_GET_FAIL';
export const JIRA_SERVER_VALIDATE_REQUEST = 'JIRA_SERVER_VALIDATE_REQUEST';
export const JIRA_SERVER_VALIDATE_SUCCESS = 'JIRA_SERVER_VALIDATE_SUCCESS';
export const JIRA_SERVER_VALIDATE_FAIL = 'JIRA_SERVER_VALIDATE_FAIL';
export const JIRA_SERVER_SAVE_REQUEST = 'JIRA_SERVER_SAVE_REQUEST';
export const JIRA_SERVER_SAVE_SUCCESS = 'JIRA_SERVER_SAVE_SUCCESS';
export const JIRA_SERVER_SAVE_FAIL = 'JIRA_SERVER_SAVE_FAIL';
export const JIRA_SERVER_DELETE_REQUEST = 'JIRA_SERVER_DELETE_REQUEST';
export const JIRA_SERVER_DELETE_SUCCESS = 'JIRA_SERVER_DELETE_SUCCESS';
export const JIRA_SERVER_DELETE_FAIL = 'JIRA_SERVER_DELETE_FAIL';
export const JIRA_SERVER_TEST_REQUEST = 'JIRA_SERVER_TEST_REQUEST';
export const JIRA_SERVER_TEST_SUCCESS = 'JIRA_SERVER_TEST_SUCCESS';
export const JIRA_SERVER_TEST_FAIL = 'JIRA_SERVER_TEST_FAIL';
export const JIRA_SERVER_PLUGIN_REQUEST = 'JIRA_SERVER_PLUGIN_REQUEST';
export const JIRA_SERVER_PLUGIN_SUCCESS = 'JIRA_SERVER_PLUGIN_SUCCESS';
export const JIRA_SERVER_PLUGIN_FAIL = 'JIRA_SERVER_PLUGIN_FAIL';
export const JIRA_SERVER_CLEAR_FIELD_ERRORS = 'JIRA_SERVER_CLEAR_FIELD_ERRORS';
