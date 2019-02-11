import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Overlay, Popover } from 'react-bootstrap';

import SystemMessage from 'component/common/SystemMessage';
import { getAboutInfo } from 'store/actions/about';
import { getLatestMessages } from 'store/actions/system';
import '../../css/footer.scss';
import '../../css/messages.scss';
import '../../css/logos.scss';

//import { synopsys_black } from 'Index.js';


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

    componentWillReceiveProps(nextProps) {
        if (!nextProps.fetching) {
            const { latestMessages } = nextProps;
            const showOverlay = this.hasErrorMessages(latestMessages);

            if (!this.state.hideOverlayByUser) {
                this.setState({ showOverlay });
            }
            this.startAutoReload();
        }
    }

    getFontAwesomeIcon() {
        const { latestMessages } = this.props;
        if (this.hasErrorMessages(latestMessages) || this.hasWarninigMessages(latestMessages)) {
            return 'fa fa-exclamation-triangle';
        }
        return 'fa fa-check-circle';
    }

    getIconColor() {
        const { latestMessages } = this.props;
        if (this.hasErrorMessages(latestMessages)) {
            return 'statusPopoverError errorStatus';
        } else if (this.hasWarninigMessages(latestMessages)) {
            return 'warningStatus';
        }
        return 'validStatus';
    }

    hasErrorMessages(messages) {
        return this.containsSeverity(messages, 'ERROR');
    }

    hasWarninigMessages(messages) {
        return this.containsSeverity(messages, 'WARNING');
    }

    containsSeverity(messages, severity) {
        if (messages && messages.length > 0) {
            if (messages.find(message => message.severity === severity)) {
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
        const popover = (<Popover id="system-errors-popover" className="popoverContent" title="System Messages">{errorMessages}</Popover>);
        const overlayComponent = (
            <Overlay
                rootClose
                show={this.state.showOverlay}
                onHide={() => this.setState({ showOverlay: false, hideOverlayByUser: true })}
                container={this}
                placement="top"
                target={() => ReactDOM.findDOMNode(this.target)}
            >
                {popover}
            </Overlay>);
        return (
            <div className="statusPopover">
                <div
                    ref={(button) => {
                        this.target = button;
                    }}
                    onClick={this.handleOverlayButton}
                >
                    <div className={iconColor}><span className={iconClassName} /></div>
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
        // Run reload in 10seconds - kill an existing timer if it exists.
        this.cancelAutoReload();
        this.timeout = setTimeout(() => this.reload(), 10000);
    }

    reload() {
        this.props.getAboutInfo();
        this.props.getLatestMessages();
    }


    render() {
        const { version, projectUrl } = this.props;
        const errorComponent = this.createErrorComponent();
        return (
            <div className="footer">
                <span className="synopsysLogoSpan">
                    <img
                        class="synopsysFooterLogo"
                        src="https://www.synopsys.com/content/dam/synopsys/company/about/legal/synopsys-logos/blacklogo/synopsys_blk.png"
                        alt={projectUrl}
                        href={projectUrl}
                    />
                    &nbsp;|&nbsp;ALERT
                </span>
                <span className="productVersion">v{version}</span>
                <span className="copyright">
                    &nbsp;© 2019&nbsp;
                    <a id="aboutLink" href="https://www.synopsys.com/">Synopsys, Inc.</a>
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
    latestMessages: PropTypes.arrayOf(PropTypes.object)
};

AboutInfoFooter.defaultProps = {
    fetching: false,
    description: '',
    latestMessages: []
};

const mapStateToProps = state => ({
    fetching: state.about.fetching,
    version: state.about.version,
    description: state.about.description,
    projectUrl: state.about.projectUrl,
    latestMessages: state.system.latestMessages
});

const mapDispatchToProps = dispatch => ({
    getAboutInfo: () => dispatch(getAboutInfo()),
    getLatestMessages: () => dispatch(getLatestMessages())
});

export default connect(mapStateToProps, mapDispatchToProps)(AboutInfoFooter);
