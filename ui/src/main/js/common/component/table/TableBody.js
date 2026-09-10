import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import MultiSelectCell from 'common/component/table/cell/MultiSelectCell';
import WrapperCell from 'common/component/table/cell/WrapperCell';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles((theme) => ({
    tableRow: {
        height: '52px',
        borderBottom: `1px solid ${theme.colors.grey.lighterGrey}`
    },
    nonMultiSelect: {
        '& > :first-child': {
            paddingLeft: '35px'
        }
    },
    nonMultiSelectExpandable: {
        '& > :nth-child(2)': {
            paddingLeft: '35px'
        }
    },
    rowExpander: {
        cursor: 'pointer',
        '&:hover': {
            backgroundColor: theme.colors.white.darkWhite
        }
    },
    rowExpanded: {
        cursor: 'pointer',
        backgroundColor: theme.colors.white.darkWhite
    },
    expandIconCell: {
        width: '40px',
        paddingLeft: '12px'
    },
    expandIconPlaceholder: {
        width: '40px'
    },
    expandedContentRow: {
        borderBottom: `1px solid ${theme.colors.grey.lighterGrey}`
    },
    expandedContentCell: {
        padding: '16px 24px',
        overflowWrap: 'break-word',
        wordBreak: 'break-word',
        maxWidth: 0
    }
}));

const ACTION_ELEMENTS = ['button', 'a', 'input', 'select', 'textarea', 'label'];

function isAction(element) {
    return element?.closest ? ACTION_ELEMENTS.some(tag => element.closest(tag)) : false;
}

function ExpandedContentCell({ data, expandedRows, rowIndex, isExpandable, ExpandableContent }) {
    const classes = useStyles();

    if (!ExpandableContent) {
        return null;
    }

    if (isExpandable?.(data)) {
        const isExpanded = expandedRows.includes(rowIndex);
        return (
            <td className={classes.expandIconCell}>
                <FontAwesomeIcon
                    icon={isExpanded ? 'chevron-down' : 'chevron-right'}
                    size="sm"
                    fixedWidth
                />
            </td>
        );
    }

    return <td className={classes.expandIconPlaceholder} />;
}

ExpandedContentCell.propTypes = {
    data: PropTypes.object,
    expandedRows: PropTypes.arrayOf(PropTypes.number),
    rowIndex: PropTypes.number,
    isExpandable: PropTypes.func,
    ExpandableContent: PropTypes.func
};

const TableBody = ({ columns, multiSelect, tableData, selected, onSelected, disableSelectOptions, cellId, ExpandableContent, isExpandable }) => {
    const classes = useStyles();
    const [expandedRows, setExpandedRows] = useState([]);

    const totalColumns = columns.length + (multiSelect ? 1 : 0) + (ExpandableContent ? 1 : 0);

    useEffect(() => {
        setExpandedRows([]);
    }, [tableData]);

    function isRowExpanded(rowIndex) {
        return expandedRows.includes(rowIndex);
    }

    function handleRowExpander(rowIndex) {
        if (isRowExpanded(rowIndex)) {
            setExpandedRows(prev => prev.filter(i => i !== rowIndex));
        } else {
            setExpandedRows(prev => [...prev, rowIndex]);
        }
    }

    function handleRowClick(event, rowData, rowIndex) {
        if (isExpandable?.(rowData) && !isAction(event.target)) {
            handleRowExpander(rowIndex);
        }
    }

    return (
        <tbody>
            {tableData?.map((rowData, rowIndex) => {
                const expandable = isExpandable?.(rowData);
                const expanded = isRowExpanded(rowIndex);

                const tableRowClass = classNames(classes.tableRow, {
                    [classes.nonMultiSelect]: !multiSelect && !ExpandableContent,
                    [classes.nonMultiSelectExpandable]: !multiSelect && ExpandableContent,
                    [classes.rowExpander]: expandable && !expanded,
                    [classes.rowExpanded]: expandable && expanded
                });

                return (
                    <React.Fragment key={`${rowIndex}-table-row`}>
                        <tr
                            className={tableRowClass}
                            role={expandable ? 'button' : undefined}
                            tabIndex={expandable ? 0 : undefined}
                            aria-expanded={expandable ? expanded : undefined}
                            onClick={event => handleRowClick(event, rowData, rowIndex)}
                            onKeyDown={(event) => {
                                 if (!expandable || isAction(event.target)) {
                                     return;
                                 }
                                 if (event.key === 'Enter' || event.key === ' ') {
                                     event.preventDefault();
                                     handleRowExpander(rowIndex);
                                 }
                             }}
                        >
                            <ExpandedContentCell
                                data={rowData}
                                rowIndex={rowIndex}
                                expandedRows={expandedRows}
                                isExpandable={isExpandable}
                                ExpandableContent={ExpandableContent}
                            />

                            {multiSelect && (
                                <MultiSelectCell
                                    data={rowData}
                                    selected={selected}
                                    onSelected={onSelected}
                                    disableSelectOptions={disableSelectOptions}
                                    cellId={cellId}
                                />
                            )}

                            {columns.map((col, colIndex) => {
                                const columnKey = `${col.key}-${rowIndex}-${colIndex}`;
                                if (col.customCell) {
                                    const CustomCell = col.customCell;
                                    return (
                                        <WrapperCell key={columnKey} settings={col.settings}>
                                            <CustomCell id={col.key} data={rowData} settings={col.settings} customCallback={col.customCallback} />
                                        </WrapperCell>
                                    );
                                }
                                return (
                                    <WrapperCell key={columnKey} datakey={col.key}>
                                        {rowData[col.key] ?? '—'}
                                    </WrapperCell>
                                );
                            })}
                        </tr>

                        {expanded && (
                            <tr className={classes.expandedContentRow}>
                                <td className={classes.expandedContentCell} colSpan={totalColumns}>
                                    {ExpandableContent(rowData)}
                                </td>
                            </tr>
                        )}
                    </React.Fragment>
                );
            })}
        </tbody>
    );
};

TableBody.propTypes = {
    columns: PropTypes.arrayOf(PropTypes.shape({
        key: PropTypes.string,
        label: PropTypes.string,
        sortable: PropTypes.bool
    })),
    multiSelect: PropTypes.bool,
    selected: PropTypes.arrayOf(PropTypes.string),
    onSelected: PropTypes.func,
    tableData: PropTypes.arrayOf(PropTypes.object),
    disableSelectOptions: PropTypes.object,
    cellId: PropTypes.string,
    ExpandableContent: PropTypes.func,
    isExpandable: PropTypes.func
};

export default TableBody;
