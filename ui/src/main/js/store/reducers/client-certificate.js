import {
    SERIALIZE,
    CLIENT_CERTIFICATE_GET_REQUEST,
    CLIENT_CERTIFICATE_GET_SUCCESS,
    CLIENT_CERTIFICATE_GET_ERROR,
    CLIENT_CERTIFICATE_POST_REQUEST,
    CLIENT_CERTIFICATE_POST_SUCCESS,
    CLIENT_CERTIFICATE_POST_ERROR,
    CLIENT_CERTIFICATE_DELETE_REQUEST,
    CLIENT_CERTIFICATE_DELETE_SUCCESS,
    CLIENT_CERTIFICATE_DELETE_ERROR,
    CLIENT_CERTIFICATE_CLEAR_FIELD_ERRORS
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    fetching: false,
    data: {},
    error: HTTPErrorUtils.createEmptyErrorObject(),
    saveStatus: ''
};

const clientCertificate = (state = initialState, action) => {
    switch (action.type) {
        case CLIENT_CERTIFICATE_GET_REQUEST:
            return {
                ...state,
                fetching: true
            };
        case CLIENT_CERTIFICATE_GET_SUCCESS:
            return {
                ...state,
                data: action.certificate,
                fetching: false
            };
        case CLIENT_CERTIFICATE_GET_ERROR:
            return {
                ...state,
                error: HTTPErrorUtils.createErrorObject(action),
                fetching: false
            };
        case CLIENT_CERTIFICATE_POST_REQUEST:
            return {
                ...state,
                inProgress: true,
                saveStatus: 'SAVING'
            };
        case CLIENT_CERTIFICATE_POST_SUCCESS:
            return {
                ...state,
                inProgress: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: 'SAVED'
            };
        case CLIENT_CERTIFICATE_POST_ERROR:
            return {
                ...state,
                inProgress: false,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: 'ERROR'
            };
        case CLIENT_CERTIFICATE_DELETE_REQUEST:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false
            };
        case CLIENT_CERTIFICATE_DELETE_SUCCESS:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: true,
                error: HTTPErrorUtils.createEmptyErrorObject()
            };
        case CLIENT_CERTIFICATE_DELETE_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action)
            };
        case CLIENT_CERTIFICATE_CLEAR_FIELD_ERRORS: {
            return {
                ...state,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: ''
            };
        }
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default clientCertificate;
