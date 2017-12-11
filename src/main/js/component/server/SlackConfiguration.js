'use strict';
import React from 'react';
import PropTypes from 'prop-types';
import TextInput from '../../field/input/TextInput';
import ServerConfiguration from './ServerConfiguration';

import { alignCenter } from '../../../css/main.css';

class SlackConfiguration extends ServerConfiguration {
	constructor(props) {
		super(props);
	}

	render() {
        let content =
            <div>
			</div>;
        return super.render(content);
	}
};

SlackConfiguration.propTypes = {
    headerText: PropTypes.string,
    configButtonTest: PropTypes.bool,
    baseUrl: PropTypes.string,
    testUrl: PropTypes.string
};

SlackConfiguration.defaultProps = {
    headerText: 'Slack Configuration',
    configButtonTest: false,
    baseUrl: '/configuration/slack'
};

export default SlackConfiguration;
