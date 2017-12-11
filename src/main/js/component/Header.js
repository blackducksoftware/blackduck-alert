'use strict';

import React, { Component } from 'react';

import { submitButtons, submitContainers, header, fixedHeader, title } from '../../css/header.css';
import logo from '../../img/BDTextLogo.png';

class Header extends Component {
	//constructor is part of the Component lifecycle
	constructor(props) {
		super(props);
		this.handleLogout = this.handleLogout.bind(this);
	}

	handleLogout(event) {
		event.preventDefault();
		var self = this;
		var method = 'POST';
		fetch('/logout', {
			method: method,
			credentials: "same-origin",
			headers: {
				'Content-Type': 'application/json'
			}
		}).then(function(response) {
			if (response.ok) {
				self.props.handleState('loggedIn', false)
			} else {
				return response.json().then(json => {
					let errors = json.errors;
					if (errors) {
						for (var key in errors) {
							if (errors.hasOwnProperty(key)) {
								let name = key.concat('Error');
								let value = errors[key];
								self.setState({
									[name]: value
								});
							}
						}
					}
					self.setState({
						configurationMessage: json.message
					});
				});
			}
		});
	}

	//render is part of the Component lifecycle, used to render the Html
	render() {
		let logout = "";
		if (this.props.includeLogout === true){
			logout = <div className={submitContainers}><input className={submitButtons} type="submit" onClick={this.handleLogout} value="Logout"></input></div>;
		}
		let headerClass = header;
		if (this.props.fixed === true){
			headerClass = fixedHeader;
		}

		return (
			<div className={headerClass}>
				<img src='img/BDTextLogo.png' alt="logo"></img>
                <div className={title}>Black Duck Alert</div>
				{logout}
			</div>
		)
	}

}

export default Header;
