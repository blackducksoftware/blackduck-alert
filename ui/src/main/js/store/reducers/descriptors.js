import {
    DESCRIPTORS_DISTRIBUTION_FETCH_ERROR,
    DESCRIPTORS_DISTRIBUTION_FETCHED,
    DESCRIPTORS_DISTRIBUTION_FETCHING,
    DESCRIPTORS_DISTRIBUTION_RESET,
    DESCRIPTORS_FETCH_ERROR,
    DESCRIPTORS_FETCHED,
    DESCRIPTORS_FETCHING,
    SERIALIZE
} from 'store/actions/types';
import * as HTTPErrorUtils from 'common/util/httpErrorUtilities';

const initialState = {
    fetching: false,
    error: HTTPErrorUtils.createEmptyErrorObject(),
    // may need to rethink this state object
    currentDistributionComponents: null,
    items: []
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case DESCRIPTORS_FETCHING:
            return {
                ...state,
                fetching: true,
                error: HTTPErrorUtils.createEmptyErrorObject()
            };

        case DESCRIPTORS_FETCHED:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                items: [
                    ...action.items
                ]
            };

        case DESCRIPTORS_FETCH_ERROR:
            return {
                ...state,
                fetching: false,
                error: HTTPErrorUtils.createErrorObject(action)
            };

        case DESCRIPTORS_DISTRIBUTION_FETCHING:
            return {
                ...state,
                fetching: true,
                currentDistributionComponents: null,
                error: HTTPErrorUtils.createEmptyErrorObject()
            };

        case DESCRIPTORS_DISTRIBUTION_FETCHED:
            return {
                ...state,
                fetching: false,
                currentDistributionComponents: action.currentDistributionComponents
            };

        case DESCRIPTORS_DISTRIBUTION_FETCH_ERROR:
            return {
                ...state,
                fetching: false,
                currentDistributionComponents: null,
                error: HTTPErrorUtils.createErrorObject(action)
            };

        case DESCRIPTORS_DISTRIBUTION_RESET:
            return {
                ...state,
                fetching: false,
                currentDistributionComponents: null,
                error: HTTPErrorUtils.createEmptyErrorObject()
            };

        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default config;
