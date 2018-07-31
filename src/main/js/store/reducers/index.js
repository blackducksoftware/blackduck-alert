import {routerReducer as routing} from 'react-router-redux';
import {combineReducers} from 'redux';

import about from './about';
import audit from './audit';
import config from './config';
import emailConfig from './emailConfig';
import hipChatConfig from './hipChatConfig';
import projects from './projects';
import schedulingConfig from './schedulingConfig';
import session from './session';
import refresh from './refresh';

const rootReducer = combineReducers({
    about,
    audit,
    config,
    emailConfig,
    hipChatConfig,
    projects,
    schedulingConfig,
    session,
    refresh,
    routing
});

export default rootReducer;
