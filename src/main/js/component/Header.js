'use strict';

import React from 'react';

import styles from '../../css/main.css';
import logo from '../../img/BDTextLogo.png';

class Header extends React.Component {
	
	
	render() {
		return (
				<div className={styles.header}>
				<img src={logo} alt="logo" />
				</div>
		)
	}
	
}

export default Header;