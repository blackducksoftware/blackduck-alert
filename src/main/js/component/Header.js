'use strict';

import React from 'react';

import styles from '../../css/header.css';
import logo from '../../img/BDTextLogo.png';

class Header extends React.Component {
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
		if (this.props.includeLogout == 'true'){
			logout = <div className={styles.submitContainers}><input className={styles.inputButton} type="submit" onClick={this.handleLogout} value="Logout"></input></div>;
		}
		let headerClass = styles.header;
		if (this.props.fixed == 'true'){
			headerClass = styles.fixedHeader;
		}
		
		return (
				<div className={headerClass}>
				<img src={logo} alt="logo"></img>
				{logout}
				</div>
		)
	}
	
}

export default Header;