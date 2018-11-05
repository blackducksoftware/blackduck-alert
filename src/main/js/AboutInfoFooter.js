import React from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import '../css/footer.scss';
import {OverlayTrigger, Popover} from 'react-bootstrap'

import {getAboutInfo} from './store/actions/about';

class AboutInfoFooter extends React.Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.getAboutInfo();
    }

    createErrorComponent() {
        const {startupErrors} = this.props;

        const message = startupErrors ? startupErrors : "none";
        const divClassName = startupErrors ? "errorStatus" : "validStatus";
        const iconClassName = startupErrors ? "fa fa-exclamation-triangle" : "fa fa-check-circle";

        const popover = (<Popover id="system-errors-popover" title="System Errors">{message}</Popover>);
        return (
            <div className="statusPopover">
                <OverlayTrigger
                    container={this}
                    trigger="click"
                    placement="top"
                    overlay={popover}>
                    <div className={divClassName}><span className={iconClassName}></span></div>
                </OverlayTrigger>
            </div>);
    }

    render() {
        const {version, projectUrl} = this.props;
        const errorComponent = this.createErrorComponent();
        return (
            <div className="footer">
                <a className="productName" alt={projectUrl} href={projectUrl}>
                    <strong>BLACK</strong>DUCK | Alert
                </a>
                <span className="productVersion">v{version}</span>
                <span className="copyright">
                    &nbsp;© 2018&nbsp;
                    <a id="aboutLink" href='http://www.blackducksoftware.com'>Black Duck Software, Inc</a>
                    &nbsp;All rights reserved.
                </span>
                {errorComponent}
            </div>
        );
    }
}

AboutInfoFooter.propTypes = {
    fetching: PropTypes.bool,
    version: PropTypes.string.isRequired,
    description: PropTypes.string,
    projectUrl: PropTypes.string.isRequired,
    startupErrors: PropTypes.string
};

AboutInfoFooter.defaultProps = {
    fetching: false,
    version: '',
    description: '',
    projectUrl: '',
    startupErrors: ''
};

const mapStateToProps = state => ({
    fetching: state.about.fetching,
    version: state.about.version,
    description: state.about.description,
    projectUrl: state.about.projectUrl,
    startupErrors: state.about.startupErrors
});

const mapDispatchToProps = dispatch => ({
    getAboutInfo: () => dispatch(getAboutInfo())
});

export default connect(mapStateToProps, mapDispatchToProps)(AboutInfoFooter);
