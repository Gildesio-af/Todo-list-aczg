import { FILTER_TYPES, TASK_STATUS } from './constants.js';
import * as Storage from './storage.js';
import * as UI from './ui.js';

let currentFilter = FILTER_TYPES.ALL;
let searchQuery = '';
let currentEditingTaskId = null;
let currentViewingTask = null;

const formTask = document.getElementById('createTaskForm');
const modal = document.getElementById('taskModal');
const detailsModal = document.getElementById('taskDetailsModal');
const searchInput = document.getElementById('search-input');
const sidebarItems = document.querySelectorAll('aside ul li[data-filter]');

const refreshApp = () => {
    const tasksToRender = Storage.getFilteredTasks(currentFilter, searchQuery);

    const actionHandlers = {
        onViewDetails: (task) => openDetailsModal(task),
        onToggleStatus: (id, isChecked) => { Storage.toggleTaskStatus(id, isChecked); refreshApp(); },
        onDelete: (id) => { Storage.deleteTask(id); refreshApp(); },
        onEdit: (task) => populateFormForEdit(task)
    };

    UI.renderTasks(tasksToRender, currentFilter, actionHandlers);
    UI.updatePageHeader(currentFilter);
};


const openModal = () => { UI.updateModalUI(false); modal.classList.remove('hidden'); };
const closeModal = () => { modal.classList.add('hidden'); formTask.reset(); currentEditingTaskId = null; };
const closeDetailsModal = () => detailsModal.classList.add('hidden');

const populateFormForEdit = (task) => {
    formTask.elements['name'].value = task.name;
    formTask.elements['description'].value = task.description || '';
    formTask.elements['category'].value = task.category || '';
    formTask.elements['priority'].value = task.priority || '1';
    formTask.elements['startDate'].value = task.startDate || '';
    formTask.elements['endDate'].value = task.endDate || '';
    formTask.elements['status'].value = task.status || TASK_STATUS.TODO;
    currentEditingTaskId = task.id;
    UI.updateModalUI(true);
    modal.classList.remove('hidden');
};

const openDetailsModal = (task) => {
    currentViewingTask = task;
    document.getElementById('detailsTaskName').textContent = task.name;
    document.getElementById('detailsTaskDescription').textContent = task.description || 'Sem descrição';
    document.getElementById('detailsTaskCategory').textContent = task.category || 'Sem categoria';
    document.getElementById('detailsTaskPriority').textContent = task.priority;
    document.getElementById('detailsTaskStartDate').textContent = UI.formatDateWithTime(task.startDate);
    document.getElementById('detailsTaskEndDate').textContent = UI.formatDateWithTime(task.endDate);
    document.getElementById('detailsTaskStatus').textContent = task.status;
    detailsModal.classList.remove('hidden');
};

document.getElementById('btn-new-task').addEventListener('click', openModal);
document.getElementById('closeModalBtn').addEventListener('click', closeModal);
document.getElementById('cancelTaskBtn').addEventListener('click', closeModal);
document.getElementById('closeDetailsModalBtn').addEventListener('click', closeDetailsModal);
document.getElementById('closeDetailsBtn').addEventListener('click', closeDetailsModal);

modal.addEventListener('click', (e) => { if (e.target === modal) closeModal(); });
detailsModal.addEventListener('click', (e) => { if (e.target === detailsModal) closeDetailsModal(); });

sidebarItems.forEach(item => {
    item.addEventListener('click', () => {
        sidebarItems.forEach(i => i.classList.remove('aside-session-selected'));
        item.classList.add('aside-session-selected');
        currentFilter = item.dataset.filter;
        refreshApp();
    });
});

searchInput.addEventListener('input', (event) => {
    searchQuery = event.target.value;
    refreshApp();
});


formTask.addEventListener('submit', (event) => {
    event.preventDefault();
    const formData = new FormData(formTask);
    const startDate = formData.get('startDate');
    const endDate = formData.get('endDate');

    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        alert("A data de término deve ser maior que a data de início.");
        return;
    }

    const taskDTO = {
        id: currentEditingTaskId || Date.now(),
        name: formData.get('name'),
        description: formData.get('description'),
        priority: formData.get('priority'),
        startDate: startDate || null,
        endDate: endDate || null,
        category: formData.get('category'),
        status: formData.get('status')
    };

    if (currentEditingTaskId) {
        Storage.updateTask(currentEditingTaskId, taskDTO);
    } else {
        Storage.addTask(taskDTO);
    }

    refreshApp();
    closeModal();
});


refreshApp();