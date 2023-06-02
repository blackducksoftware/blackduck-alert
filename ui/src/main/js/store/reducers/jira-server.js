import {
    SERIALIZE,
    JIRA_SERVER_GET_REQUEST,
    JIRA_SERVER_GET_SUCCESS,
    JIRA_SERVER_GET_FAIL,
    JIRA_SERVER_VALIDATE_REQUEST,
    JIRA_SERVER_VALIDATE_SUCCESS,
    JIRA_SERVER_VALIDATE_FAIL,
    JIRA_SERVER_SAVE_REQUEST,
    JIRA_SERVER_SAVE_SUCCESS,
    JIRA_SERVER_SAVE_FAIL,
    JIRA_SERVER_DELETE_REQUEST,
    JIRA_SERVER_DELETE_SUCCESS,
    JIRA_SERVER_DELETE_FAIL,
    JIRA_SERVER_TEST_REQUEST,
    JIRA_SERVER_TEST_SUCCESS,
    JIRA_SERVER_TEST_FAIL,
    JIRA_SERVER_PLUGIN_REQUEST,
    JIRA_SERVER_PLUGIN_SUCCESS,
    JIRA_SERVER_PLUGIN_FAIL,
    JIRA_SERVER_CLEAR_FIELD_ERRORS
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    fetching: false,
    data: {},
    error: HTTPErrorUtils.createEmptyErrorObject(),
    fieldErrors: {},
    saveStatus: '',
    deleteStatus: '',
    pluginStatus: '',
    testStatus: ''
};

const jiraServer = (state = initialState, action) => {
    switch (action.type) {
        case JIRA_SERVER_GET_REQUEST:
            return {
                ...state,
                fetching: true,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case JIRA_SERVER_GET_SUCCESS:
            return {
                ...state,
                fetching: false,
                data: action.data,
                fieldErrors: action.errors || {}
            };
        case JIRA_SERVER_GET_FAIL:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case JIRA_SERVER_VALIDATE_REQUEST:
            return {
                ...state,
                fetching: true,
                saveStatus: 'VALIDATING'
            };
        case JIRA_SERVER_VALIDATE_SUCCESS:
            return {
                ...state,
                fetching: false,
                saveStatus: 'VALIDATED',
                error: HTTPErrorUtils.createEmptyErrorObject()
            };
        case JIRA_SERVER_VALIDATE_FAIL:
            return {
                ...state,
                fetching: false,
                saveStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case JIRA_SERVER_SAVE_REQUEST:
            return {
                ...state,
                fetching: true,
                saveStatus: 'SAVING',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case JIRA_SERVER_SAVE_SUCCESS:
            return {
                ...state,
                fetching: false,
                saveStatus: 'SAVED',
                fieldErrors: action.errors || {}
            };
        case JIRA_SERVER_SAVE_FAIL:
            return {
                ...state,
                fetching: false,
                saveStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case JIRA_SERVER_DELETE_REQUEST:
            return {
                ...state,
                fetching: true,
                deleteStatus: 'DELETING'
            };
        case JIRA_SERVER_DELETE_SUCCESS:
            return {
                ...state,
                fetching: false,
                deleteStatus: 'DELETED',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {}
            };
        case JIRA_SERVER_DELETE_FAIL:
            return {
                ...state,
                fetching: false,
                deleteStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case JIRA_SERVER_TEST_REQUEST:
            return {
                ...state,
                fetching: true,
                saveStatus: '',
                testStatus: 'TESTING'
            };
        case JIRA_SERVER_TEST_SUCCESS:
            return {
                ...state,
                fetching: false,
                testStatus: 'SUCCESS'
            };
        case JIRA_SERVER_TEST_FAIL:
            return {
                ...state,
                fetching: false,
                testStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action)
            };
        case JIRA_SERVER_PLUGIN_REQUEST:
            return {
                ...state,
                fetching: true,
                pluginStatus: 'FETCHING'
            };
        case JIRA_SERVER_PLUGIN_SUCCESS:
            return {
                ...state,
                fetching: false,
                pluginStatus: 'SUCCESS',
                message: action.message,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {}
            };
        case JIRA_SERVER_PLUGIN_FAIL:
            return {
                ...state,
                fetching: false,
                pluginStatus: 'ERROR',
                error: HTTPErrorUtils.createErrorObject(action),
                fieldErrors: action.errors || {}
            };
        case JIRA_SERVER_CLEAR_FIELD_ERRORS:
            return {
                ...state,
                saveStatus: '',
                deleteStatus: '',
                pluginStatus: '',
                testStatus: '',
                error: HTTPErrorUtils.createEmptyErrorObject(),
                fieldErrors: {}
            };

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default jiraServer;
