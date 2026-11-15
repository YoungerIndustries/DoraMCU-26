/* ============================================
   DoraMCU Shared Component Library
   ============================================
   Universal UI components used across all client layers:
   - static/ (Spring Boot web portal)
   - dora-electron/ (Electron desktop app)
   - dora-web/ (PWA and platform-adaptive clients)
   
   Import this file after variables.css to get all shared components.
   ============================================ */

'use strict';

/**
 * SharedComponents — Universal UI component factory
 * 
 * Usage:
 *   // Toast notification
 *   SharedComponents.toast('Success!', { type: 'success' });
 *   
 *   // Modal dialog
 *   const modal = SharedComponents.modal({ title: 'Confirm', body: 'Are you sure?' });
 *   modal.open();
 *   
 *   // Button
 *   const btn = SharedComponents.button('Submit', { variant: 'primary' });
 *   document.querySelector('.form').appendChild(btn);
 */
const SharedComponents = (function() {
    'use strict';

    // ── Toast Notification System ──

    /**
     * Display a toast notification
     * @param {string} message - Toast message text
     * @param {Object} options - Toast options
     * @param {string} [options.type='info'] - Toast type: success, warning, danger, info
     * @param {number} [options.duration=4000] - Duration in ms (0 = persistent)
     * @param {string} [options.position='top-right'] - Position: top-right, top-left, bottom-right, bottom-left, top-center, bottom-center
     * @param {Function} [options.onClose] - Callback when toast is closed
     * @returns {HTMLElement} The toast element
     */
    function toast(message, options = {}) {
        const {
            type = 'info',
            duration = 4000,
            position = 'top-right',
            onClose = null
        } = options;

        // Ensure toast container exists
        const container = getOrCreateToastContainer(position);

        // Create toast element
        const toastEl = document.createElement('div');
        toastEl.className = `shared-toast shared-toast--${type}`;
        toastEl.setAttribute('role', 'alert');
        toastEl.setAttribute('aria-live', 'polite');

        // Icon based on type
        const icons = {
            success: '✓',
            warning: '⚠',
            danger: '✕',
            info: 'ℹ'
        };

        toastEl.innerHTML = `
            <span class="shared-toast__icon">${icons[type] || icons.info}</span>
            <span class="shared-toast__message">${escapeHtml(message)}</span>
            <button class="shared-toast__close" aria-label="Close notification">&times;</button>
        `;

        // Add to container
        container.appendChild(toastEl);

        // Close button handler
        const closeBtn = toastEl.querySelector('.shared-toast__close');
        const closeToast = () => {
            toastEl.classList.add('shared-toast--removing');
            setTimeout(() => {
                if (toastEl.parentNode) {
                    toastEl.parentNode.removeChild(toastEl);
                    if (onClose) onClose();
                }
            }, 300);
        };

        closeBtn.addEventListener('click', closeToast);

        // Auto-close after duration
        if (duration > 0) {
            setTimeout(closeToast, duration);
        }

        return toastEl;
    }

    /**
     * Get or create the toast container for a given position
     * @param {string} position - Toast container position
     * @returns {HTMLElement}
     * @private
     */
    function getOrCreateToastContainer(position) {
        let container = document.querySelector(`.shared-toast-container--${position}`);
        if (!container) {
            container = document.createElement('div');
            container.className = `shared-toast-container shared-toast-container--${position}`;
            container.setAttribute('aria-live', 'polite');
            container.setAttribute('aria-atomic', 'false');
            document.body.appendChild(container);
        }
        return container;
    }

    // ── Modal Dialog System ──

    /**
     * Create a modal dialog
     * @param {Object} options - Modal options
     * @param {string} [options.title=''] - Modal title
     * @param {string|HTMLElement} [options.body=''] - Modal body content (HTML string or element)
     * @param {string} [options.footer=''] - Modal footer content (HTML string or element)
     * @param {boolean} [options.scrollable=false] - Whether body is scrollable
     * @param {boolean} [options.closable=true] - Whether modal can be closed by clicking overlay
     * @param {boolean} [options.escapeClosable=true] - Whether modal can be closed with Escape key
     * @param {string} [options.size='md'] - Modal size: sm, md, lg, xl
     * @param {Function} [options.onOpen] - Callback when modal opens
     * @param {Function} [options.onClose] - Callback when modal closes
     * @returns {Object} Modal API with open(), close(), and element properties
     */
    function modal(options = {}) {
        const {
            title = '',
            body = '',
            footer = '',
            scrollable = false,
            closable = true,
            escapeClosable = true,
            size = 'md',
            onOpen = null,
            onClose = null
        } = options;

        // Create modal structure
        const overlay = document.createElement('div');
        overlay.className = 'shared-modal-overlay hidden';
        overlay.setAttribute('role', 'dialog');
        overlay.setAttribute('aria-modal', 'true');
        overlay.setAttribute('aria-labelledby', `shared-modal-title-${Date.now()}`);

        const sizeClass = `shared-modal--${size}`;
        const scrollableClass = scrollable ? 'shared-modal__body--scrollable' : '';

        const bodyContent = body instanceof HTMLElement ? '' : body;
        overlay.innerHTML = `
            <div class="shared-modal ${sizeClass}">
                <div class="shared-modal__header">
                    <h3 class="shared-modal__title" id="${overlay.getAttribute('aria-labelledby')}">${escapeHtml(title)}</h3>
                    <button class="shared-modal__close-btn" aria-label="Close dialog">&times;</button>
                </div>
                <div class="shared-modal__body ${scrollableClass}">
                    ${bodyContent}
                </div>
                ${footer ? `<div class="shared-modal__footer">${typeof footer === 'string' ? footer : ''}</div>` : ''}
            </div>
        `;

        // Append body element if provided
        if (body instanceof HTMLElement) {
            overlay.querySelector('.shared-modal__body').appendChild(body);
        }
        // Append footer element if provided
        if (footer instanceof HTMLElement) {
            const footerEl = overlay.querySelector('.shared-modal__footer');
            if (footerEl) footerEl.appendChild(footer);
        }

        document.body.appendChild(overlay);

        // Close functionality
        const closeBtn = overlay.querySelector('.shared-modal__close-btn');
        let isOpen = false;

        const close = () => {
            if (!isOpen) return;
            isOpen = false;
            overlay.classList.add('shared-modal--closing');
            overlay.classList.add('hidden');
            document.body.style.overflow = '';
            if (onClose) onClose();
            // Remove after animation
            setTimeout(() => {
                if (overlay.parentNode) {
                    overlay.parentNode.removeChild(overlay);
                }
            }, 300);
        };

        const open = () => {
            if (isOpen) return;
            isOpen = true;
            overlay.classList.remove('hidden');
            // Force reflow for animation
            void overlay.offsetWidth;
            overlay.classList.add('shared-modal--opening');
            document.body.style.overflow = 'hidden';
            if (onOpen) onOpen();
        };

        // Event listeners
        closeBtn.addEventListener('click', close);

        if (closable) {
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) {
                    close();
                }
            });
        }

        if (escapeClosable) {
            const handleEscape = (e) => {
                if (e.key === 'Escape' && isOpen) {
                    close();
                    document.removeEventListener('keydown', handleEscape);
                }
            };
            document.addEventListener('keydown', handleEscape);
        }

        // Set focus on first focusable element
        const originalFocus = document.activeElement;
        const focusOnOpen = () => {
            const focusable = overlay.querySelector('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
            if (focusable) {
                focusable.focus();
            }
        };

        return {
            open,
            close,
            overlay,
            get body() { return overlay.querySelector('.shared-modal__body'); },
            get header() { return overlay.querySelector('.shared-modal__header'); },
            get footer() { return overlay.querySelector('.shared-modal__footer'); },
            setTitle(newTitle) {
                const titleEl = overlay.querySelector('.shared-modal__title');
                if (titleEl) titleEl.textContent = newTitle;
            }
        };
    }

    // ── Button Factory ──

    /**
     * Create a styled button
     * @param {string} text - Button text
     * @param {Object} options - Button options
     * @param {string} [options.variant='primary'] - Variant: primary, secondary, danger, ghost, icon
     * @param {string} [options.size='md'] - Size: sm, md, lg
     * @param {boolean} [options.disabled=false] - Whether button is disabled
     * @param {boolean} [options.loading=false] - Show loading spinner
     * @param {string} [options.icon=''] - Icon text/emoji before button text
     * @param {Function} [options.onClick] - Click handler
     * @returns {HTMLButtonElement}
     */
    function button(text, options = {}) {
        const {
            variant = 'primary',
            size = 'md',
            disabled = false,
            loading = false,
            icon = '',
            onClick = null
        } = options;

        const btn = document.createElement('button');
        btn.className = `shared-btn shared-btn--${variant} shared-btn--${size}`;
        if (disabled) btn.disabled = true;
        if (loading) btn.classList.add('shared-btn--loading');

        let inner = '';
        if (loading) {
            inner += '<span class="shared-btn__spinner"></span>';
        }
        if (icon) {
            inner += `<span class="shared-btn__icon">${escapeHtml(icon)}</span>`;
        }
        inner += `<span class="shared-btn__text">${escapeHtml(text)}</span>`;
        btn.innerHTML = inner;

        if (onClick) {
            btn.addEventListener('click', onClick);
        }

        return btn;
    }

    // ── Form Field Factory ──

    /**
     * Create a form field with label and validation
     * @param {Object} options - Field options
     * @param {string} options.label - Field label
     * @param {string} [options.type='text'] - Input type: text, password, email, number, textarea, select
     * @param {string} [options.name=''] - Field name
     * @param {string} [options.placeholder=''] - Placeholder text
     * @param {string} [options.value=''] - Initial value
     * @param {string} [options.helpText=''] - Help text below field
     * @param {string} [options.error=''] - Error message (if any)
     * @param {boolean} [options.required=false] - Whether field is required
     * @param {Array} [options.options=[]] - Select options: [{value, label}]
     * @returns {HTMLElement} Field wrapper element
     */
    function formField(options = {}) {
        const {
            label = '',
            type = 'text',
            name = '',
            placeholder = '',
            value = '',
            helpText = '',
            error = '',
            required = false,
            options: selectOptions = []
        } = options;

        const wrapper = document.createElement('div');
        wrapper.className = 'shared-form-field';
        if (error) wrapper.classList.add('shared-form-field--error');

        const id = `field-${name}-${Date.now()}`;

        let inputHtml = '';
        if (type === 'textarea') {
            inputHtml = `<textarea id="${id}" name="${escapeHtml(name)}" class="shared-form-input" placeholder="${escapeHtml(placeholder)}" ${required ? 'required' : ''}>${escapeHtml(value)}</textarea>`;
        } else if (type === 'select') {
            const optionHtml = selectOptions.map(opt =>
                `<option value="${escapeHtml(opt.value)}">${escapeHtml(opt.label)}</option>`
            ).join('');
            inputHtml = `<select id="${id}" name="${escapeHtml(name)}" class="shared-form-input" ${required ? 'required' : ''}>${optionHtml}</select>`;
        } else {
            inputHtml = `<input type="${escapeHtml(type)}" id="${id}" name="${escapeHtml(name)}" class="shared-form-input" placeholder="${escapeHtml(placeholder)}" value="${escapeHtml(value)}" ${required ? 'required' : ''}>`;
        }

        wrapper.innerHTML = `
            <label class="shared-form-label" for="${id}">
                ${escapeHtml(label)} ${required ? '<span class="shared-form-required">*</span>' : ''}
            </label>
            ${inputHtml}
            ${helpText ? `<span class="shared-form-help">${escapeHtml(helpText)}</span>` : ''}
            ${error ? `<span class="shared-form-error">${escapeHtml(error)}</span>` : ''}
        `;

        return wrapper;
    }

    // ── Data Table Factory ──

    /**
     * Create a sortable, filterable data table
     * @param {Object} options - Table options
     * @param {Array} [options.columns=[]] - Column definitions: [{key, label, sortable, width}]
     * @param {Array} [options.data=[]] - Data rows
     * @param {boolean} [options.striped=true] - Alternating row colors
     * @param {boolean} [options.hover=true] - Row hover effect
     * @param {boolean} [options.pagination=false] - Enable pagination
     * @param {number} [options.pageSize=20] - Rows per page
     * @param {Function} [options.onRowClick] - Row click handler
     * @param {Function} [options.onSort] - Sort change handler
     * @returns {Object} Table API with render(), updateData(), and element properties
     */
    function dataTable(options = {}) {
        const {
            columns = [],
            data = [],
            striped = true,
            hover = true,
            pagination = false,
            pageSize = 20,
            onRowClick = null,
            onSort = null
        } = options;

        let currentPage = 1;
        let sortColumn = null;
        let sortDirection = 'asc';
        let filteredData = [...data];

        // Create table container
        const container = document.createElement('div');
        container.className = 'shared-data-table';

        const tableClass = 'shared-table';
        const stripedClass = striped ? 'shared-table--striped' : '';
        const hoverClass = hover ? 'shared-table--hover' : '';

        // Build header
        const headerHtml = columns.map(col => {
            const sortableClass = col.sortable ? 'shared-table__header--sortable' : '';
            const sortIcon = sortColumn === col.key ? (sortDirection === 'asc' ? ' ↑' : ' ↓') : '';
            return `<th class="shared-table__header ${sortableClass}" data-key="${escapeHtml(col.key)}" style="${col.width ? `width: ${col.width}` : ''}">
                ${escapeHtml(col.label)}${sortIcon}
            </th>`;
        }).join('');

        // Build rows
        const buildRows = () => {
            const start = pagination ? (currentPage - 1) * pageSize : 0;
            const end = pagination ? Math.min(start + pageSize, filteredData.length) : filteredData.length;
            const pageData = filteredData.slice(start, end);

            return pageData.map((row, idx) => {
                const cells = columns.map(col => {
                    const value = row[col.key];
                    const cellContent = typeof value === 'object' && value !== null ? JSON.stringify(value) : String(value || '');
                    return `<td>${cellContent}</td>`;
                }).join('');
                return `<tr data-index="${idx}" data-row-key="${row.id || idx}">${cells}</tr>`;
            }).join('');
        };

        // Build pagination
        const buildPagination = () => {
            if (!pagination) return '';
            const totalPages = Math.ceil(filteredData.length / pageSize);
            if (totalPages <= 1) return '';

            let paginationHtml = '<div class="shared-pagination">';
            paginationHtml += `<span class="shared-pagination__info">Page ${currentPage} of ${totalPages}</span>`;
            paginationHtml += `<button class="shared-btn shared-btn--sm shared-btn--secondary" ${currentPage === 1 ? 'disabled' : ''} data-action="prev">Previous</button>`;
            paginationHtml += `<button class="shared-btn shared-btn--sm shared-btn--secondary" ${currentPage === totalPages ? 'disabled' : ''} data-action="next">Next</button>`;
            paginationHtml += '</div>';
            return paginationHtml;
        };

        container.innerHTML = `
            <div class="shared-table__wrapper">
                <table class="${tableClass} ${stripedClass} ${hoverClass}">
                    <thead>
                        <tr>${headerHtml}</tr>
                    </thead>
                    <tbody>
                        ${buildRows()}
                    </tbody>
                </table>
            </div>
            ${buildPagination()}
        `;

        // Sort handler
        container.querySelectorAll('.shared-table__header--sortable').forEach(header => {
            header.addEventListener('click', () => {
                const key = header.dataset.key;
                if (sortColumn === key) {
                    sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
                } else {
                    sortColumn = key;
                    sortDirection = 'asc';
                }

                // Sort data
                filteredData.sort((a, b) => {
                    const aVal = a[key];
                    const bVal = b[key];
                    if (aVal < bVal) return sortDirection === 'asc' ? -1 : 1;
                    if (aVal > bVal) return sortDirection === 'asc' ? 1 : -1;
                    return 0;
                });

                // Re-render
                render();
                if (onSort) onSort({ key, direction: sortDirection });
            });
        });

        // Row click handler
        if (onRowClick) {
            container.addEventListener('click', (e) => {
                const row = e.target.closest('tr[data-row-key]');
                if (row) {
                    const index = parseInt(row.dataset.index);
                    onRowClick(filteredData[index], index);
                }
            });
        }

        // Pagination handlers
        container.addEventListener('click', (e) => {
            const btn = e.target.closest('[data-action]');
            if (!btn) return;
            const action = btn.dataset.action;
            const totalPages = Math.ceil(filteredData.length / pageSize);

            if (action === 'prev' && currentPage > 1) {
                currentPage--;
                render();
            } else if (action === 'next' && currentPage < totalPages) {
                currentPage++;
                render();
            }
        });

        const render = () => {
            const tbody = container.querySelector('tbody');
            if (tbody) tbody.innerHTML = buildRows();

            // Update pagination
            const pagination = container.querySelector('.shared-pagination');
            if (pagination) {
                pagination.outerHTML = buildPagination();
            }

            // Update sort indicators
            container.querySelectorAll('.shared-table__header').forEach(header => {
                const key = header.dataset.key;
                const sortIcon = sortColumn === key ? (sortDirection === 'asc' ? ' ↑' : ' ↓') : '';
                const label = columns.find(c => c.key === key)?.label || '';
                header.textContent = label + sortIcon;
            });
        };

        return {
            element: container,
            render,
            updateData(newData) {
                filteredData = [...newData];
                render();
            },
            getData() {
                return filteredData;
            },
            setFilter(filterFn) {
                filteredData = data.filter(filterFn);
                currentPage = 1;
                render();
            }
        };
    }

    // ── Progress Bar ──

    /**
     * Create a progress bar
     * @param {Object} options - Progress bar options
     * @param {number} [options.value=0] - Current value (0-100)
     * @param {string} [options.variant='info'] - Variant: info, success, warning, danger
     * @param {boolean} [options.indeterminate=false] - Show indeterminate animation
     * @param {string} [options.label=''] - Label text
     * @returns {Object} Progress bar API with element and setValue()
     */
    function progressBar(options = {}) {
        const {
            value = 0,
            variant = 'info',
            indeterminate = false,
            label = ''
        } = options;

        const wrapper = document.createElement('div');
        wrapper.className = 'shared-progress';

        const indeterminateClass = indeterminate ? 'shared-progress__bar--indeterminate' : '';
        const variantClass = `shared-progress__bar--${variant}`;

        wrapper.innerHTML = `
            ${label ? `<div class="shared-progress__label">${escapeHtml(label)} <span class="shared-progress__value">${value}%</span></div>` : ''}
            <div class="shared-progress__track">
                <div class="shared-progress__bar ${variantClass} ${indeterminateClass}" style="width: ${value}%"></div>
            </div>
        `;

        const setValue = (newValue) => {
            const bar = wrapper.querySelector('.shared-progress__bar');
            const valueLabel = wrapper.querySelector('.shared-progress__value');
            if (bar) bar.style.width = `${newValue}%`;
            if (valueLabel) valueLabel.textContent = `${newValue}%`;
        };

        return { element: wrapper, setValue };
    }

    // ── Badge/Tag ──

    /**
     * Create a badge/tag
     * @param {string} text - Badge text
     * @param {Object} options - Badge options
     * @param {string} [options.variant='info'] - Variant: info, success, warning, danger, neutral
     * @param {boolean} [options.removable=false] - Show remove button
     * @param {Function} [options.onRemove] - Remove callback
     * @returns {HTMLElement}
     */
    function badge(text, options = {}) {
        const {
            variant = 'info',
            removable = false,
            onRemove = null
        } = options;

        const span = document.createElement('span');
        span.className = `shared-badge shared-badge--${variant}`;

        let inner = escapeHtml(text);
        if (removable) {
            inner += `<button class="shared-badge__remove" aria-label="Remove">&times;</button>`;
            if (onRemove) {
                span.querySelector('.shared-badge__remove').addEventListener('click', (e) => {
                    e.stopPropagation();
                    onRemove(span);
                });
            }
        }

        span.innerHTML = inner;
        return span;
    }

    // ── Card Component ──

    /**
     * Create a card component
     * @param {Object} options - Card options
     * @param {string} [options.title=''] - Card title
     * @param {string|HTMLElement} [options.body=''] - Card body content
     * @param {string} [options.footer=''] - Card footer content
     * @param {string} [options.icon=''] - Card icon emoji/text
     * @param {Function} [options.onClick] - Card click handler
     * @returns {HTMLElement}
     */
    function card(options = {}) {
        const {
            title = '',
            body = '',
            footer = '',
            icon = '',
            onClick = null
        } = options;

        const div = document.createElement('div');
        div.className = 'shared-card';
        if (onClick) div.classList.add('shared-card--clickable');

        const bodyContent = body instanceof HTMLElement ? '' : body;
        div.innerHTML = `
            ${title ? `
            <div class="shared-card__header">
                ${icon ? `<span class="shared-card__icon">${escapeHtml(icon)}</span>` : ''}
                <h4 class="shared-card__title">${escapeHtml(title)}</h4>
            </div>` : ''}
            <div class="shared-card__body">
                ${bodyContent}
            </div>
            ${footer ? `<div class="shared-card__footer">${typeof footer === 'string' ? footer : ''}</div>` : ''}
        `;

        // Append body element if provided
        if (body instanceof HTMLElement) {
            div.querySelector('.shared-card__body').appendChild(body);
        }
        // Append footer element if provided
        if (footer instanceof HTMLElement) {
            const footerEl = div.querySelector('.shared-card__footer');
            if (footerEl) footerEl.appendChild(footer);
        }

        if (onClick) {
            div.addEventListener('click', onClick);
        }

        return div;
    }

    // ── Dropdown/Select Menu ──

    /**
     * Create a dropdown menu
     * @param {Object} options - Dropdown options
     * @param {string} [options.triggerText='Options'] - Trigger button text
     * @param {Array} [options.items=[]] - Menu items: [{label, value, icon, disabled, divider}]
     * @param {Function} [options.onSelect] - Selection handler
     * @returns {Object} Dropdown API with element, open(), close()
     */
    function dropdown(options = {}) {
        const {
            triggerText = 'Options',
            items = [],
            onSelect = null
        } = options;

        const wrapper = document.createElement('div');
        wrapper.className = 'shared-dropdown';

        const menuId = `dropdown-${Date.now()}`;
        wrapper.innerHTML = `
            <button class="shared-dropdown__trigger" aria-haspopup="true" aria-expanded="false" aria-controls="${menuId}">
                ${escapeHtml(triggerText)}
                <span class="shared-dropdown__arrow">▾</span>
            </button>
            <ul class="shared-dropdown__menu hidden" id="${menuId}" role="menu">
                ${items.map((item, idx) => {
                    if (item.divider) return '<li class="shared-dropdown__divider" role="separator"></li>';
                    const disabledClass = item.disabled ? 'shared-dropdown__item--disabled' : '';
                    return `<li class="shared-dropdown__item ${disabledClass}" role="menuitem" data-value="${escapeHtml(item.value || '')}" data-index="${idx}">
                        ${item.icon ? `<span class="shared-dropdown__icon">${escapeHtml(item.icon)}</span>` : ''}
                        <span class="shared-dropdown__label">${escapeHtml(item.label)}</span>
                    </li>`;
                }).join('')}
            </ul>
        `;

        const trigger = wrapper.querySelector('.shared-dropdown__trigger');
        const menu = wrapper.querySelector('.shared-dropdown__menu');
        let isOpen = false;

        const open = () => {
            isOpen = true;
            menu.classList.remove('hidden');
            trigger.setAttribute('aria-expanded', 'true');
        };

        const close = () => {
            isOpen = false;
            menu.classList.add('hidden');
            trigger.setAttribute('aria-expanded', 'false');
        };

        const toggle = () => {
            if (isOpen) close();
            else open();
        };

        trigger.addEventListener('click', (e) => {
            e.stopPropagation();
            toggle();
        });

        // Close on outside click
        document.addEventListener('click', (e) => {
            if (!wrapper.contains(e.target) && isOpen) {
                close();
            }
        });

        // Item selection
        menu.addEventListener('click', (e) => {
            const item = e.target.closest('.shared-dropdown__item');
            if (!item || item.classList.contains('shared-dropdown__item--disabled')) return;

            const index = parseInt(item.dataset.index);
            const value = item.dataset.value;
            close();
            if (onSelect) onSelect(items[index], value, index);
        });

        return { element: wrapper, open, close, toggle };
    }

    // ── Spinner/Loading Indicator ──

    /**
     * Create a loading spinner
     * @param {Object} options - Spinner options
     * @param {string} [options.size='md'] - Size: sm, md, lg
     * @param {string} [options.text=''] - Loading text
     * @returns {HTMLElement}
     */
    function spinner(options = {}) {
        const {
            size = 'md',
            text = ''
        } = options;

        const div = document.createElement('div');
        div.className = `shared-spinner shared-spinner--${size}`;

        if (text) {
            div.innerHTML = `<div class="shared-spinner__circle"></div><span class="shared-spinner__text">${escapeHtml(text)}</span>`;
        } else {
            div.innerHTML = `<div class="shared-spinner__circle"></div>`;
        }

        return div;
    }

    // ── Tooltip ──

    /**
     * Add a tooltip to an element
     * @param {HTMLElement} element - Element to add tooltip to
     * @param {string} text - Tooltip text
     * @param {Object} options - Tooltip options
     * @param {string} [options.position='top'] - Position: top, bottom, left, right
     * @returns {Object} Tooltip API with show(), hide(), destroy()
     */
    function tooltip(element, text, options = {}) {
        const {
            position = 'top'
        } = options;

        const tooltipEl = document.createElement('div');
        tooltipEl.className = `shared-tooltip shared-tooltip--${position}`;
        tooltipEl.setAttribute('role', 'tooltip');
        tooltipEl.textContent = text;
        document.body.appendChild(tooltipEl);

        const show = () => {
            const rect = element.getBoundingClientRect();
            const tooltipRect = tooltipEl.getBoundingClientRect();

            let top, left;
            switch (position) {
                case 'top':
                    top = rect.top - tooltipRect.height - 8;
                    left = rect.left + (rect.width - tooltipRect.width) / 2;
                    break;
                case 'bottom':
                    top = rect.bottom + 8;
                    left = rect.left + (rect.width - tooltipRect.width) / 2;
                    break;
                case 'left':
                    top = rect.top + (rect.height - tooltipRect.height) / 2;
                    left = rect.left - tooltipRect.width - 8;
                    break;
                case 'right':
                    top = rect.top + (rect.height - tooltipRect.height) / 2;
                    left = rect.right + 8;
                    break;
            }

            // Keep within viewport
            left = Math.max(8, Math.min(left, window.innerWidth - tooltipRect.width - 8));
            top = Math.max(8, Math.min(top, window.innerHeight - tooltipRect.height - 8));

            tooltipEl.style.top = `${top}px`;
            tooltipEl.style.left = `${left}px`;
            tooltipEl.classList.add('shared-tooltip--visible');
        };

        const hide = () => {
            tooltipEl.classList.remove('shared-tooltip--visible');
        };

        const destroy = () => {
            element.removeEventListener('mouseenter', show);
            element.removeEventListener('mouseleave', hide);
            element.removeEventListener('focus', show);
            element.removeEventListener('blur', hide);
            if (tooltipEl.parentNode) {
                tooltipEl.parentNode.removeChild(tooltipEl);
            }
        };

        element.addEventListener('mouseenter', show);
        element.addEventListener('mouseleave', hide);
        element.addEventListener('focus', show);
        element.addEventListener('blur', hide);

        return { show, hide, destroy };
    }

    // ── Utility Functions ──

    /**
     * Escape HTML to prevent XSS
     * @param {string} text - Text to escape
     * @returns {string}
     * @private
     */
    function escapeHtml(text) {
        if (text === null || text === undefined) return '';
        const div = document.createElement('div');
        div.textContent = String(text);
        return div.innerHTML;
    }

    // ── Public API ──
    return {
        toast,
        modal,
        button,
        formField,
        dataTable,
        progressBar,
        badge,
        card,
        dropdown,
        spinner,
        tooltip
    };
})();
