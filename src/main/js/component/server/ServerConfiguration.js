'use strict';

import React from 'react';
import ConfigButtons from '../ConfigButtons';
import Configuration from '../Configuration';
import { alignCenter, progressIcon } from '../../../css/main.css';
import { content_block } from '../../../css/server_config.css';

class ServerConfiguration extends Configuration {
    constructor(props) {
        super(props);
    }

    render(content) {
        const { headerText, configButtonTest, configButtonsSave } = this.props;
        return (
			<div>
                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <h1>Server Configuration / { headerText }</h1>
    			    {content}
                    <ConfigButtons isFixed={false} includeSave={configButtonsSave} includeTest={configButtonTest} type="submit" onTestClick={this.handleTestSubmit} />
                </form>
			</div>
		)
    }
};

export default ServerConfiguration;
