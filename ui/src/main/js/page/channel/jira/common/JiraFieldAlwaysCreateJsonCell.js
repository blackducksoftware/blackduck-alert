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
    const { treatValueAsJson } = data;

    return (
        <div className={treatValueAsJson ? classes.enabled : classes.disabled}>
            <FontAwesomeIcon icon={treatValueAsJson ? 'check' : 'times'} />
        </div>

    );
};

JiraFieldAlwaysCreateJsonCell.propTypes = {
    data: PropTypes.shape({
        treatValueAsJson: PropTypes.bool
    })
};

export default JiraFieldAlwaysCreateJsonCell;