import React, { useState } from 'react';
// import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    ldapForm: {
        padding: [0, '20px']
    }
});

const LdapForm = () => {
    const classes = useStyles();

    return (
        <div className={classes.ldapForm}>
            <h2>LDAP Configuration</h2>
        </div>
    );
};

// LdapForm.propTypes = {
// };

export default LdapForm;
