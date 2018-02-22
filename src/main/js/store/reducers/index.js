import { routerReducer as routing } from 'react-router-redux';
import { combineReducers } from 'redux';

import config from './config';
import emailConfig from './emailConfig';
import session from './session';

const rootReducer = combineReducers({
    config,
    emailConfig,
    session,
    routing
});

export default rootReducer;
