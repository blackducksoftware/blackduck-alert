'use strict';

import React, { Component } from 'react';
import Logo from './Logo';

import '../../../css/header.scss';

class Header extends Component {
	render() {
		return (
			<div className="header">
				<Logo />
                <div className="title">Alert</div>
                <div className="submitContainers">
                    <div className="loginSpacer"></div>
                </div>
			</div>
		)
	}

}

export default Header;
