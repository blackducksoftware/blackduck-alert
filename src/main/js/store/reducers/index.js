import { routerReducer as routing } from 'react-router-redux';
import { combineReducers } from 'redux';

import config from './config';
import emailConfig from './emailConfig';
import hipChatConfig from './hipChatConfig';
import schedulingConfig from './schedulingConfig';
import session from './session';

const rootReducer = combineReducers({
    config,
    emailConfig,
    hipChatConfig,
    schedulingConfig,
    session,
    routing
});

export default rootReducer;
