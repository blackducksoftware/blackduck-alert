import React, { Component } from 'react';
import { connect } from "react-redux";

class CertificatesPage extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div> Test Certificate Content.</div>
        );
    }
}

const mapStateToProps = state => ({});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(CertificatesPage);
