'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import { Switch, Route, Router } from 'react-router';
import { createHashHistory } from 'history'

import MainPage from './MainPage';
import LoginPage from './LoginPage';

const browserHistory = createHashHistory()

ReactDOM.render(
		<Router history={browserHistory}>
		<Switch>
		<Route exact path='/login' component={LoginPage} />
		<Route exact path='/' component={MainPage} />
		</Switch>
		</Router>,
		document.getElementById('react')
);