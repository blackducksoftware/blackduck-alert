import {createUseStyles} from "react-jss";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import PropTypes from "prop-types";
import React from "react";

const useStyles = createUseStyles({
    enabled: {
        color: 'green'
    },
    disabled: {
        color: 'red'
    }
});

const JiraFieldAlwaysCreateJsonCell = ({ data }) => {
    const classes = useStyles();
    const { createJsonObject } = data;

    return (
        <div className={createJsonObject ? classes.enabled : classes.disabled}>
            <FontAwesomeIcon icon={createJsonObject ? 'check' : 'times'} />
        </div>

    );
};

JiraFieldAlwaysCreateJsonCell.propTypes = {
    data: PropTypes.shape({
        createJsonObject: PropTypes.bool
    })
};

export default JiraFieldAlwaysCreateJsonCell;