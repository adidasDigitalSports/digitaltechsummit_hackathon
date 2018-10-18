/*
 *  plan.h
 *
 *  copyright (c) Deutsche Telekom AG 2018
 *  Licensed under the BSD 3-Clause License. See the LICENSE file for further details.
 */

#ifndef __PLAN_H__
#define __PLAN_H__


#define MAXPHASES 100

typedef struct {
	String 			name;
	String 			color;
	String 			blinkDirection;
	long 			blinkInterval;
	long 			duration;
	unsigned long   nextBlinkToggle;
	unsigned long 	phaseEnd;
} Phase;


typedef struct {
	String 			name;
	Phase 			phases[MAXPHASES];
	unsigned int 	phasesCount;
	int 			currentPhaseIndex;
	int 			iterations;
	int 			currentIteration;
} Plan;

#endif
