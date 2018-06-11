import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import '../css/footer.scss';

import { getAboutInfo } from './store/actions/about';

class AboutInfo extends React.Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.getAboutInfo();
    }

    render() {
        const { version, projectUrl } = this.props;
        return (
            <div className="footer">
                <a className="productName" alt={projectUrl} href={projectUrl}>
                    <strong>BLACK</strong>DUCK | Alert
                </a>
                <span className="productVersion">v{version}</span>
                <span className="copyright">
                    &nbsp;© 2018&nbsp;
                    <a id="about-blackduck" href='http://www.blackducksoftware.com'>Black Duck Software, Inc</a>
                    &nbsp;All rights reserved.
                </span>
            </div>
        );
    }
}

AboutInfo.propTypes = {
    fetching: PropTypes.bool,
    version: PropTypes.string.isRequired,
    description: PropTypes.string,
    projectUrl: PropTypes.string.isRequired
};

AboutInfo.defaultProps = {
    fetching: false,
    version: '',
    description: '',
    projectUrl: ''
};

const mapStateToProps = state => ({
    fetching: state.about.fetching,
    version: state.about.version,
    description: state.about.description,
    projectUrl: state.about.projectUrl
});

const mapDispatchToProps = dispatch => ({
    getAboutInfo: () => dispatch(getAboutInfo())
});

export default connect(mapStateToProps, mapDispatchToProps)(AboutInfo);
