'use strict';

import React from 'react';

import styles from '../css/main.css';
import logo from '../img/BDTextLogo.png';

import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';

class LoginPage extends React.Component {
	constructor() {
		super();
		this.state = {
				mainIndex: 0,
				channelIndex: 0
		};
	}

	render() {
		return (
				<h1> YAY </h1>
		);
	}
}

export default LoginPage;