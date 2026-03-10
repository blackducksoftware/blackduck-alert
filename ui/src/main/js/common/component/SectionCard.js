import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles(theme => ({
    sectionCard: {
        borderRadius: '12px',
        backgroundColor: theme.colors.white.default,
        border: theme.defaultBorder,
        padding: '20px',
        flexGrow: 1,
        boxShadow: `0 1px 3px 0 ${theme.colors.borderColor}, 0 1px 2px -1px ${theme.colors.borderColor}`,
        '& > form': {
            marginBottom: 0
        }
    },

    sectionTitle: {
        display: 'flex',
        flexDirection: 'row',
        
        alignItems: 'center',
        columnGap: '8px'
    },
    sectionHeader: {
        fontSize: '18px',
        fontWeight: 600,
        marginBottom: 0
    },
    description: {
        marginTop: '8px',
        fontSize: '16px',
        color: theme.colors.grey.darkerGrey
    },
    content: {
        marginTop: '28px'
    }
    
}));

export default function SectionCard({ children, title, icon, description }) {
    const classes = useStyles();

    return (
        <section className={classes.sectionCard}>
            {title && (
                <div className={classes.sectionTitle}>
                    {icon && <FontAwesomeIcon icon={icon} size="lg" color="#4a5565" />}
                    <h4 className={classes.sectionHeader}>{title}</h4>
                </div>
            )}
            
            {description && <div className={classes.description}>{description}</div>}

            <div className={classes.content}>
                {children}
            </div>
        </section>
    );
}

SectionCard.propTypes = {
    children: PropTypes.any.isRequired,
    title: PropTypes.string,
    description: PropTypes.string,
    icon: PropTypes.string
};