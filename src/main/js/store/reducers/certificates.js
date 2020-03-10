import {
    CERTIFICATES_DELETE_ERROR,
    CERTIFICATES_DELETED,
    CERTIFICATES_DELETING,
    CERTIFICATES_FETCH_ERROR_ALL,
    CERTIFICATES_FETCHED_ALL,
    CERTIFICATES_FETCHING_ALL,
    CERTIFICATES_SAVE_ERROR,
    CERTIFICATES_SAVED,
    CERTIFICATES_SAVING,
    SERIALIZE
} from "../actions/types";

const initialState = {
    inProgress: false,
    fetching: false,
    deleteSuccess: false,
    data: [],
    certificateFetchError: '',
    certificateSaveError: null,
    certificateDeleteError: null,
};

const certificates = (state = initialState, action) => {
    switch (action.type) {
        case CERTIFICATES_DELETE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                certificateDeleteError: action.certificateDeleteError
            });
        case CERTIFICATES_DELETED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: true
            });
        case CERTIFICATES_DELETING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false
            });
        case CERTIFICATES_FETCH_ERROR_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                certificatesFetchError: action.certificatesFetchError,
                fetching: false
            });
        case CERTIFICATES_FETCHED_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                data: action.certificates,
                fetching: false
            });
        case CERTIFICATES_FETCHING_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                data: [],
                fetching: true
            });
        case CERTIFICATES_SAVE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                certificateSaveError: action.certificateSaveError
            });
        case CERTIFICATES_SAVED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false
            });
        case CERTIFICATES_SAVING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false
            });
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default certificates;
