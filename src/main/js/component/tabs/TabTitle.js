'use strict';

import React, { Component } from 'react';
import { channelTabs, tabTitle, titleText } from '../../../css/tabs.css';

class TabTitle extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const { text, icon } = this.props;
        let iconElement = null;
        if(icon) {
            const fontAwesomeIcon = `fa ${icon}`;
            iconElement = <i className={fontAwesomeIcon} aria-hidden='true'></i>;
        }

        return (
            <span className={tabTitle}>
                {iconElement}
                <span className={titleText}>{text}</span>
            </span>
        );
    }
};

export default TabTitle;
