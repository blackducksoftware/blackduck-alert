'use strict';

import React, { Component } from 'react';
import Logo from './Logo';
import { submitButtons, submitContainers, header, fixedHeader, title, loginSpacer } from '../../css/header.css';

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
		fetch('/api/logout', {
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
		})
		.catch(function(error) {
 		 	console.log(error);
 		});
	}

	//render is part of the Component lifecycle, used to render the Html
	render() {
		let logout = "";
		if (this.props.includeLogout === true){
			logout = <input className={submitButtons} type="submit" onClick={this.handleLogout} value="Logout"></input>;
		} else {
            logout = <div className={loginSpacer}></div>
        }
		let headerClass = header;
		if (this.props.fixed === true){
			headerClass = fixedHeader;
		}

		return (
			<div className={headerClass}>
				<Logo />
                <div className={title}>Alert</div>
                <div className={submitContainers}>
				    {logout}
                </div>
			</div>
		)
	}

}

export default Header;
