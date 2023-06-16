import {
    CERTIFICATE_VALIDATE_ERROR,
    CERTIFICATE_VALIDATED,
    CERTIFICATE_VALIDATING,
    CERTIFICATES_CLEAR_FIELD_ERRORS,
    CERTIFICATES_DELETE_ERROR,
    CERTIFICATES_DELETED,
    CERTIFICATES_DELETING,
    CERTIFICATES_GET_FAIL,
    CERTIFICATES_GET_REQUEST,
    CERTIFICATES_GET_SUCCESS,
    CERTIFICATES_SAVE_ERROR,
    CERTIFICATES_SAVED,
    CERTIFICATES_SAVING,
    SERIALIZE
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    fetching: false,
    data: [],
    error: HTTPErrorUtils.createEmptyErrorObject(),
    saveStatus: ''
};

const certificates = (state = initialState, action) => {
    switch (action.type) {
        case CERTIFICATES_DELETE_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: ''
            };
        case CERTIFICATES_DELETED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: true,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: ''
            };
        case CERTIFICATES_DELETING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: ''
            };
        case CERTIFICATES_GET_FAIL:
            return {
                ...state,
                error: HTTPErrorUtils.createErrorObject(action),
                fetching: false
            };
        case CERTIFICATES_GET_SUCCESS:
            return {
                ...state,
                data: action.certificates,
                fetching: false
            };
        case CERTIFICATES_GET_REQUEST:
            return {
                ...state,
                fetching: true
            };
        case CERTIFICATE_VALIDATING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: 'VALIDATING'
            };
        case CERTIFICATE_VALIDATED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: 'VALIDATED'
            };
        case CERTIFICATE_VALIDATE_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: 'ERROR'
            };
        case CERTIFICATES_SAVE_ERROR:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: 'ERROR'
            };
        case CERTIFICATES_SAVED:
            return {
                ...state,
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: 'SAVED'
            };
        case CERTIFICATES_SAVING:
            return {
                ...state,
                inProgress: true,
                deleteSuccess: false,
                saveStatus: 'SAVING'
            };
        case CERTIFICATES_CLEAR_FIELD_ERRORS: {
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

export default certificates;
