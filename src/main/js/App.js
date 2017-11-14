'use strict';

import React from 'react';
import ReactDOM from 'react-dom';

import MainPage from './MainPage';
import LoginPage from './LoginPage';


class App extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
				loggedIn: false
		};
	}
	
	componentDidMount() {
		var self = this;
		fetch('/verify', {
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
		});
	}
	
	render() {
		let page = null;
		if (this.state.loggedIn) {
			page = <MainPage></MainPage>
		} else {
			page = <LoginPage></LoginPage>
		}
		return (
			<div>
				{page}
			</div>
		)
	}
	
}

ReactDOM.render(
		<div>
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
		<App></App>
		</div>,
		document.getElementById('react')
);