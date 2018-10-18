/*
 *  DS2413helper.h
 *
 *  copyright (c) Deutsche Telekom AG 2018
 *  Licensed under the BSD 3-Clause License. See the LICENSE file for further details.
 */

#ifndef __DS2413HELPER__
#define __DS2413HELPER__

#define TOUCH_FAILURE -1

//14,11,15,10 come from conversion hex->int from incoming DS2413 values
#define TOUCH_A 14
#define TOUCH_B 11
#define TOUCH_BOTH 15
#define TOUCH_NONE 10

//values simply defined, not to be confused with the DS2413 values
#define TOUCH_A2B 89
#define TOUCH_B2A 98
#define TOUCH_TIMEOUT 0

//declaration
typedef struct {
  int a;
  int b;
  unsigned long timestamp;
  int type;
} TouchInfoT;

TouchInfoT *readDS2413();

#endif
