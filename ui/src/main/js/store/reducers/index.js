import { combineReducers } from 'redux';
import { connectRouter } from 'connected-react-router';

import about from 'store/reducers/about';
import audit from 'store/reducers/audit';
import azure from 'store/reducers/azure';
import certificates from 'store/reducers/certificates';
import descriptors from 'store/reducers/descriptors';
import distribution from 'store/reducers/distribution';
import jiraServer from 'store/reducers/jira-server';
import provider from 'store/reducers/provider';
import session from 'store/reducers/session';
import system from 'store/reducers/system';
import refresh from 'store/reducers/refresh';
import roles from 'store/reducers/roles';
import tasks from 'store/reducers/tasks';
import users from 'store/reducers/users';

const rootReducer = (history) => combineReducers({
    router: connectRouter(history),
    about,
    audit,
    azure,
    certificates,
    descriptors,
    distribution,
    jiraServer,
    provider,
    session,
    system,
    refresh,
    roles,
    tasks,
    users
});

export default rootReducer;
