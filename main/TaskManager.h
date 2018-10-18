/*
 *	TaskManager.h
 *
 *	copyright (c) Andreas Kraft 2018
 *	Licensed under the BSD 3-Clause License. See the LICENSE file for further details.
 *
 *	Implementation of a small Arduino task manager for running small tasks.
 */

# ifndef __TASKMANAGER_H__
# define __TASKMANAGER_H__

# include "LinkedList.h"

typedef bool (*TaskHandler)();

// Structure to represent a single task
typedef struct {
	long 			id;
	TaskHandler 	taskHandler;
	TaskHandler 	initTaskHandler;
	TaskHandler 	deinitTaskHandler;
	unsigned long 	interval;
	unsigned long 	nextRun;
	unsigned long	runUntil;	// run for n ms
	long			iterations;
	long 			runCount;
	bool 			running;
	bool			inStart;
	bool			inStop;
	bool 			runOnTime;	// true = try to run exactly on time slice, otherwise now + intervall
} Task;


// The actual Task Manager
class TaskManager {

private:
	LinkedList<Task *> 	tasks;		// List of tasks
	long 				nextID;		// next uniq ID for tasks
	unsigned long		runTaskMs;	// Current millis to use globally for current runTasks

	Task 	*_getTaskById(long taskId);

public:
	TaskManager();
	~TaskManager();

	// Check for runnable tasks and execute them.
	// This method must be called very often and regularly, ideally in the loop() function of a sketch.
	void    runTasks();

  	// Add a new task.
  	// *taskHandler* is a pointer to a function that is called for executing the task.
  	// *interval* is the time in milliseconds between task executions.
  	// The method returns a *taskID*, or -1 in case of an error.
	long 	addTask(TaskHandler taskHandler, unsigned long interval);

	// Add a new task.
  	// *taskHandler* is a pointer to a function that is called for executing the task.
  	// *interval* is the time in milliseconds between task executions.
  	// *autoStart* indicates whether the task execution should start implicitly, or must be started via one of the *startTask()* methods.
  	// The method returns a *taskID*, or -1 in case of an error.
	long 	addTask(TaskHandler taskHandler, unsigned long interval, bool autoStart);

	// Add a new task.
  	// *taskHandler* is a pointer to a function that is called for executing the task.
  	// *initTaskHandler* is a pointer to a function that is called once when the task is started. Might be *NULL*.
  	// *deinitTaskHandler* is a pointer to a function that is called once when the task is stopped. Might be *NULL*.
  	// *interval* is the time in milliseconds between task executions.
  	// *autoStart* indicates whether the task execution should start implicitly, or must be started via one of the *startTask()* methods.
  	// The method returns a *taskID*, or -1 in case of an error.
	long 	addTask(TaskHandler taskHandler, TaskHandler initTaskHandler, TaskHandler deinitTaskHandler, unsigned long interval, bool autoStart);

	// Remove a task from the task manager.
	// *taskId* is the ID of the task to be removed.
 	void 	removeTask(long taskId);

 	// Stop and remove all tasks and reset the task manager.
 	void	reset();

 	// Check whether a task is currently running.
	bool 	isTaskRunning(long taskId);

	// Start a task.
	// *taskId* is the ID of the task to be started.
	void    startTask(long taskId);

	// Start a task.
	// *taskId* is the ID of the task to be started.
	// *startAfter* is the number of milliseconds after which the task is executed for the first time. 0 means immediately.
	// *runFor* is the number of time in milliseconds for the task to run. 0 means forever.
	// *iterations* is the number of times for the task to run. 0 means forever.
	void    startTask(long taskId, unsigned long startAfter, unsigned long runFor, unsigned long iterations);

	// Start a task.
	// *taskId* is the ID of the task to be started.
	// *startAfter* is the number of milliseconds after which the task is executed for the first time. 0 means immediately.
	// *runFor* is the number of time in milliseconds for the task to run. 0 means forever.
	// *iterations* is the number of times for the task to run. 0 means forever.
 	// *runOnTime* if true then the TasManager tries to run the task exactly on the time slice (ie. after the interval time, including the time the task needs to execute), otherwise after *interval* ms after the task handler returned.  The default for other *startTask()* methods for this parameter is *true*.
	void    startTask(long taskId, unsigned long startAfter, unsigned long runFor, unsigned long iterations, bool runOnTime);

	// Stop a task.
	// *taskId* is the ID of the task to be stopped.
	void    stopTask(long taskId);
};

# endif
