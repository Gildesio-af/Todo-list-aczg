import { FILTER_TYPES, TASK_STATUS } from './constants.js';

let tasks = JSON.parse(localStorage.getItem('tasks')) || [];

const save = () => localStorage.setItem('tasks', JSON.stringify(tasks));

export const getTasks = () => tasks;

export const addTask = (taskDTO) => {
    tasks.push(taskDTO);
    save();
};

export const updateTask = (id, taskDTO) => {
    const index = tasks.findIndex(t => t.id === id);
    if (index !== -1) {
        tasks[index] = taskDTO;
        save();
    }
};

export const deleteTask = (id) => {
    tasks = tasks.filter(t => t.id !== id);
    save();
};

export const toggleTaskStatus = (id, isDone) => {
    const task = tasks.find(t => t.id === id);
    if (task) {
        task.status = isDone ? TASK_STATUS.DONE : TASK_STATUS.TODO;
        save();
    }
};

export const getTaskByName = (name) => {
    return tasks.find(t => t.name.toLowerCase() === name.toLowerCase());
};

export const getFilteredTasks = (currentFilter, searchQuery) => {
    let filtered = [...tasks];

    if (currentFilter === FILTER_TYPES.IMPORTANT) {
        filtered.sort((a, b) => Number(b.priority) - Number(a.priority));
    } else if (currentFilter === FILTER_TYPES.DONE) {
        filtered = filtered.filter(task => task.status === TASK_STATUS.DONE);
    }

    if (searchQuery.trim() !== '') {
        const query = searchQuery.toLowerCase();
        filtered = filtered.filter(task =>
            (task.name && task.name.toLowerCase().includes(query)) ||
            (task.category && task.category.toLowerCase().includes(query))
        );
    }

    return filtered;
};