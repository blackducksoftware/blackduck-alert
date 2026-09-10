import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Table from 'common/component/table/Table';
import EventTypeCell from 'page/audit/EventTypeCell';
import DistributionLastSentCell from 'page/audit/DistributionLastSentCell';
import RefreshFailureCell from 'page/audit/RefreshFailureCell';

const emptyTableConfig = {
    message: 'There are no records to display for this table.'
};

const useStyles = createUseStyles(theme => ({
    expandedContentCell: {
        display: 'flex',
        flexDirection: 'column'
    },
    errorTitle: {
        fontWeight: 'bold',
        color: 'red'
    },
    stackTrace: {
        display: '-webkit-box',
        '-webkit-line-clamp': 3,
        '-webkit-box-orient': 'vertical',
        overflow: 'hidden'
    },
    copyButtonWrapper: {
        position: 'relative',
        alignSelf: 'flex-end',
        margin: ['8px', '8px', 0]
    },
    copyButton: {
        display: 'inline-flex',
        alignItems: 'center',
        gap: '6px',
        padding: 0,
        color: theme.colors.grey.default,
        background: 'none',
        border: 'none',
        cursor: 'pointer',
        fontSize: '13px',
        '&:hover': {
            color: theme.colors.grey.darkerGrey
        }
    },
    copiedButton: {
        color: theme.colors.status.success.text
    },
    tooltip: {
        position: 'absolute',
        bottom: 'calc(100% + 6px)',
        right: 0,
        padding: ['3px', '8px'],
        borderRadius: '4px',
        backgroundColor: theme.colors.grey.darkerGrey,
        color: theme.colors.white.default,
        fontSize: '12px',
        whiteSpace: 'nowrap',
        pointerEvents: 'none',
        opacity: 1,
        transition: 'opacity 0.2s ease'
    }
}));

function CopyStacktraceButton({ stackTrace }) {
    const classes = useStyles();
    const [copied, setCopied] = useState(false);

    function handleCopy() {
        navigator.clipboard.writeText(stackTrace)
            .then(() => {
                setCopied(true);
                setTimeout(() => setCopied(false), 2000);
            })
            .catch(() => {
                // Clipboard can fail due to permissions / insecure context.
            });
    }

    return (
        <div className={classes.copyButtonWrapper}>
            {copied && <div className={classes.tooltip}>Copied stacktrace!</div>}
            <button
                type="button"
                className={`${classes.copyButton}${copied ? ` ${classes.copiedButton}` : ''}`}
                onClick={handleCopy}
            >
                <FontAwesomeIcon icon={copied ? 'check' : 'copy'} size="sm" />
                Copy Stacktrace
            </button>
        </div>
    );
}

CopyStacktraceButton.propTypes = {
    stackTrace: PropTypes.string
};

const DistributionTable = ({ data }) => {
    const classes = useStyles();
    const COLUMNS = [{
        key: 'name',
        label: 'Distribution Job',
        sortable: false
    }, {
        key: 'eventType',
        label: 'Event Type',
        sortable: false,
        customCell: EventTypeCell
    }, {
        key: 'timeLastSent',
        label: 'Time Last Sent',
        sortable: false,
        customCell: DistributionLastSentCell
    }, {
        key: 'refreshJob',
        sortable: false,
        customCell: RefreshFailureCell,
        settings: {
            alignment: 'center',
            type: 'job',
            notificationId: data.id
        }
    }];

    function getExpandableRowContent(data) {
        return (
            <div className={classes.expandedContentCell}>
                <div className={classes.errorTitle}>{data.errorMessage}</div>
                <div className={classes.stackTrace}>{data.errorStackTrace}</div>
                {data.errorStackTrace && (
                    <CopyStacktraceButton stackTrace={data.errorStackTrace} />
                )}
            </div>
        );
    }

    return (
        <Table
            tableData={data?.jobs}
            columns={COLUMNS}
            emptyTableConfig={emptyTableConfig}
            isExpandable={rowData => !!rowData}
            ExpandableContent={getExpandableRowContent}
        />
    );
};

DistributionTable.propTypes = {
    data: PropTypes.shape({
        jobs: PropTypes.object,
        id: PropTypes.string
    })
};

export default DistributionTable;
