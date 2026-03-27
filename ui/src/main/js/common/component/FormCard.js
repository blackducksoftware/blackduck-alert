import React from 'react';
import * as PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import SectionCard from 'common/component/SectionCard';

const useStyles = createUseStyles({
    formCard: {
        width: 'fit-content',
        margin: '0 auto'
    },
    formCardContent: {
        padding: '10px',
        '& > h5': {
            marginBottom: '20px'
        }
    }
});

const FormCard = ({
    formTitle, children
}) => {
    const classes = useStyles();
    return (
        <div className={classes.formCard}>
            <SectionCard width="640px">
                <div className={classes.formCardContent}>
                    {formTitle && <h5>{formTitle}</h5>}
                    {children}
                </div>
            </SectionCard>
        </div>
    );
};

FormCard.propTypes = {
    formTitle: PropTypes.string,
    children: PropTypes.node.isRequired
};

export default FormCard;
