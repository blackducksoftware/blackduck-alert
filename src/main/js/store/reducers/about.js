import {ABOUT_INFO_FETCH_ERROR, ABOUT_INFO_FETCHED, ABOUT_INFO_FETCHING, SERIALIZE} from '../actions/types';

const initialState = {
    fetching: false,
    version: '',
    description: '',
    projectUrl: '',
    channelList: [],
    providerList: [],
    initialized: false,
    startupTime: '',
    systemMessages: []
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case ABOUT_INFO_FETCHING:
            return Object.assign({}, state, {
                fetching: true
            });
        case ABOUT_INFO_FETCHED:
            return Object.assign({}, state, {
                fetching: false,
                version: action.version,
                description: action.description,
                projectUrl: action.projectUrl,
                initialized: action.initialized,
                startupTime: action.startupTime,
                systemMessages: action.systemMessages
            });
        case ABOUT_INFO_FETCH_ERROR:
            return Object.assign({}, state, {
                fetching: false
            });
        case SERIALIZE:
            return initialState;
        default:
            return state;
    }
}

export default config;
