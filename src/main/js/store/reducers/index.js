import { routerReducer as routing } from 'react-router-redux';
import { combineReducers } from 'redux';

import audit from './audit';
import config from './config';
import emailConfig from './emailConfig';
import hipChatConfig from './hipChatConfig';
import projects from './projects';
import schedulingConfig from './schedulingConfig';
import session from './session';

const rootReducer = combineReducers({
    audit,
    config,
    emailConfig,
    hipChatConfig,
    projects,
    schedulingConfig,
    session,
    routing
});

export default rootReducer;
