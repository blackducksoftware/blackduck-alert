import React from 'react';
import ReactDOM from 'react-dom';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import '../css/footer.scss';
import {Overlay, Popover} from 'react-bootstrap'

import {getAboutInfo} from './store/actions/about';

class AboutInfoFooter extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            showOverlay: false
        };
        this.createErrorComponent = this.createErrorComponent.bind(this);
        this.createErrorList = this.createErrorList.bind(this);
        this.handleOverlayButton = this.handleOverlayButton.bind(this);
    }

    componentDidMount() {
        this.props.getAboutInfo();

    }

    componentWillReceiveProps(nextProps) {
        const {startupErrors} = nextProps;
        const showOverlay = startupErrors && startupErrors.length > 0 ? true : false;
        this.setState({showOverlay: showOverlay});
    }

    createErrorComponent() {
        const errorMessages = this.createErrorList();
        const showOverlay = errorMessages ? true : false;
        const divClassName = showOverlay ? "errorStatus" : "validStatus";
        const iconClassName = showOverlay ? "fa fa-exclamation-triangle" : "fa fa-check-circle";
        const popover = (<Popover id="system-errors-popover" title="System Errors">{errorMessages}</Popover>);
        const overlayComponent = (
            <Overlay
                rootClose
                show={this.state.showOverlay}
                onHide={() => this.setState({showOverlay: false})}
                container={this}
                placement="top"
                target={() => ReactDOM.findDOMNode(this.target)}
            >
                {popover}
            </Overlay>);
        return (
            <div className="statusPopover">
                <div ref={button => {
                    this.target = button
                }} onClick={this.handleOverlayButton}>
                    <div className={divClassName}><span className={iconClassName}></span></div>
                </div>
                {overlayComponent}
            </div>
        );
    }

    createErrorList() {
        const {startupErrors} = this.props;
        if (startupErrors && startupErrors.length > 0) {
            return startupErrors.map((message) => {
                return (<div>{message}</div>);
            });
        } else {
            return null;
        }
    }

    handleOverlayButton() {
        this.setState({showOverlay: !this.state.showOverlay});
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
    startupErrors: PropTypes.arrayOf(PropTypes.string)
};

AboutInfoFooter.defaultProps = {
    fetching: false,
    version: '',
    description: '',
    projectUrl: '',
    startupErrors: []
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
