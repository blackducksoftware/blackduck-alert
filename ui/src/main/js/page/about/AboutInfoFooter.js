import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Overlay, Popover, PopoverBody, PopoverHeader } from 'react-bootstrap';

import SystemMessage from 'common/component/SystemMessage';
import { getAboutInfo } from 'store/actions/about';
import { getLatestMessages } from 'store/actions/system';
import '../../../css/footer.scss';
import '../../../css/messages.scss';
import '../../../css/logos.scss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import BlackDuckLogoAllBlack from '/src/main/img/BlackDuckLogoAllBlack.png';

class AboutInfoFooter extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            showOverlay: false,
            hideOverlayByUser: false
        };
        this.createErrorComponent = this.createErrorComponent.bind(this);
        this.createMessageList = this.createMessageList.bind(this);
        this.handleOverlayButton = this.handleOverlayButton.bind(this);
        this.reload = this.reload.bind(this);
    }

    componentDidMount() {
        this.props.getAboutInfo();
        this.props.getLatestMessages();
        this.startAutoReload();
    }

    componentDidUpdate(prevProps) {
        const { latestMessages, fetching } = this.props;
    
        // Only update if fetching state changed from true to false
        if (prevProps.fetching && !fetching) {
            const showOverlay = this.hasErrorMessages(latestMessages);

            if (!this.state.hideOverlayByUser) {
                this.setState({ showOverlay });
            }
        }
        
        // Only start auto-reload if messages actually changed
        if (prevProps.latestMessages !== latestMessages) {
            this.startAutoReload();
        }
    }

    getFontAwesomeIcon() {
        const { latestMessages } = this.props;
        if (this.hasErrorMessages(latestMessages) || this.hasWarningMessages(latestMessages)) {
            return 'exclamation-triangle';
        }
        return 'check-circle';
    }

    getIconColor() {
        const { latestMessages } = this.props;
        if (this.hasErrorMessages(latestMessages)) {
            return 'statusPopoverError errorStatus';
        }
        if (this.hasWarningMessages(latestMessages)) {
            return 'warningStatus';
        }
        return 'validStatus';
    }

    hasErrorMessages(messages) {
        return this.containsSeverity(messages, 'ERROR');
    }

    hasWarningMessages(messages) {
        return this.containsSeverity(messages, 'WARNING');
    }

    containsSeverity(messages, severity) {
        if (messages && messages.length > 0) {
            if (messages.find((message) => message.severity === severity)) {
                return true;
            }
            return false;
        }
        return false;
    }

    createMessageList() {
        const { latestMessages } = this.props;
        if (latestMessages && latestMessages.length > 0) {
            return latestMessages.map((message) => {
                const itemKey = `system_message_${message.createdAt}`;
                return (<SystemMessage key={itemKey} createdAt={message.createdAt} content={message.content} severity={message.severity} />);
            });
        }
        return null;
    }

    createErrorComponent() {
        const errorMessages = this.createMessageList();
        const iconColor = this.getIconColor();
        const iconClassName = this.getFontAwesomeIcon();
        const popover = (
            <Popover id="system-errors-popover" className="popoverContainer">
                <PopoverHeader>System Messages</PopoverHeader>
                <PopoverBody>
                    {errorMessages}
                </PopoverBody>
            </Popover>
        );
        const overlayComponent = (
            <Overlay
                rootClose
                show={this.state.showOverlay}
                onHide={() => this.setState({ showOverlay: false, hideOverlayByUser: true })}
                placement="top"
                target={() => ReactDOM.findDOMNode(this.target)}
            >
                {popover}
            </Overlay>
        );
        return (
            <div id="about-footer-status" className="statusPopover">
                <div
                    ref={(button) => {
                        this.target = button;
                    }}
                    onClick={this.handleOverlayButton}
                >
                    <div className={iconColor}>
                        <FontAwesomeIcon icon={iconClassName} className="alert-icon" size="lg" />
                    </div>
                </div>
                {overlayComponent}
            </div>
        );
    }

    handleOverlayButton() {
        this.setState({ showOverlay: !this.state.showOverlay, hideOverlayByUser: !this.state.hideOverlayByUser });
    }

    cancelAutoReload() {
        clearTimeout(this.timeout);
    }

    startAutoReload() {
        // Run reload in 30seconds - kill an existing timer if it exists.
        this.cancelAutoReload();
        this.timeout = setTimeout(() => this.reload(), 30000);
    }

    reload() {
        this.props.getAboutInfo();
        this.props.getLatestMessages();
    }

    render() {
        const { version, projectUrl, copyrightYear } = this.props;
        const errorComponent = this.createErrorComponent();
        return (
            <div className="footer">
                <span className="logoContainer">
                    <a href={projectUrl}>
                        <img
                            className="blackduckFooterLogo"
                            src={BlackDuckLogoAllBlack}
                            alt={projectUrl}
                        />
                    </a>
                    <span className="logoDivider" />
                    <span className="footerAlertText">ALERT</span>
                </span>
                <span className="productVersion">
                    v
                    {version}
                </span>
                <span className="copyright">
                    &nbsp;© {copyrightYear}&nbsp;
                    <a id="aboutLink" href="https://www.blackduck.com/">Black Duck Software, Inc.</a>
                    &nbsp;All rights reserved
                </span>
                {errorComponent}
            </div>
        );
    }
}

AboutInfoFooter.propTypes = {
    getAboutInfo: PropTypes.func.isRequired,
    getLatestMessages: PropTypes.func.isRequired,
    fetching: PropTypes.bool,
    version: PropTypes.string.isRequired,
    description: PropTypes.string,
    projectUrl: PropTypes.string.isRequired,
    copyrightYear: PropTypes.string.isRequired,
    latestMessages: PropTypes.arrayOf(PropTypes.object)
};

AboutInfoFooter.defaultProps = {
    fetching: false,
    description: '',
    latestMessages: []
};

const mapStateToProps = (state) => ({
    fetching: state.about.fetching,
    version: state.about.version,
    description: state.about.description,
    projectUrl: state.about.projectUrl,
    copyrightYear: state.about.copyrightYear,
    latestMessages: state.system.latestMessages
});

const mapDispatchToProps = (dispatch) => ({
    getAboutInfo: () => dispatch(getAboutInfo()),
    getLatestMessages: () => dispatch(getLatestMessages())
});

export default connect(mapStateToProps, mapDispatchToProps)(AboutInfoFooter);
