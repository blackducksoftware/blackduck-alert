import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import { getAboutInfo } from '../../store/actions/about';

class AboutInfo extends React.Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.getAboutInfo();
    }

    render() {
        const { version,description, projectUrl, channelList, providerList } = this.props;
        return (
            <div>
                <h1>
                    <span className="fa fa-info"/>
                    About
                </h1>
                <div className="form-horizontal">
                    <div className="form-group">
                        <label className="col-sm-3 control-label">Description:</label>
                        <div className="col-sm-8">
                            {description}
                        </div>
                    </div>
                    <div className="form-group">
                        <label className="col-sm-3 control-label">Version:</label>
                        <div className="col-sm-8">
                            {version}
                        </div>
                    </div>
                    <div className="form-group">
                        <label className="col-sm-3 control-label">Project URL:</label>
                        <div className="col-sm-8">
                            <a alt={projectUrl} href={projectUrl}>{projectUrl}</a>
                        </div>
                    </div>
                    <div className="form-group">
                        <label className="col-sm-3 control-label">Supported Distribution Channels:</label>
                        <div className="col-sm-8">
                            {channelList.sort().join(", ")}
                        </div>
                    </div>
                    <div className="form-group">
                        <label className="col-sm-3 control-label">Supported Providers:</label>
                        <div className="col-sm-8">
                            {providerList.sort().join(", ")}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

AboutInfo.propTypes = {
    fetching: PropTypes.bool,
    version: PropTypes.string.isRequired,
    description: PropTypes.string,
    projectUrl: PropTypes.string.isRequired,
    channelList: PropTypes.arrayOf(PropTypes.string),
    providerList: PropTypes.arrayOf(PropTypes.string)
};

AboutInfo.defaultProps = {
    fetching: false,
    version: '',
    description: '',
    projectUrl: '',
    channelList: [],
    providerList: []
};

const mapStateToProps = state => ({
    fetching: state.about.fetching,
    version: state.about.version,
    description: state.about.description,
    projectUrl: state.about.projectUrl,
    channelList: state.about.channelList,
    providerList: state.about.providerList
});

const mapDispatchToProps = dispatch => ({
    getAboutInfo: () => dispatch(getAboutInfo())
});

export default connect(mapStateToProps, mapDispatchToProps)(AboutInfo);
