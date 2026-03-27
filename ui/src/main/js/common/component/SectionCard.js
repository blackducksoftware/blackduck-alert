import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import theme from '../../_theme';

const useStyles = createUseStyles({
    sectionCard: {
        width: ({ width }) => width,
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
    }
});

/**
 * Common component for displaying a section with a title and content. Used in the about page to display different sections of information.
 * @param {string} title - The title to be displayed above the content
 * @param {string} description - A description to be displayed below the title and above the content
 * @param {string} icon - An optional FontAwesome icon name to be displayed to the left of the title
 * @param {string | ReactNode} children - The content to be displayed below the title and description
 * @param {string} width - The width of the section card, default is 100%
 * @returns
 */
export default function SectionCard({ children, title, icon, description, width = '100%' }) {
    const classes = useStyles({ width });

    return (
        <section className={classes.sectionCard}>
            {title && (
                <div className={classes.sectionTitle}>
                    {icon && <FontAwesomeIcon icon={icon} size="lg" color={theme.colors.grey.darkerGrey} />}
                    <h4 className={classes.sectionHeader}>{title}</h4>
                </div>
            )}

            {description && <div className={classes.description}>{description}</div>}

            {children}
        </section>
    );
}

SectionCard.propTypes = {
    children: PropTypes.any.isRequired,
    title: PropTypes.string,
    description: PropTypes.string,
    icon: PropTypes.string
};
