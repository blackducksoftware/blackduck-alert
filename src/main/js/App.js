import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import { combineReducers, createStore, applyMiddleware } from 'redux';
import { Provider } from 'react-redux';
import createHistory from 'history/createBrowserHistory'
import { ConnectedRouter, routerReducer, routerMiddleware, push } from 'react-router-redux'

import MainPage from './MainPage';
import LoginPage from './LoginPage';

const history = createHistory();
const middleware = routerMiddleware(history);
const store = createStore(
    combineReducers({
        router: routerReducer
    }),
    applyMiddleware(middleware)
);

class App extends Component {
	constructor(props) {
		super(props);
		this.state = {
				loggedIn: false
		};
		this.handleState = this.handleState.bind(this)
	}

	componentDidMount() {
		var self = this;
		fetch('/api/verify', {
			credentials: "same-origin"
		})
		.then(function(response) {
			if (!response.ok) {
				self.setState({
					loggedIn: false
				});
			} else {
				self.setState({
					loggedIn: true
				});
			}
		})
 		.catch(function(error) {
 		 	console.log(error); 
 		});
		
	}

	handleState(name, value) {
		this.setState({
			[name]: value
		});
	}

	render() {
		let page = null;
		if (this.state.loggedIn) {
			page = <MainPage handleState={this.handleState}></MainPage>
		} else {
			page = <LoginPage handleState={this.handleState}></LoginPage>
		}
		return (
			<div>
				{page}
			</div>
		)
	}
}

ReactDOM.render(
    <Provider store={store}>
        <ConnectedRouter history={history}>
			<App></App>
		</ConnectedRouter>
	</Provider>,
	document.getElementById('react')
);
