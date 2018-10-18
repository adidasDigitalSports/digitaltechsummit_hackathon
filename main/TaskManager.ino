/*
 *	TaskManager.ino
 *
 *	copyright (c) Andreas Kraft 2018
 *	Licensed under the BSD 3-Clause License. See the LICENSE file for further details.
 *
 *	Implementation of a small Arduino task manager for running small tasks.
 */

#include "TaskManager.h"

// extern int usbValue;
// extern int batValue;

TaskManager::TaskManager() {
	nextID = 0;
}

TaskManager::~TaskManager() {
	reset();
}


void TaskManager::runTasks() {
	runTaskMs = millis();

	for (int i = 0; i < tasks.size(); i++) {
		Task *task = tasks.get(i);

		if (task->running && runTaskMs >= task->nextRun) {
			if (task->runOnTime) {
				task->nextRun = task->nextRun + task->interval; // next run: n ms measured from *before* current task execution
			}
			if (task->inStart || task->inStop) {	// guard against exec tasks in start or stop handler
				continue;
			}

			bool result = (*task->taskHandler)();
			task->runCount++;
			if ( ! task->runOnTime) {
				task->nextRun = millis() + task->interval; // next run: current time, after task handler returned, + intervall ms
			}

			if ( ! result) {
				stopTask(task->id);
				continue;
			}
			// handle iterations
			if (task->iterations > 0 && task->runCount >= task->iterations) {
				stopTask(task->id);
				continue;
			}
		}
		// handle run until. Stop task when end is reached
		if (task->runUntil > 0 && runTaskMs > task->runUntil) {
			stopTask(task->id);
			continue;
		}
	}

	runTaskMs = 0;
}


long TaskManager::addTask(TaskHandler taskHandler, unsigned long interval) {
	return addTask(taskHandler, interval, true);
}


long TaskManager::addTask(TaskHandler taskHandler, unsigned long interval, bool autoStart) {
	return addTask(taskHandler, NULL, NULL, interval, autoStart);
}

long TaskManager::addTask(TaskHandler taskHandler, TaskHandler initTaskHandler, TaskHandler deinitTaskHandler, unsigned long interval, bool autoStart) {
	Task *task = new Task();
	task->id = nextID++;
	task->taskHandler = taskHandler;
	task->initTaskHandler = initTaskHandler;
	task->deinitTaskHandler = deinitTaskHandler;
	task->interval = interval;
	task->iterations = 0;	// TODO: iterations not supported at the moment.
	task->runCount = 0;
	task->nextRun = 0;
	task->runUntil = 0;
	task->running = false;
	task->inStart = false;
	task->inStop = false;
	task->runOnTime = true;
	tasks.add(task);

	if (autoStart) {
		startTask(task->id);
	}

	return task->id;
}


void TaskManager::removeTask(long taskId) {
	for (int i = 0; i < tasks.size(); i++) {
		Task *task = tasks.get(i);
		if (task->id == taskId) {
			stopTask(task->id);
			tasks.remove(i);
			delete(task);
			return;
		}
	}
}


void TaskManager::reset() {
	while (tasks.size() > 0) {
		Task *task = tasks.get(0);
		stopTask(task->id);
		tasks.remove(0);
		delete(task);
	}
}


bool TaskManager::isTaskRunning(long taskId) {
	Task *task = _getTaskById(taskId);
	return task? task->running : false;
}


void TaskManager::startTask(long taskId) {
	startTask(taskId, 0, 0, 0);
}



void TaskManager::startTask(long taskId, unsigned long startAfter, unsigned long runFor, unsigned long iterations) {
	startTask(taskId, startAfter, runFor, iterations, true);
}

void TaskManager::startTask(long taskId, unsigned long startAfter, unsigned long runFor, unsigned long iterations, bool runOnTime) {
	Task *task = _getTaskById(taskId);
	if (task) {
		if (task->inStart) {	// prevent double/endless calls
			return;
		}
		if (task->initTaskHandler) {
			task->inStart = true;
			bool result = (*task->initTaskHandler)();
			task->inStart = false;
			if ( ! result ) {	// no, then don't start
				return;
			}
		}
		task->runCount = 0;
		task->running = true;
		task->nextRun = (runTaskMs > 0 ? runTaskMs : millis()) + startAfter;
		task->runOnTime = runOnTime;

		if (runFor > 0) {
			task->runUntil = task->nextRun + runFor;
		}
		if (iterations > 0) {
			task->iterations = iterations;
		}
	}
}


void TaskManager::stopTask(long taskId) {
	Task *task = _getTaskById(taskId);

	if (task) {
		if (task->inStop) {	// prevent double/endless calls
			return;
		}
		task->running = false;
		if (task->deinitTaskHandler) {
			task->inStop = true;
			bool result = (*task->deinitTaskHandler)();
			task->inStop = false;
			if (task->deinitTaskHandler) {
				if ( ! result) {	// no, then don't stop
					task->running = true;
					return;
				}
			}
		}
	}
}


Task *TaskManager::_getTaskById(long taskId) {
	for (int i = 0; i < tasks.size(); i++) {
		Task *task = tasks.get(i);
		if (task->id == taskId) {
			return task;
		}
	}
	return NULL;
}
