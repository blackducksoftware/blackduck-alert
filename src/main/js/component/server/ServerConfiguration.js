'use strict';

import React from 'react';
import ConfigButtons from '../ConfigButtons';
import Configuration from '../Configuration';
import { alignCenter } from '../../../css/main.css';
import { content_block } from '../../../css/server_config.css';

class ServerConfiguration extends Configuration {
    constructor(props) {
        super(props);
    }
    render(content) {
        const { headerText, configButtonTest } = this.props;
        return (
			<div>
                <form onSubmit={this.handleSubmit}>
                    <h1 className={alignCenter}>{ headerText }</h1>
                    <div className={content_block}>
    			    {content}
                    </div>
                    <ConfigButtons includeTest={configButtonTest} type="submit" onTestClick={this.handleTestSubmit} />
                    <p name="configurationMessage">{this.state.configurationMessage}</p>
                </form>
			</div>
		)
    }
};

export default ServerConfiguration;
