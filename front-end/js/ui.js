import { PRIORITY, TASK_STATUS } from './constants.js';

export const formatTime = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
};

export const formatDateWithTime = (dateString) => {
    if (!dateString) return 'Não definido';
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR') + ' às ' + date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
};

export const updateModalUI = (isEdit) => {
    const modalTitle = document.querySelector('#taskModal .sub-title');
    const submitBtn = document.querySelector('#taskModal .btn-submit');
    modalTitle.textContent = isEdit ? 'Editar Tarefa' : 'Nova Tarefa';
    submitBtn.textContent = isEdit ? 'Atualizar Tarefa' : 'Criar Tarefa';
};

export const updatePageHeader = (currentFilter) => {
    const pageTitle = document.querySelector('.main-container .title');
    const taskGroupHeader = document.querySelector('.task-group-header');

    const configs = {
        'all': { title: 'Minhas Tarefas', subtitle: 'Hoje', icon: './assets/calendar.svg' },
        'important': { title: 'Importantes', subtitle: 'Por Prioridade', icon: './assets/star.png' },
        'done': { title: 'Concluídas', subtitle: 'Finalizadas', icon: './assets/correct2.svg' }
    };

    const config = configs[currentFilter];
    pageTitle.textContent = config.title;
    taskGroupHeader.innerHTML = `
        <img src="${config.icon}" alt="Ícone">
        <h2 class="sub-title">${config.subtitle}</h2>
    `;
};

export const renderTasks = (filteredTasks, currentFilter, handlers) => {
    const taskList = document.querySelector('.task-list');
    taskList.innerHTML = '';

    if (filteredTasks.length === 0) {
        taskList.innerHTML = `
            <li class="task-item" style="justify-content: center; opacity: 0.5; padding: 24px;">
                <p class="text-black">Nenhuma tarefa encontrada.</p>
            </li>`;
        return;
    }

    filteredTasks.forEach(task => {
        const li = document.createElement('li');
        li.className = 'task-item';

        let timeString = task.startDate ? formatTime(task.startDate) : '';
        if (task.startDate && task.endDate) timeString += ' - ' + formatTime(task.endDate);
        else if (!task.startDate && task.endDate) timeString = formatTime(task.endDate);

        const metaHtml = timeString ? `<div class="task-meta"><img src="./assets/clock.svg" class="meta-icon"><span class="search-item">${timeString}</span></div>` : '';
        const priorityHtml = currentFilter === 'important' ? `<span class="priority-badge" style="background-color: ${PRIORITY.COLORS[task.priority] || '#6b7280'};">★ ${PRIORITY.LABELS[task.priority] || task.priority}</span>` : '';
        const categoryHtml = task.category ? `<span style="font-size: 12px; background: #e5e7eb; padding: 2px 8px; border-radius: 12px; color: #374151;">${task.category}</span>` : '';

        const isCompleted = task.status === TASK_STATUS.DONE;

        li.innerHTML = `
            <input type="checkbox" class="task-checkbox" ${isCompleted ? 'checked' : ''}>
            <div class="task-info" style="cursor: pointer; flex: 1;">
                <p class="text-black" style="${isCompleted ? 'text-decoration: line-through; opacity: 0.7;' : ''}">${task.name}</p>
                <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-top: 2px;">
                    ${metaHtml} ${priorityHtml} ${categoryHtml}
                </div>
            </div>
            <div style="display: flex; gap: 4px; margin-left: auto;">
                <button class="btn-edit" title="Editar tarefa"><span style="font-size: 16px;">✎</span></button>
                <button class="btn-delete" title="Excluir tarefa"><img src="./assets/trash.png" alt="Excluir tarefa"></button>
            </div>
        `;

        li.querySelector('.task-info').addEventListener('click', () => handlers.onViewDetails(task));
        li.querySelector('.task-checkbox').addEventListener('change', (e) => handlers.onToggleStatus(task.id, e.target.checked));
        li.querySelector('.btn-edit').addEventListener('click', (e) => { e.stopPropagation(); handlers.onEdit(task); });
        li.querySelector('.btn-delete').addEventListener('click', (e) => { e.stopPropagation(); handlers.onDelete(task.id); });

        taskList.appendChild(li);
    });
};